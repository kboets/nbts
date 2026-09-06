import {Injectable, signal, WritableSignal} from '@angular/core';
import type {Country} from '../shared/models/country';

@Injectable({
    providedIn: 'root',
})
export class CountryStore {
    /** Holds the current countries or null if not loaded */
    countries: WritableSignal<Country[] | null> = signal(null);
    /** Holds the currently selected countries or null if not loaded */
    selectedCountries: WritableSignal<Country[] | null> = signal(null);

    /** Loading flag for async fetches */
    loading: WritableSignal<boolean> = signal(false);

    /** Error message when a fetch fails */
    error: WritableSignal<string | null> = signal(null);

    startLoading() {
        this.error.set(null);
        this.loading.set(true);
    }

    setCountries(ls: Country[] | null) {
        this.countries.set(ls);
        this.loading.set(false);
        this.error.set(null);
    }

    setSelectedCountries(ls: Country[] | null) {
        this.selectedCountries.set(ls);
        this.loading.set(false);
        this.error.set(null);
    }

    setError(msg: string | null) {
        this.error.set(msg);
        this.loading.set(false);
    }

    clear() {
        this.countries.set(null);
        this.selectedCountries.set(null);
        this.error.set(null);
        this.loading.set(false);
    }
}

