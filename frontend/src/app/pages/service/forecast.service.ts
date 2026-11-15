import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpErrorService } from './http-error.service';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, switchMap } from 'rxjs/operators';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { Forecast } from '../shared/models/forecast';
import { ForecastDetail } from '../shared/models/forecastDetail';
import { Result } from '../shared/models/result';

@Injectable({
    providedIn: 'root'
})
export class ForecastService {
    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    constructor() {}

    /**  retrieve the forecasts for a specific league,round and season */
    leagueForForecast = signal<number | undefined>(undefined);
    roundForForecast = signal<number | undefined>(undefined);
    seasonForForecast = signal<number | undefined>(undefined);

    public selectLeagueForForecast(leagueId: number) {
        this.leagueForForecast.set(leagueId);
    }
    public resetLeagueForForecast() {
        this.leagueForForecast.set(undefined);
    }
    public selectRoundForForecast(round: number) {
        this.roundForForecast.set(round);
    }
    public resetRoundForForecast() {
        this.roundForForecast.set(undefined);
    }
    public selectSeasonForForecast(season: number) {
        this.seasonForForecast.set(season);
    }
    public resetSeasonForForecast() {
        this.seasonForForecast.set(undefined);
    }

    forecastsByLeagueAndSeason$ = combineLatest([toObservable(this.leagueForForecast), toObservable(this.seasonForForecast), toObservable(this.roundForForecast)]).pipe(
        distinctUntilChanged(([previousLeagueId, previousSeason, previousRound], [leagueId, season, round]) => previousLeagueId === leagueId && previousSeason === season && previousRound === round),
        switchMap(([leagueId, season, round]) => {
            if (leagueId === undefined || season === undefined || round === undefined) {
                return of({ data: [], error: undefined } as Result<Forecast[]>);
            }

            return this.getDummyForecast4LeagueAndRound(leagueId, round, season).pipe(
                map((results) => ({ data: results }) as Result<Forecast[]>),
                catchError((error) =>
                    of({
                        data: [],
                        error: this.errorService.formatError(error)
                    } as Result<Forecast[]>)
                )
            );
        }),
        //tap((result) => console.log('getForecasts', result)),
        shareReplay(1)
    );

    private selectedForecastsResult = toSignal(this.forecastsByLeagueAndSeason$, {
        initialValue: { data: [], error: undefined } as Result<Forecast[]>
    });

    forecasts = computed(() => this.selectedForecastsResult().data ?? []);
    forecastsError = computed(() => this.selectedForecastsResult().error);

    /**  retrieve the forecasts for a specific league,round and season */
    getForecasts(leagueId: number, round: number, season: number): Observable<Forecast[]> {
        return this.http.get<Forecast[]>(`${this.baseUrl}/forecasts/${leagueId}/${round}/${season}`);
    }

    getCurrentRound(leagueId: number, season: number): Observable<number> {
        return this.http.get<number>(`${this.baseUrl}/currentRound/${leagueId}/${season}`);
    }

    private getDummyForecast4LeagueAndRound(leagueId: number, round: number, season: number): Observable<Forecast[]> {
        console.log('getDummyForecast4LeagueAndRound', leagueId, round, season);
        return of([
            {
                leagueName: 'Jupiler Pro League',
                countryCode: 'BE',
                round: 12,
                forecastDetails: [
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'KV Mechelen',
                        team: 'KV Mechelen',
                        opponent: 'Club Brugge KV',
                        teamScore: 95,
                        opponentRankScore: 14,
                        totalPoints: 109
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'SK Beveren',
                        team: 'SK Beveren',
                        opponent: 'Gent',
                        teamScore: -40,
                        opponentRankScore: -18,
                        totalPoints: -58
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'SK Beveren',
                        team: 'Gent',
                        opponent: 'SK Beveren',
                        teamScore: 105,
                        opponentRankScore: 32,
                        totalPoints: 137
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Lommel United',
                        team: 'Standard Liege',
                        opponent: 'Lommel United',
                        teamScore: -20,
                        opponentRankScore: -20,
                        totalPoints: -40
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Kortrijk',
                        team: 'Union St. Gilloise',
                        opponent: 'Kortrijk',
                        teamScore: 185,
                        opponentRankScore: 43,
                        totalPoints: 228
                    },
                    {
                        nextMatchDate: '2026-11-20',
                        nextMatchHomeTeam: 'Cercle Brugge',
                        team: 'Cercle Brugge',
                        opponent: 'KVC Westerlo',
                        teamScore: 50,
                        opponentRankScore: -7,
                        totalPoints: 43
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Anderlecht',
                        team: 'Anderlecht',
                        opponent: 'OH Leuven',
                        teamScore: 110,
                        opponentRankScore: 39,
                        totalPoints: 149
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Kortrijk',
                        team: 'Kortrijk',
                        opponent: 'Union St. Gilloise',
                        teamScore: -20,
                        opponentRankScore: -22,
                        totalPoints: -42
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Charleroi',
                        team: 'Charleroi',
                        opponent: 'St. Truiden',
                        teamScore: 135,
                        opponentRankScore: 8,
                        totalPoints: 143
                    },
                    {
                        nextMatchDate: '2026-11-20',
                        nextMatchHomeTeam: 'Cercle Brugge',
                        team: 'KVC Westerlo',
                        opponent: 'Cercle Brugge',
                        teamScore: 30,
                        opponentRankScore: 12,
                        totalPoints: 42
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Zulte Waregem',
                        team: 'Zulte Waregem',
                        opponent: 'Antwerp',
                        teamScore: -95,
                        opponentRankScore: -43,
                        totalPoints: -138
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Zulte Waregem',
                        team: 'Antwerp',
                        opponent: 'Zulte Waregem',
                        teamScore: -30,
                        opponentRankScore: -2,
                        totalPoints: -32
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Lommel United',
                        team: 'Lommel United',
                        opponent: 'Standard Liege',
                        teamScore: 95,
                        opponentRankScore: 36,
                        totalPoints: 131
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Genk',
                        team: 'Genk',
                        opponent: 'RAAL La Louvière',
                        teamScore: 40,
                        opponentRankScore: 11,
                        totalPoints: 51
                    },
                    {
                        nextMatchDate: '2026-11-22',
                        nextMatchHomeTeam: 'Anderlecht',
                        team: 'OH Leuven',
                        opponent: 'Anderlecht',
                        teamScore: 110,
                        opponentRankScore: 28,
                        totalPoints: 138
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Charleroi',
                        team: 'St. Truiden',
                        opponent: 'Charleroi',
                        teamScore: -45,
                        opponentRankScore: -43,
                        totalPoints: -88
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'Genk',
                        team: 'RAAL La Louvière',
                        opponent: 'Genk',
                        teamScore: -65,
                        opponentRankScore: -40,
                        totalPoints: -105
                    },
                    {
                        nextMatchDate: '2026-11-21',
                        nextMatchHomeTeam: 'KV Mechelen',
                        team: 'Club Brugge KV',
                        opponent: 'KV Mechelen',
                        teamScore: 165,
                        opponentRankScore: 47,
                        totalPoints: 212
                    }
                ]
            }
        ]);
    }
}
