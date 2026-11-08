import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule, NgClass} from '@angular/common';
import {ConfirmationService, MessageService} from 'primeng/api';

@Component({
    selector: 'app-forecasts',
    standalone: true,
    imports: [CommonModule, FormsModule, NgClass],
    providers: [MessageService, ConfirmationService],
    templateUrl: './forecasts.component.html'
})
export class ForecastsComponent implements OnInit {
    constructor() { }

    ngOnInit(): void {

    }
}
