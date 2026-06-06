package boets.be.nbts.leagues.web;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record League(Integer leagueId,
                     String name,
                     String logo,
                     String countryCode,
                     Integer season,
                     LocalDate start,
                     LocalDate end,
                     Boolean current) {
}
