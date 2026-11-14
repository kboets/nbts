import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TabsModule } from 'primeng/tabs';
import { TableModule } from 'primeng/table';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { TagModule } from 'primeng/tag';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { AccordionModule } from 'primeng/accordion';
import { CountryStore } from '../../service/country.store';
import { CountryService } from '../../service/country.service';
import { LeagueStore } from '../../service/league.store';
import { LeagueService } from '../../service/league.service';
import { ForecastService } from '../../service/forecast.service';
import { Country } from '@/app/pages/shared/models/country';
import { ForecastDetail } from '../../shared/models/forecastDetail';

@Component({
    selector: 'app-forecasts',
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, CommonModule, ToastModule, TableModule],
    providers: [MessageService, ConfirmationService],
    templateUrl: './forecasts.component.html'
})
export class ForecastsComponent implements OnInit {
    // data for the accordion tabs
    public selectedCountries = signal<Country[] | null>(null);
    selectedCountry: Country | null = null;

    // stores and services
    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    private leagueStore = inject(LeagueStore);
    private leaguesService = inject(LeagueService);
    private forecastService = inject(ForecastService);

    // data for the selection of the league in the country tab
    public selectedLeagues = this.leaguesService.selectedLeagues;
    public selectedLeaguesError = this.leaguesService.selectedLeaguesError;

    // data for the selection of the round in the league tab
    public forecastsForLeagueAndRound = this.forecastService.forecasts;
    public forecastsForLeagueAndRoundError = this.forecastService.forecastsError;

    constructor() {
        this.initForecastData();
    }

    onCountryOpenTab(event: any) {
        console.log('Country tab opened:', event);
    }

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

    ngOnInit(): void {
        this.initForecastData();
    }

    public getNextMatchDisplay(detail: ForecastDetail): string {
        if (!detail) {
            return '';
        }
        return detail.nextMatchHomeTeam === detail.team ? `${detail.team} - ${detail.opponent}` : `${detail.opponent} - ${detail.team}`;
    }

    private initForecastData() {
        this.countryService.getSelectedCountries().subscribe((countriesList: Country[]) => {
            this.selectedCountries.set(countriesList);
        });
        this.forecastService.selectLeagueForForecast(144);
        this.forecastService.selectRoundForForecast(12);
        this.forecastService.selectSeasonForForecast(2026);
    }
}
