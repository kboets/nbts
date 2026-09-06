import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CountryStore} from './country.store';
import type {Country} from '../shared/models/country';
import {catchError, Observable, of, tap} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class CountryService {

    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private countryStore = inject(CountryStore);

    constructor() { }

    getCountries(): Observable<Country[]> {
        this.countryStore.startLoading();
        return this.http.get<Country[]>(`${this.baseUrl}/countries`).pipe(
            tap((countries) => {
                this.countryStore.setCountries(countries);
            }),
            catchError((error) => {
                this.countryStore.setCountries([]);
                this.countryStore.setError(`Failed to load countries: ${error.message}`);
                return of([]);
            })
        );
    }

    getSelectedCountries(): Observable<Country[]> {
        this.countryStore.startLoading();
        return this.http.get<Country[]>(`${this.baseUrl}/countries/selected`).pipe(
            tap((countries) => {
                this.countryStore.setSelectedCountries(countries);
            }),
            catchError((error) => {
                this.countryStore.setSelectedCountries([]);
                this.countryStore.setError(`Failed to load selected countries: ${error.message}`);
                return of([]);
            })
        )
    }
}
