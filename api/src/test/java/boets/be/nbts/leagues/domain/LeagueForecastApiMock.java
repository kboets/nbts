package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class LeagueForecastApiMock implements LeagueForecastApi {
    @Override
    public List<ForecastLeague> getSelectedLeaguesForForecasting() {
        ForecastLeague forecastLeague = new ForecastLeague(144, "Jupiler league", 2026, "BE");
        return List.of(forecastLeague);
    }
}
