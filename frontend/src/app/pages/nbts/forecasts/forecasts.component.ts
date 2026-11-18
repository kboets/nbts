import {Component, computed, effect, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ConfirmationService, MessageService, TreeNode} from 'primeng/api';
import {TabsModule} from 'primeng/tabs';
import {TableModule} from 'primeng/table';
import {AutoCompleteModule} from 'primeng/autocomplete';
import {TagModule} from 'primeng/tag';
import {DataViewModule} from 'primeng/dataview';
import {ButtonModule} from 'primeng/button';
import {ToastModule} from 'primeng/toast';
import {TreeTableModule} from 'primeng/treetable';
import {LeagueService} from '../../service/league.service';
import {ForecastService} from '../../service/forecast.service';
import {League} from '../../shared/models/league';
import {ForecastDetail} from '../../shared/models/forecastDetail';

interface ForecastTreeRow {
    league: League | null;
    name: string;
    round: number | null;
    team: string;
    nextMatchHomeTeam: string;
    opponent: string;
    nextMatchDate: string;
    teamScore: number | null;
    opponentRankScore: number | null;
    totalPoints: number | null;
}

@Component({
    selector: 'app-forecasts',
    standalone: true,
    imports: [TabsModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, CommonModule, ToastModule, TableModule, TreeTableModule],
    providers: [MessageService, ConfirmationService],
    templateUrl: './forecasts.component.html'
})
export class ForecastsComponent implements OnInit {

    // services
    private leaguesService = inject(LeagueService);
    private forecastService = inject(ForecastService);

    // persisted current leagues
    public selectedLeagues = signal<League[]>([]);
    public selectedLeaguesError = signal<string | null>(null);

    // data for the forecast in each round in the league tab
    public selectedRound = signal<number | undefined>(undefined);
    public forecastsForLeagueAndRound = this.forecastService.forecasts;
    public forecastsForLeagueAndRoundError = this.forecastService.forecastsError;
    private forecastRoundByLeague = signal<Record<string, number>>({});
    private forecastDetailsByLeague = signal<Record<string, ForecastDetail[]>>({});
    private forecastLoading = signal<Set<string>>(new Set());
    private forecastErrors = signal<Record<string, string>>({});
    private collapsedForecastKeys = signal<Set<string>>(new Set());
    private requestedLeagueKeys = new Set<string>();

    // data for the tree map of the leagues in the country tab
    public treeMapForecastData = computed<TreeNode<ForecastTreeRow>[]>(() => {
        const detailsByLeague = this.forecastDetailsByLeague();
        const roundsByLeague = this.forecastRoundByLeague();
        const loading = this.forecastLoading();
        const errors = this.forecastErrors();
        const collapsedKeys = this.collapsedForecastKeys();

        return (this.selectedLeagues() ?? []).map(league => {
            const key = this.getLeagueKey(league);
            const details = detailsByLeague[key];
            let children: TreeNode<ForecastTreeRow>[] | undefined;

            if (loading.has(key)) {
                children = [this.createMessageNode(key, 'Forecasts laden...')];
            } else if (errors[key]) {
                children = [this.createMessageNode(key, errors[key])];
            } else if (details) {
                children = details.map((detail, index) => ({
                    key: `${key}-${index}`,
                    data: {
                        league: null,
                        name: '',
                        round: null,
                        team: detail.team,
                        nextMatchHomeTeam: detail.nextMatchHomeTeam,
                        opponent: detail.opponent,
                        nextMatchDate: detail.nextMatchDate,
                        teamScore: detail.teamScore ?? null,
                        opponentRankScore: detail.opponentRankScore ?? null,
                        totalPoints: detail.totalPoints
                    },
                    leaf: true
                }));
            }

            return {
                key,
                data: {
                    league,
                    name: league.name,
                    round: roundsByLeague[key] ?? null,
                    team: '',
                    nextMatchHomeTeam: '',
                    opponent: '',
                    nextMatchDate: '',
                    teamScore: null,
                    opponentRankScore: null,
                    totalPoints: null
                },
                children,
                leaf: details !== undefined && details.length === 0,
                expanded: !collapsedKeys.has(key)
            };
        }).filter(node => !!node.children?.length);
    });
    public allForecastsExpanded = computed(() => {
        const nodes = this.treeMapForecastData();
        return nodes.length > 0 && nodes.every(node => node.expanded === true);
    });

    constructor() {
        effect(() => {
            for (const league of this.selectedLeagues() ?? []) {
                this.loadForecastsForLeague(league);
            }
        });
    }

    ngOnInit(): void {
        this.leaguesService.getCurrentLeagues().subscribe({
            next: leagues => this.selectedLeagues.set(leagues),
            error: error => this.selectedLeaguesError.set(`Could not load leagues: ${error.message ?? error}`)
        });
    }

    toggleAllForecasts() {
        if (this.allForecastsExpanded()) {
            this.collapsedForecastKeys.set(new Set(this.selectedLeagues().map(league => this.getLeagueKey(league))));
        } else {
            this.collapsedForecastKeys.set(new Set());
        }
    }

    toggleForecasts(node: TreeNode<ForecastTreeRow>) {
        const key = node.key;
        if (!key) {
            return;
        }
        this.collapsedForecastKeys.update(keys => {
            const next = new Set(keys);
            if (next.has(key)) {
                next.delete(key);
            } else {
                next.add(key);
            }
            return next;
        });
    }

    private loadForecastsForLeague(league: League) {
        const key = this.getLeagueKey(league);
        if (this.requestedLeagueKeys.has(key)) {
            return;
        }

        this.requestedLeagueKeys.add(key);
        this.forecastLoading.update(keys => new Set(keys).add(key));
        const season = Number(league.season);

        this.forecastService.getCurrentRound(league.leagueId, season).subscribe({
            next: currentRound => {
                const nextRound = currentRound + 1;
                this.forecastRoundByLeague.update(rounds => ({...rounds, [key]: nextRound}));
                this.forecastService.selectLeagueForForecast(league.leagueId);
                this.forecastService.selectRoundForForecast(nextRound);
                this.forecastService.selectSeasonForForecast(season);
                this.selectedRound.set(nextRound);

                this.forecastService.getForecasts(league.leagueId, nextRound, season).subscribe({
                    next: forecasts => {
                        const details = forecasts
                            .flatMap(forecast => forecast.forecastDetails)
                            .sort((first, second) => second.totalPoints - first.totalPoints);
                        this.forecastDetailsByLeague.update(allDetails => ({...allDetails, [key]: details}));
                        this.finishForecastRequest(key);
                    },
                    error: error => {
                        this.failForecastRequest(key, `Could not load forecasts: ${error.message ?? error}`);
                    }
                });
            },
            error: error => {
                this.failForecastRequest(key, `Could not load current round: ${error.message ?? error}`);
            }
        });
    }

    private finishForecastRequest(key: string) {
        this.forecastLoading.update(keys => {
            const next = new Set(keys);
            next.delete(key);
            return next;
        });
    }

    private failForecastRequest(key: string, message: string) {
        this.finishForecastRequest(key);
        this.requestedLeagueKeys.delete(key);
        this.forecastErrors.update(errors => ({...errors, [key]: message}));
    }

    private getLeagueKey(league: League): string {
        return `${league.leagueId}-${league.season}`;
    }

    private createMessageNode(key: string, message: string): TreeNode<ForecastTreeRow> {
        return {
            key: `${key}-message`,
            data: {
                league: null,
                name: message,
                round: null,
                team: '',
                nextMatchHomeTeam: '',
                opponent: '',
                nextMatchDate: '',
                teamScore: null,
                opponentRankScore: null,
                totalPoints: null
            },
            leaf: true
        };
    }
}
