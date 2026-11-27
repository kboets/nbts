import {provideZonelessChangeDetection, signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {ForecastsComponent} from './forecasts.component';
import {ForecastService} from '../../service/forecast.service';
import {LeagueService} from '../../service/league.service';

describe('ForecastsComponent calculation dialog', () => {
    it('opens and closes the selected score calculation', async () => {
        const league = {
            leagueId: 1, season: '2026', name: 'League', countryCode: 'BE',
            logo: '', start: new Date(), end: new Date(), current: true
        };
        const forecast = {
            leagueName: 'League', countryCode: 'BE', round: 2,
            forecastDetails: [
                {team: 'Team A', opponent: 'Team B', nextMatchHomeTeam: 'Team A',
                    nextMatchDate: '2026-01-01', totalPoints: 42, calculationHtml: '<p>Calculation A</p>'},
                {team: 'Team B', opponent: 'Team A', nextMatchHomeTeam: 'Team A',
                    nextMatchDate: '2026-01-01', totalPoints: 20, calculationHtml: '<p>Calculation B</p>'},
                {team: 'Team C', opponent: 'Team D', nextMatchHomeTeam: 'Team C',
                    nextMatchDate: '2026-01-01', totalPoints: 10}
            ]
        };
        await TestBed.configureTestingModule({
            imports: [ForecastsComponent],
            providers: [
                provideZonelessChangeDetection(),
                {provide: LeagueService, useValue: {getCurrentLeagues: () => of([league])}},
                {provide: ForecastService, useValue: {
                    forecasts: signal([]), forecastsError: signal(undefined),
                    getCurrentRound: () => of(1), getForecasts: () => of([forecast]),
                    selectLeagueForForecast: () => {}, selectRoundForForecast: () => {},
                    selectSeasonForForecast: () => {}
                }}
            ]
        }).compileComponents();
        const fixture = TestBed.createComponent(ForecastsComponent);
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();

        const buttons = fixture.nativeElement.querySelectorAll('button[aria-label^="Toon scoreberekening"]') as NodeListOf<HTMLButtonElement>;
        expect(buttons.length).toBe(2);
        expect(buttons[0].closest('td')?.textContent).toContain('42');
        expect(fixture.nativeElement.textContent).not.toContain('Calculation A');

        buttons[0].click();
        await fixture.whenStable();
        expect(document.querySelector('.forecast-calculation')?.textContent).toContain('Calculation A');
        expect(document.querySelectorAll('.forecast-calculation').length).toBe(1);
        const dialog = document.querySelector('[role="dialog"]') as HTMLElement;
        expect(dialog.textContent).toContain('Scoreberekening Team A');
        const close = dialog.querySelector('button[aria-label="Sluiten"]') as HTMLButtonElement;
        expect(close).not.toBeNull();
        close.click();
        await fixture.whenStable();
        expect(fixture.componentInstance.calculationVisible).toBeFalse();

        buttons[1].click();
        await fixture.whenStable();
        expect(document.querySelector('.forecast-calculation')?.textContent).toContain('Calculation B');
        expect(document.querySelector('[role="dialog"]')?.textContent).toContain('Scoreberekening Team B');
        expect(fixture.componentInstance.calculationVisible).toBeTrue();
        fixture.destroy();
    });
});
