package boets.be.nbts.leagues.web;

import java.time.LocalDate;

public record League(Integer leagueId,
                     String name,
                     String logo,
                     String countryCode,
                     Integer season,
                     LocalDate start,
                     LocalDate end,
                     Boolean current) {

}
