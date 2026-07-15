package boets.be.nbts.leagues.domain.models;

public record LeagueDeletedEvent(String countryCode,
                                 int leagueId,
                                 int season) {}
