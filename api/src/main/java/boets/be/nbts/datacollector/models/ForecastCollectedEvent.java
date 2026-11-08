package boets.be.nbts.datacollector.models;

import java.util.List;

public record ForecastCollectedEvent(List<ForecastRawData> forecastRawData) {
}
