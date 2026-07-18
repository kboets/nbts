package boets.be.nbts.leagues.domain.models;

public record LeagueSavedEvent(
        String countryCode,
        int leagueId,
        int season) {
}
