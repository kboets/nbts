package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Standing;

import java.util.List;

public interface StandingClient {

    List<Standing> getStandingsByLeagueAndSeason(int leagueId, int season);

    default Standing mapToStanding(StandingApiResponse.League league, StandingApiResponse.StandingEntry standingEntry) {
        return new Standing(
                league.id(),
                league.season(),
                standingEntry.rank(),
                standingEntry.team().name(),
                standingEntry.team().id(),
                standingEntry.points(),
                standingEntry.all().played(),
                standingEntry.all().win(),
                standingEntry.all().draw(),
                standingEntry.all().lose(),
                standingEntry.update().toLocalDate()
        );
    }
}
