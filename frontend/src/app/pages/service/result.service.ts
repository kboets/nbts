import {computed, inject, Injectable, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {HttpClient} from '@angular/common/http';
import {combineLatest, Observable, of, tap} from 'rxjs';
import {catchError, distinctUntilChanged, map, shareReplay, switchMap} from 'rxjs/operators';
import {HttpErrorService} from './http-error.service';
import type {MatchResult} from '../shared/models/matchResult';
import {Result} from '../shared/models/result';

@Injectable({
    providedIn: 'root'
})
export class ResultService {
    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    constructor() { }

    /**  retrieve the selected results for a specific league and season */
    leagueForResult = signal<number | undefined>(undefined);
    seasonForResult = signal<string | undefined>(undefined);

    public selectLeagueForResult(leagueId: number) {
        this.leagueForResult.set(leagueId);
    }
    public resetLeagueForResult() {
        this.leagueForResult.set(undefined);
    }
    public selectSeasonForResult(season: number | string) {
        this.seasonForResult.set(String(season));
    }
    public resetSeasonForResult() {
        this.seasonForResult.set(undefined);
    }

    resultsByLeagueAndSeason$ = combineLatest([
        toObservable(this.leagueForResult),
        toObservable(this.seasonForResult)
    ]).pipe(
        distinctUntilChanged(([previousLeagueId, previousSeason], [leagueId, season]) =>
            previousLeagueId === leagueId && previousSeason === season
        ),
        switchMap(([leagueId, season]) => {
            if (leagueId === undefined || season === undefined) {
                return of({ data: [], error: undefined } as Result<MatchResult[]>);
            }

            return this.getResultsByLeagueAndSeason(leagueId, season)
                .pipe(
                    map((results) => ({ data: results }) as Result<MatchResult[]>),
                    catchError((error) =>
                        of({
                            data: [],
                            error: this.errorService.formatError(error),
                        } as Result<MatchResult[]>),
                    ),
                );
        }),
        tap((result) => console.log('resultsByLeagueAndSeason$', result)),
        shareReplay(1)
    );

    private selectedResultsResult = toSignal(this.resultsByLeagueAndSeason$, {
        initialValue: { data: [], error: undefined } as Result<MatchResult[]>,
    });
    results = computed(() => this.selectedResultsResult().data ?? []);
    resultsError = computed(() => this.selectedResultsResult().error);


    getResultsByLeagueAndSeason(leagueId: number, season: number | string): Observable<MatchResult[]> {
        return this.http.get<MatchResult[]>(`${this.baseUrl}/results/${leagueId}/${season}`);
    }

    getResults(leagueId: number, season: number | string): Observable<MatchResult[]> {
        return this.getResultsByLeagueAndSeason(leagueId, season);
    }
}
