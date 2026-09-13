import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {HttpErrorService} from './http-error.service';
import type {Version} from '../shared/models/version';
import type {ApiCounter} from '../shared/models/apiCounter';
import type {Result} from '../shared/models/result';
import {toObservable, toSignal} from "@angular/core/rxjs-interop";

import {catchError, map, Observable, of, shareReplay, switchMap} from 'rxjs';


@Injectable({
    providedIn: 'root',
})
export class AdminService {

    private baseUrl = '/nbts/api';
    private http = inject(HttpClient);
    private errorService = inject(HttpErrorService);

    private versionResult$ = this.getVersion().pipe(
        //tap((response) => console.log('maven version:', response.mavenVersion)),
        map((response) => ({ data: response.mavenVersion }) as Result<string>),
        catchError((error) =>
            of({
                data: undefined,
                error: this.errorService.formatError(error),
            } as Result<string>),
        ),
    );

    private versionResult = toSignal(this.versionResult$, { initialValue: { data: '', error: undefined } });
    readonly version = computed(() => this.versionResult().data ?? '');
    readonly versionError = computed(() => this.versionResult().error);

    private getVersion(): Observable<Version> {
        return this.http.get<Version>(`${this.baseUrl}/currentVersion`);
    }

    /**  API COUNTER **/
    private apiCounterRefresh = signal(0);
    private apiCounterResult$ = toObservable(this.apiCounterRefresh).pipe(
        switchMap(() =>
            this.getApiCounter().pipe(
                map((response) => ({ data: response.counter } as Result<number>)),
                catchError((error) =>
                    of({
                        data: undefined,
                        error: this.errorService.formatError(error),
                    } as Result<number>),
                ),
            ),
        ),
        shareReplay(1),
    );

    private apiCounterResult = toSignal(this.apiCounterResult$, { initialValue: { data: 0, error: undefined } });
    readonly apiCounter = computed(() => this.apiCounterResult().data ?? 0);
    readonly apiCounterError = computed(() => this.apiCounterResult().error);

    refreshApiCounter() {
        this.apiCounterRefresh.update((value) => value + 1);
    }

    private getApiCounter(): Observable<ApiCounter> {
        return this.http.get<ApiCounter>(`${this.baseUrl}/apiCounter`);
    }
}
