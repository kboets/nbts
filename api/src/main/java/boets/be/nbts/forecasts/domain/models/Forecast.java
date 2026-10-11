package boets.be.nbts.forecasts.domain.models;

import java.util.List;

public record Forecast(String leagueName, String countryCode, List<ForecastDetail> forecastDetails) {

    public Forecast withForecastDetails(List<ForecastDetail> forecastDetails) {
        return new Forecast(
                leagueName,
                countryCode,
                forecastDetails
        );
    }


}
