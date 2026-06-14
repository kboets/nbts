import { Component, inject, OnInit } from '@angular/core';
import { NotificationsWidget } from './components/notificationswidget';
import { StatsWidget } from './components/statswidget';
import { RecentSalesWidget } from './components/recentsaleswidget';
import { BestSellingWidget } from './components/bestsellingwidget';
import { RevenueStreamWidget } from './components/revenuestreamwidget';
import { DashboardService } from './dashboard-service';
import { LeagueStore } from '../service/league.store';
import type { League } from '../shared/models/league';
@Component({
    selector: 'app-dashboard',
    imports: [StatsWidget, RecentSalesWidget, BestSellingWidget, RevenueStreamWidget, NotificationsWidget],
    template: `
        <div class="grid grid-cols-12 gap-8">
            <app-stats-widget class="contents" />
            <div class="col-span-12 xl:col-span-6">
                <app-recent-sales-widget />
                <app-best-selling-widget />
            </div>
            <div class="col-span-12 xl:col-span-6">
                <app-revenue-stream-widget />
                <app-notifications-widget />
            </div>
        </div>
    `
})
export class Dashboard implements OnInit {

    private dashboardService = inject(DashboardService);
    private leagueStore = inject(LeagueStore);

    ngOnInit() {
        this.leagueStore.startLoading();
        this.dashboardService.getCurrentLeagues().subscribe({
            next: (leagues: League[]) => this.leagueStore.setLeagues(leagues),
            error: (err: any) => {
                const msg = err?.message || 'Failed to load leagues';
                this.leagueStore.setError(msg);
            },
        });
    }
}
