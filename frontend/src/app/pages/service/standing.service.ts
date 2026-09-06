import {computed, inject, Injectable, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {HttpClient} from '@angular/common/http';
import {combineLatest, Observable, of, tap} from 'rxjs';
import {catchError, distinctUntilChanged, map, shareReplay, switchMap} from 'rxjs/operators';
import {HttpErrorService} from './http-error.service';
import type {Standing} from '../shared/models/standing';
import {Result} from "../shared/models/result";

@Injectable({
    providedIn: 'root'
})
export class StandingService {
    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    constructor() { }


    /**  retrieve the selected standings for a specific league and season */
    leagueForStanding = signal<number | undefined>(undefined);
    seasonForStanding = signal<string | undefined>(undefined);

    public selectLeagueForStanding(leagueId: number) {
        this.leagueForStanding.set(leagueId);
    }
    public resetLeagueForStanding() {
        this.leagueForStanding.set(undefined);
    }
    public selectSeasonForStanding(season: number | string) {
        this.seasonForStanding.set(String(season));
    }
    public resetSeasonForStanding() {
        this.seasonForStanding.set(undefined);
    }

    standingByLeagueAndSeason$ = combineLatest([
        toObservable(this.leagueForStanding),
        toObservable(this.seasonForStanding)
    ]).pipe(
        distinctUntilChanged(([previousLeagueId, previousSeason], [leagueId, season]) =>
            previousLeagueId === leagueId && previousSeason === season
        ),
        switchMap(([leagueId, season]) => {
            if (leagueId === undefined || season === undefined) {
                return of({ data: [], error: undefined } as Result<Standing[]>);
            }

            return this.getStandingForLeagueAndSeason(leagueId, season)
                .pipe(
                    map((results) => ({ data: results }) as Result<Standing[]>),
                    catchError((error) =>
                        of({
                            data: [],
                            error: this.errorService.formatError(error),
                        } as Result<Standing[]>),
                    ),
                );
        }),
        tap((result) => console.log('standingByLeagueAndSeason$', result)),
        shareReplay(1)
    );

    private selectedStandingResult = toSignal(this.standingByLeagueAndSeason$, {
        initialValue: { data: [], error: undefined } as Result<Standing[]>,
    });
    standing = computed(() => this.selectedStandingResult().data ?? []);
    standingError = computed(() => this.selectedStandingResult()?.error);

    getStandingForLeagueAndSeason(leagueId: number, season: string): Observable<any> {
        return this.http.get<Standing[]>(`${this.baseUrl}/standings/${leagueId}/${season}`);
    }
}
