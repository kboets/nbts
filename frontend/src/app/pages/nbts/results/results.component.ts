import {Component, inject, OnInit, signal} from '@angular/core';
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
import {AccordionModule} from 'primeng/accordion';
import {Country} from "../../shared/models/country";
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {LeagueService} from "../../service/league.service";
import {LeagueStore} from "../../service/league.store";


@Component({
    selector: 'app-results',
    templateUrl: './results.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, CommonModule, ToastModule, TableModule, ConfirmDialogModule],
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

    // data for the table
    public selectedLeagues = this.leaguesService.selectedLeagues;

    constructor() {
        this.countryService.getSelectedCountries().subscribe((countriesList: Country[]) => {
            this.selectedCountries.set(countriesList);
        });
    }

    ngOnInit(): void {
        this.countryService.getSelectedCountries().subscribe((countriesList: Country[]) => {
            this.selectedCountries.set(countriesList);
        });

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

    filterCountry(event: AutoCompleteCompleteEvent) {
        const query = (event.query || '').toLowerCase();
        const countryList = this.countryStore.selectedCountries() || [];

        this.filteredCountries = countryList.filter(country =>
            country.nameNL?.toLowerCase().includes(query) ||
            country.nameEN?.toLowerCase().includes(query)
        );
    }

    onCountryOpenTab(event: any) {
        console.log('onCountryOpenTab', event);
        this.leaguesService.resetCountryForSelectedLeagues();
        this.leaguesService.selectCountryForSelectedLeagues(event);
    }
}
