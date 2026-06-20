import {Component, OnInit} from '@angular/core';
import {TabsModule} from 'primeng/tabs';

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule]
})
export class LeaguesComponent implements OnInit {

    ngOnInit(): void {
    }

}
