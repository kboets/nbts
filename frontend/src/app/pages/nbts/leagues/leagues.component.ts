import {Component, inject, OnInit, WritableSignal} from '@angular/core';
import {TabsModule} from 'primeng/tabs';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {AccordionModule} from 'primeng/accordion';
import {Country} from '../../shared/models/country';

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule]
})
export class LeaguesComponent implements OnInit {

    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    public countries:WritableSignal<Country[] | null>;

    constructor() {
        this.countries = this.countryStore.countries;
    }

    ngOnInit(): void {
        this.countryService.getCountries();
    }

    onNewLeaguesCountryOpenTab(event: any) {
        console.log(event);
    }

}
