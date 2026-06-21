import {Component, inject, OnInit} from '@angular/core';
import {TabsModule} from 'primeng/tabs';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {AccordionModule} from 'primeng/accordion';

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule]
})
export class LeaguesComponent implements OnInit {

    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);

    constructor() { }

    ngOnInit(): void {
        this.countryService.getCountries().then((countries) => {
            console.log('aantal landen', countries.length);
        });
    }

}
