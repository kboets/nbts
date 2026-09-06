import {Routes} from '@angular/router';
import {LeaguesComponent} from './leagues/leagues.component';
import {ResultsComponent} from './results/results.component';

export default [
    { path: 'leagues', component: LeaguesComponent },
    { path: 'results', component: ResultsComponent }
] as Routes;
