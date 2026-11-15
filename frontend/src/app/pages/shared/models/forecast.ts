import { ForecastDetail } from './forecastDetail';

export interface Forecast {
    leagueName: string;
    countryCode: string;
    round: number;
    forecastDetails: ForecastDetail[];
}
