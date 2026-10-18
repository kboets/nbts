package boets.be.nbts.leagues;

import boets.be.nbts.leagues.domain.models.ForecastLeague;

import java.util.List;

public interface LeagueForecastApi {

    List<ForecastLeague> getSelectedLeaguesForForecasting();
}
