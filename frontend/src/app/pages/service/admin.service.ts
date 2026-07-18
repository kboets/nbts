import {computed, inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {HttpErrorService} from './http-error.service';
import type {Version} from '../shared/models/version';
import type {Result} from '../shared/models/result';
import {toSignal} from "@angular/core/rxjs-interop";

import {catchError, map, Observable, of, tap} from 'rxjs';


@Injectable({
    providedIn: 'root',
})
export class AdminService {

    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    private versionResult$ = this.getVersion().pipe(
        tap((response) => console.log('maven version:', response.mavenVersion)),
        map((response) => ({ data: response.mavenVersion }) as Result<string>),
        catchError((error) =>
            of({
                data: undefined,
                error: this.errorService.formatError(error),
            } as Result<string>),
        ),
    );

    private versionResult = toSignal(this.versionResult$, { initialValue: { data: '', error: undefined } });
    version = computed(() => this.versionResult()?.data);
    versionError = computed(() => this.versionResult()?.error);

    private getVersion(): Observable<Version> {
        return this.http.get<Version>(`${this.baseUrl}/currentVersion`);
    }
}
