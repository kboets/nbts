import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {TabsModule} from 'primeng/tabs';
import {TableModule} from 'primeng/table';
import {AutoCompleteCompleteEvent, AutoCompleteModule} from 'primeng/autocomplete';
import {TagModule} from 'primeng/tag';
import {DataViewModule} from 'primeng/dataview';
import {ButtonModule} from 'primeng/button';
import {ToastModule} from 'primeng/toast';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {PaginatorModule, PaginatorState} from 'primeng/paginator';
import {AccordionModule} from 'primeng/accordion';
import {SplitterModule} from 'primeng/splitter';
import {Country} from "../../shared/models/country";
import {League} from '../../shared/models/league';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {LeagueService} from "../../service/league.service";
import {LeagueStore} from "../../service/league.store";
import {ResultService} from '../../service/result.service';
import {StandingService} from "../../service/standing.service";


@Component({
    selector: 'app-results',
    templateUrl: './results.component.html',
    styleUrls: ['./results.components.scss'],
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, CommonModule, ToastModule, TableModule, ConfirmDialogModule, PaginatorModule, SplitterModule],
})
export class ResultsComponent implements OnInit {

    // data for the search box
    public selectedCountries = signal<Country[] | null>(null);
    filteredCountries: Country[] = [];
    selectedCountry: Country | null = null;

    // stores and services
    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    private leaguesService = inject(LeagueService);
    private leagueStore = inject(LeagueStore);
    private resultService = inject(ResultService);
    private standingService = inject(StandingService);

    // data for the selection of the league in the country tab
    public selectedLeagues = this.leaguesService.selectedLeagues;
    public selectedLeaguesError = this.leaguesService.selectedLeaguesError;

    // data for the result table
    public results4League = this.resultService.results;
    public results4LeagueError = this.resultService.resultsError;
    public selectedRound = signal<number | undefined>(undefined);

    // data for the standing table
    public standingData = this.standingService.standing;
    public standingDataError = this.standingService.standingError;
    public standingLastUpdated = computed(() => this.standingData()?.[0]?.lastUpdated);


    constructor() {
        this.countryService.getSelectedCountries().subscribe((countriesList: Country[]) => {
            this.selectedCountries.set(countriesList);
        });
        this.resultService.resetLeagueForResult();
        this.resultService.resetSeasonForResult();
        this.standingService.resetLeagueForStanding();
        this.standingService.resetSeasonForStanding();
    }

    ngOnInit(): void {
        this.countryService.getSelectedCountries().subscribe((countriesList: Country[]) => {
            this.selectedCountries.set(countriesList);
        });
        this.resultService.resetLeagueForResult();
        this.resultService.resetSeasonForResult();
        this.standingService.resetLeagueForStanding();
        this.standingService.resetSeasonForStanding();
        }

    // create signal for the standing
    public hasStanding4Country = computed(() =>
        (this.standingData()?.length ?? 0) > 0 &&
        !this.standingDataError()
    );

    // create signal for the results
    public hasResults4League = computed(() =>
        (this.results4League()?.length ?? 0) > 0 &&
        !this.results4LeagueError()
    );

    // data for the round selection
    public availableRounds = computed(() => {
        const results = this.results4League();

        if ((results?.length ?? 0) === 0 || this.results4LeagueError()) {
            return [];
        }

        return [...new Set(results.map((result) => result.round))]
            .sort((left, right) => left - right);
    });

    public currentRound = computed(() => this.results4League()
        .find((result) => result.isCurrent)?.round);

    public visibleRound = computed(() => {
        const rounds = this.availableRounds();
        const selectedRound = this.selectedRound();

        if (rounds.length === 0) {
            return undefined;
        }

        if (selectedRound !== undefined && rounds.includes(selectedRound)) {
            return selectedRound;
        }

        return this.currentRound() ?? rounds[0];
    });

    public selectedRoundPage = computed(() => {
        const visibleRound = this.visibleRound();
        const page = this.availableRounds().findIndex((round) => round === visibleRound);

        return page >= 0 ? page : 0;
    });

    public visibleRoundResults = computed(() => {
        const results = this.results4League();
        const visibleRound = this.visibleRound();

        if ((results?.length ?? 0) === 0 || this.results4LeagueError() || visibleRound === undefined) {
            return [];
        }

        return results
            .filter((result) => result.round === visibleRound)
            .sort((left, right) => new Date(left.matchDate).getTime() - new Date(right.matchDate).getTime());
    });


    onSelectCountry(event: any) {
        const selected = event?.value || this.selectedCountry;
        if (selected) {
            this.selectedCountries.set([selected]);
        }
    }

    onCountryUnselect() {
        this.selectedCountry = null;
        this.selectedCountries.set(this.countryStore.selectedCountries());
    }

    filterCountry(event: AutoCompleteCompleteEvent) {
        const query = (event.query || '').toLowerCase();
        const countryList = this.countryStore.selectedCountries() || [];

        this.filteredCountries = countryList.filter(country =>
            country.nameNL?.toLowerCase().includes(query) ||
            country.nameEN?.toLowerCase().includes(query)
        );
    }

    onCountryOpenTab(event: any) {
        //console.log('onCountryOpenTab', event);
        this.leaguesService.resetCountryForSelectedLeagues();
        this.leaguesService.selectCountryForSelectedLeagues(event);

    }

    selectLeague(league: League) {
        this.selectedRound.set(undefined);
        this.resultService.selectLeagueForResult(league.leagueId);
        this.resultService.selectSeasonForResult(league.season);
        this.standingService.selectLeagueForStanding(league.leagueId);
        this.standingService.selectSeasonForStanding(league.season);
    }

    selectAnotherLeague() {
        this.selectedRound.set(undefined);
        this.resultService.resetLeagueForResult();
        this.resultService.resetSeasonForResult();
        this.standingService.resetLeagueForStanding();
        this.standingService.resetSeasonForStanding();
    }

    onRoundPageChange(event: PaginatorState) {
        this.selectedRound.set(this.availableRounds()[event.page ?? 0]);
    }
}
