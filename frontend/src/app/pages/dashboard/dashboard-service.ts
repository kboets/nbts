import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import type {League} from '../shared/models/league';
import {LeagueService} from '../service/league.service';

@Injectable({
    providedIn: 'root',
})
export class DashboardService {

    private leagueService = inject(LeagueService);

    constructor() { }

    getCurrentLeagues(): Observable<League[]> {
        return this.leagueService.getCurrentLeagues();
    }
}
