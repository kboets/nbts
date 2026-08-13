import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LeagueStore} from '../../service/league.store';
import {AdminService} from '../../service/admin.service';

@Component({
    standalone: true,
    selector: 'app-stats-widget',
    imports: [CommonModule],
    template: `<div class="col-span-12 lg:col-span-6 xl:col-span-3">
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">Geselecteerde competities</span>
                        <ng-container *ngIf="leagueStore.loading(); else notLoading">
                            <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">Loading…</div>
                        </ng-container>
                        <ng-template #notLoading>
                            <div *ngIf="leagueStore.error() as err" class="text-surface-900 dark:text-surface-0 font-medium text-sm text-red-600">{{ err }}</div>
                            <div *ngIf="!leagueStore.error()" class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ (leagueStore.selectedLeagues() || []).length }}</div>
                        </ng-template>
                    </div>
                    <div class="flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                        <i class="fa-solid fa-baseball text-xl!"></i>
                    </div>
                </div>
<!--                <span class="text-primary font-medium">24 new </span>-->
<!--                <span class="text-muted-color">since last visit</span>-->
            </div>
        </div>
        <div class="col-span-12 lg:col-span-6 xl:col-span-3">
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">Gemaakte rapidApi calls:</span>
                        <div *ngIf="adminService.apiCounterError() as err" class="text-surface-900 dark:text-surface-0 font-medium text-sm text-red-600">{{ err }}</div>
                        <div *ngIf="!adminService.apiCounterError()" class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ adminService.apiCounter() }}</div>
                    </div>
                    <div class="flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                        <i class="pi pi-dollar text-orange-500 text-xl!"></i>
                    </div>
                </div>
                <span [ngClass]="getApiCallsLeftClass()" class="font-medium">{{ getApiCallsLeft() }} </span>
                <span class="text-muted-color">over</span>
            </div>
        </div>
        <div class="col-span-12 lg:col-span-6 xl:col-span-3">
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">Customers</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">28441</div>
                    </div>
                    <div class="flex items-center justify-center bg-cyan-100 dark:bg-cyan-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                        <i class="pi pi-users text-cyan-500 text-xl!">{{ getApiCallsLeft() }}</i>
                    </div>
                </div>
                <span class="text-primary font-medium">{{ getApiCallsLeft() }} </span>
                <span class="text-muted-color">newly registered</span>
            </div>
        </div>
        <div class="col-span-12 lg:col-span-6 xl:col-span-3">
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">Comments</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">152 Unread</div>
                    </div>
                    <div class="flex items-center justify-center bg-purple-100 dark:bg-purple-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                        <i class="pi pi-comment text-purple-500 text-xl!"></i>
                    </div>
                </div>
                <span class="text-primary font-medium">85 </span>
                <span class="text-muted-color">responded</span>
            </div>
        </div>`
})
export class StatsWidget {
    public leagueStore: LeagueStore = inject(LeagueStore);
    public adminService: AdminService = inject(AdminService);
    private max = 100;


    getApiCallsLeft(): number {
        if (this.adminService.apiCounterError()) {
            return 100;
        }
        const counter = this.adminService.apiCounter();
        // check if counter is a number and not undefined

        if (counter === undefined) {
            console.error('Invalid counter value:', counter);
            return 100;
        }
        return this.max - counter;
    }

    getApiCallsLeftClass(): string {
        if (this.adminService.apiCounterError()) {
            return 'text-primary';
        }

        const counter = this.adminService.apiCounter();

        if (counter === undefined) {
            return 'text-primary';
        }

        if (counter > 90) {
            return 'text-red-500';
        }

        if (counter > 80) {
            return 'text-orange-500';
        }

        return 'text-green-500';
    }
}
