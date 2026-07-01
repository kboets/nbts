import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule, NgClass} from '@angular/common';
import {TabsModule} from 'primeng/tabs';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {LeagueService} from '../../service/league.service';
import {AccordionModule} from 'primeng/accordion';
import {AutoCompleteCompleteEvent, AutoCompleteModule} from 'primeng/autocomplete';
import {TagModule} from 'primeng/tag';
import {DataViewModule} from 'primeng/dataview';
import {ButtonModule} from 'primeng/button';

import {Country} from '../../shared/models/country';

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, NgClass, CommonModule]
})
export class LeaguesComponent implements OnInit {

    // inject stores and services
    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    private leaguesService = inject(LeagueService);

    public countries = signal<Country[] | null>(null);
    filteredCountries: Country[] = [];
    selectedCountry: Country | null = null;

    // get signals
    newLeagues = this.leaguesService.newLeagues;
    newLeaguesError = this.leaguesService.newLeaguesError;

    constructor() {
        this.countryService.getCountries().then((countriesList) => {
            this.countries.set(countriesList);
        });
    }

    selectNewLeague(league: any) {
        console.log('league selected', league);
    }

    ngOnInit(): void {
        this.countryService.getCountries();
    }

    onNewLeaguesCountryOpenTab(event: any) {
        console.log('new tab openeded with countryCode ', event);
        this.leaguesService.selectCountryForNewLeagues(event);
    }

    onSelectCountry(event: any) {
        const selected = event?.value || this.selectedCountry;
        if (selected) {
            this.countries.set([selected]);
        }
    }

    onCountryUnselect() {
        this.selectedCountry = null;
        this.countries.set(this.countryStore.countries());
    }

    filterCountry(event: AutoCompleteCompleteEvent) {
        const query = (event.query || '').toLowerCase();
        const countryList = this.countryStore.countries() || [];

        this.filteredCountries = countryList.filter(country =>
            country.nameNL?.toLowerCase().includes(query) ||
            country.nameEN?.toLowerCase().includes(query)
        );
    }

}
