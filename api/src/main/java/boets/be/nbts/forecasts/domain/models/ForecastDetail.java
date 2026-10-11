package boets.be.nbts.forecasts.domain.models;

import java.time.LocalDate;

public record  ForecastDetail(LocalDate nextMatchDate, String team, String opponent, int totalPoints) {
}
