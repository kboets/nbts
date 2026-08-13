import {Injectable, signal, WritableSignal} from '@angular/core';
import type {League} from '../shared/models/league';

@Injectable({
    providedIn: 'root',
})
export class LeagueStore {
    /** Holds the current leagues or null if not loaded */
    selectedLeagues: WritableSignal<League[] | null> = signal(null);

    /** Loading flag for async fetches */
    loading: WritableSignal<boolean> = signal(false);

    /** Error message when a fetch fails */
    error: WritableSignal<string | null> = signal(null);

    startLoading() {
        this.error.set(null);
        this.loading.set(true);
    }

    setLeagues(ls: League[] | null) {
        this.selectedLeagues.set(ls);
        this.loading.set(false);
        this.error.set(null);
    }

    removeLeague(leagueId: number) {
        const currentLeagues = this.selectedLeagues();
        if (currentLeagues) {
            // Adjust 'leagueId' below depending on the exact property name in your League interface (e.g. leagueId, id, etc.)
            this.selectedLeagues.set(currentLeagues.filter(l => l.leagueId !== leagueId));
        }
    }

    setError(msg: string | null) {
        this.error.set(msg);
        this.loading.set(false);
    }

    clear() {
        this.selectedLeagues.set(null);
        this.error.set(null);
        this.loading.set(false);
    }
}

