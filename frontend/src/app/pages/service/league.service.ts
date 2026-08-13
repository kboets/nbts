import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import type {League} from "../shared/models/league";
import {debounceTime, distinctUntilChanged, map, Observable, of, switchMap} from 'rxjs';
import {catchError, shareReplay, tap} from 'rxjs/operators';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {HttpErrorService} from './http-error.service';
import {Result} from '../shared/models/result';


@Injectable({
    providedIn: 'root',
})
export class LeagueService {
    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    getCurrentLeagues(): Observable<League[]> {
        return this.http.get<League[]>(`${this.baseUrl}/currentLeagues`);
    }

    /** persist and remove persisted league */
    public saveNewLeague(league: League) : Observable<League> {
        return this.http.post<League>(`${this.baseUrl}/league`, league);
    }
    public removeLeague(league: League): Observable<boolean> {
        return this.http.delete<boolean>(`${this.baseUrl}/league`, { body: league });
    }

    /**  retrieve the selected leagues for a specific country */
    selectedCountryForSelectedLeagues = signal<string | undefined>(undefined);
    public selectCountryForSelectedLeagues(countryCode: string) {
        console.log('selectCountryForSelectedLeagues', countryCode);
        this.selectedCountryForSelectedLeagues.set(countryCode);
    }
    public resetCountryForSelectedLeagues() {
        this.selectedCountryForSelectedLeagues.set(undefined);
    }
    private selectedLeaguesForCountry$ = toObservable(this.selectedCountryForSelectedLeagues).pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((countryCode) => {
            if (!countryCode) {
                return of({ data: [], error: undefined } as Result<League[]>);
            }
            return this.getSelectedLeaguesForCountry(countryCode).pipe(
                map((leagues) => ({ data: leagues }) as Result<League[]>),
                catchError((error) =>
                    of({
                        data: [],
                        error: this.errorService.formatError(error),
                    } as Result<League[]>),
                ),
            );
        }),
        tap((result) => console.log('selectedLeaguesForCountry$', result)),
        shareReplay(1)
    );

    private getSelectedLeaguesForCountry(countryCode: string): Observable<League[]> {
        return this.http.get<League[]>(`${this.baseUrl}/currentLeagues/selected/${countryCode}`);
    }

    private selectedLeaguesResult = toSignal(this.selectedLeaguesForCountry$, { initialValue: { data: [], error: undefined } });
    selectedLeagues = computed(() => this.selectedLeaguesResult().data);
    selectedLeaguesError = computed(() => this.selectedLeaguesResult().error);

    /**  retrieve the new leagues for a specific country */
    selectedCountryForNewLeagues = signal<string | undefined>(undefined);

    public selectCountryForNewLeagues(countryCode: string) {
        this.selectedCountryForNewLeagues.set(countryCode);
    }

    public resetCountryForNewLeagues() {
        this.selectedCountryForNewLeagues.set(undefined);
    }

    private newLeaguesForCountry$ = toObservable(this.selectedCountryForNewLeagues).pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((countryCode) => {
             if (!countryCode) {
                return of({ data: [], error: undefined } as Result<League[]>);
            }
            return this.getCurrentLeaguesForCountry(countryCode).pipe(
                map((leagues) => ({ data: leagues }) as Result<League[]>),
                catchError((error) =>
                    of({
                        data: [],
                        error: this.errorService.formatError(error),
                    } as Result<League[]>),
                ),
            );
        }),
        tap((result) => console.log('newLeaguesForCountry$', result)),
        shareReplay(1)
    );

    private newLeaguesResult = toSignal(this.newLeaguesForCountry$, { initialValue: { data: [], error: undefined } });
    newLeagues = computed(() => this.newLeaguesResult().data);
    newLeaguesError = computed(() => this.newLeaguesResult().error);

    private getCurrentLeaguesForCountry(countryCode: string): Observable<League[]> {
        console.log('getCurrentLeaguesForCountry', countryCode);
        return this.http.get<League[]>(`${this.baseUrl}/currentLeagues/${countryCode}`);
    }

    private getDummyCurrentLeaguesForCountry(countryCode: string): Observable<League[]> {
        console.log('getDummyCurrentLeaguesForCountry', countryCode);
        return of([
            {
                leagueId: 144,
                countryCode: countryCode,
                name: 'Jupiler Pro League',
                logo: 'https://media.api-sports.io/football/leagues/144.png',
                start: new Date(2026, 7, 7),
                end: new Date(2027, 4, 22),
                season: '2026',
                current: true,
            },
            {
                leagueId: 145,
                countryCode: countryCode,
                name: 'Challenger Pro League',
                logo: 'https://media.api-sports.io/football/leagues/145.png',
                start: new Date(2026, 7, 14),
                end: new Date(2027, 4, 1),
                season: '2026',
                current: true,
            }
        ])
    }
}
