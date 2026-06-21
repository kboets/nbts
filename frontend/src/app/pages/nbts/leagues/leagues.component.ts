import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TabsModule} from 'primeng/tabs';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {AccordionModule} from 'primeng/accordion';
import {AutoCompleteCompleteEvent, AutoCompleteModule} from 'primeng/autocomplete';
import {Country} from '../../shared/models/country';

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule]
})
export class LeaguesComponent implements OnInit {

    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    public countries = signal<Country[] | null>(null);
    filteredCountries: Country[] = [];
    selectedCountry: Country | null = null;

    constructor() {
        this.countryService.getCountries().then((countriesList) => {
            this.countries.set(countriesList);
        });
    }

    ngOnInit(): void {
        this.countryService.getCountries();
    }

    onNewLeaguesCountryOpenTab(event: any) {
        console.log(event);
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
