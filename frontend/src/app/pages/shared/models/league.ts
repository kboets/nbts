export interface League {
    leagueId: number;
    name: string;
    logo: string;
    countryCode: string;
    season: string;
    start: Date;
    end: Date;
    current: boolean;
}
