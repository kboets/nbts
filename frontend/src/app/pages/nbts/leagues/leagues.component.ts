import {Component, computed, effect, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule, NgClass} from '@angular/common';
import {TabsModule} from 'primeng/tabs';
import {TableModule} from 'primeng/table';
import {CountryStore} from '../../service/country.store';
import {CountryService} from '../../service/country.service';
import {LeagueService} from '../../service/league.service';
import {League} from '../../shared/models/league';
import {AccordionModule} from 'primeng/accordion';
import {AutoCompleteCompleteEvent, AutoCompleteModule} from 'primeng/autocomplete';
import {TagModule} from 'primeng/tag';
import {DataViewModule} from 'primeng/dataview';
import {ButtonModule} from 'primeng/button';
import {ToastModule} from 'primeng/toast';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {Country} from '../../shared/models/country';
import {LeagueStore} from "../../service/league.store";

@Component({
    selector: 'app-leagues',
    templateUrl: './leagues.component.html',
    standalone: true,
    imports: [TabsModule, AccordionModule, AutoCompleteModule, FormsModule, DataViewModule, ButtonModule, TagModule, NgClass, CommonModule, ToastModule, TableModule, ConfirmDialogModule],
    providers: [MessageService, ConfirmationService]
})
export class LeaguesComponent implements OnInit {

    // inject 3rd party services
    private messageService = inject(MessageService);
    private confirmationService = inject(ConfirmationService);

    // stores and services
    public countryStore: CountryStore = inject(CountryStore);
    private countryService = inject(CountryService);
    private leaguesService = inject(LeagueService);


    public countries = signal<Country[] | null>(null);
    filteredCountries: Country[] = [];
    selectedCountry: Country | null = null;

    // get signals
    newLeagues = this.leaguesService.newLeagues;
    newLeaguesError = this.leaguesService.newLeaguesError;
    public loadingNewLeagues = signal<boolean>(false);

    // get league store
    private leagueStore = inject(LeagueStore);
    public leagues = this.leagueStore.leagues;

    public leaguesWithCountry = computed(() => {
        const list = this.leagueStore.leagues();
        const countriesList = this.countryStore.countries();
        if (!list) return null;
        return list.map(league => ({
            ...league,
            nameNL: countriesList?.find(c => c.countryCode === league.countryCode)?.nameNL || ''
        }));
    });

    constructor() {
        this.countryService.getCountries().subscribe((countriesList) => {
            this.countries.set(countriesList);
        });

        effect(() => {
            this.newLeagues();
            //console.log('newLeagues effect triggered, loadingNewLeagues:', this.loadingNewLeagues());
            if (this.loadingNewLeagues()) {
                this.loadingNewLeagues.set(false);
            }
        });
    }

    ngOnInit(): void {
        this.countryService.getCountries().subscribe((countriesList) => {
            this.countries.set(countriesList);
        });
        this.reloadCurrentLeagues();
    }

    selectNewLeague(league: League) {
        //console.log('select league: ->', league.name);
        this.leaguesService.saveNewLeague(league).subscribe({
            next: (savedLeague) => {
                //console.log('league saved', savedLeague);
                this.messageService.add({severity:'success', summary: 'Success', detail: 'Nieuwe competitie is succesvol opgeslagen.'});
                // refilter the list of leagues
                this.leaguesService.resetCountryForNewLeagues();
                this.loadingNewLeagues.set(true);
                setTimeout(() => {
                    this.leaguesService.selectCountryForNewLeagues(league.countryCode);
                }, 500);
                this.leagueStore.removeLeague(savedLeague.leagueId);
                this.reloadCurrentLeagues();
            },
            error: (error) => {
                //console.error('error saving league', error);
                this.messageService.add({severity:'error', summary: 'Error', detail: 'Er is iets misgegaan bij het opslaan van de nieuwe competitie.'});
            }
        });
    }

    onNewLeaguesCountryOpenTab(event: any) {
        //console.log('onNewLeaguesCountryOpenTab', event);
        this.leaguesService.resetCountryForNewLeagues();
        this.loadingNewLeagues.set(true);
        setTimeout(() => {
            this.leaguesService.selectCountryForNewLeagues(event);
        }, 500);
    }

    onSelectCountry(event: any) {
        const selected = event?.value || this.selectedCountry;
        if (selected) {
            this.countries.set([selected]);
        }
    }

    onCountryUnselect() {
        this.selectedCountry = null;
        this.countries.set(this.countryStore.countries());
    }

    filterCountry(event: AutoCompleteCompleteEvent) {
        const query = (event.query || '').toLowerCase();
        const countryList = this.countryStore.countries() || [];

        this.filteredCountries = countryList.filter(country =>
            country.nameNL?.toLowerCase().includes(query) ||
            country.nameEN?.toLowerCase().includes(query)
        );
    }

    confirmDelete(event: Event, league: League) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: 'Moet deze competitie verwijderd worden?',
            header: 'Opgelet',
            icon: 'pi pi-info-circle',
            rejectLabel: 'Annuleren',
            rejectButtonProps: {
                label: 'Annuleren',
                severity: 'secondary',
                outlined: true
            },
            acceptButtonProps: {
                label: 'Verwijderen',
                severity: 'danger'
            },

            accept: () => {
                this.removeLeague(league);
            },
            reject: () => {
                this.messageService.add({ severity: 'warn', summary: 'Geweigerd', detail: 'Competitie niet verwijderd' });
            }
        });
    }

    private removeLeague(league: League) {
        this.leaguesService.removeLeague(league).subscribe({
            next: (success) => {
                if (success) {
                    this.messageService.add({severity:'success', summary: 'Success', detail: 'Competitie is succesvol verwijderd.'});
                    this.reloadCurrentLeagues();
                } else {
                    this.messageService.add({severity:'error', summary: 'Error', detail: 'Competitie kon niet worden verwijderd.'});
                }
            },
            error: (error) => {
                this.messageService.add({severity:'error', summary: 'Error', detail: 'Er is een fout opgetreden bij het verwijderen van de competitie.'});
            }
        });
    }

    reloadCurrentLeagues() {
        this.leagueStore.startLoading();
        this.leaguesService.getCurrentLeagues().subscribe({
            next: (leagues: League[]) => this.leagueStore.setLeagues(leagues),
            error: (err: any) => {
                const msg = err?.message || 'Er is iets misgegaan bij het ophalen van de competities.';
                this.leagueStore.setError(msg);
            }
        })
    }


}
