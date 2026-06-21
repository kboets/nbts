import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CountryStore} from './country.store';
import type {Country} from '../shared/models/country';

@Injectable({
    providedIn: 'root'
})
export class CountryService {

    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private countryStore = inject(CountryStore);

    constructor() { }


    getData(): Country[] {
        return [
            { countryId: 43, nameNL: 'België', nameEN: 'Belgium', countryCode: 'BE', region: 'Europe', flagUrl: 'https://flagcdn.com/w20/be.png' },
            { countryId: 20, nameNL: 'Duitsland', nameEN: 'Germany', countryCode: 'DE', region: 'Europe', flagUrl: 'https://flagcdn.com/w20/de.png' },
            { countryId: 6, nameNL: 'Frankrijk', nameEN: 'France', countryCode: 'FR', region: 'Europe', flagUrl: 'https://flagcdn.com/w20/fr.png' },
            { countryId: 7, nameNL: 'Nederland', nameEN: 'Netherlands', countryCode: 'NL', region: 'Europe', flagUrl: 'https://flagcdn.com/w20/nl.png' },
            { countryId: 22, nameNL: 'Italië', nameEN: 'Italy', countryCode: 'IT', region: 'Europe', flagUrl: 'https://flagcdn.com/w20/it.png' }
        ];
    }

    getCountries(): Promise<Country[]> {
        this.countryStore.startLoading();
        return new Promise((resolve) => {
            // Simulating async call
            setTimeout(() => {
                const countries = this.getData();
                this.countryStore.setCountries(countries);
                resolve(countries);
            }, 500);
        });
    }
}
