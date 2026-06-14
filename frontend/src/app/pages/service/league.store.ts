import { Injectable, signal, WritableSignal } from '@angular/core';
import type { League } from '../shared/models/league';

@Injectable({
    providedIn: 'root',
})
export class LeagueStore {
    /** Holds the current leagues or null if not loaded */
    leagues: WritableSignal<League[] | null> = signal(null);

    /** Loading flag for async fetches */
    loading: WritableSignal<boolean> = signal(false);

    /** Error message when a fetch fails */
    error: WritableSignal<string | null> = signal(null);

    startLoading() {
        this.error.set(null);
        this.loading.set(true);
    }

    setLeagues(ls: League[] | null) {
        this.leagues.set(ls);
        this.loading.set(false);
        this.error.set(null);
    }

    setError(msg: string | null) {
        this.error.set(msg);
        this.loading.set(false);
    }

    clear() {
        this.leagues.set(null);
        this.error.set(null);
        this.loading.set(false);
    }
}

