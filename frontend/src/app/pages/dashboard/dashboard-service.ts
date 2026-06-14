import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { League } from '../shared/models/league';

@Injectable({
    providedIn: 'root',
})
export class DashboardService {

    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);

    constructor() { }

    getCurrentLeagues(): Observable<League[]> {
        return this.http.get<League[]>(`${this.baseUrl}/currentLeagues`);
    }
}
