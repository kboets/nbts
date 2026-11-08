package boets.be.nbts.results.domain.models;

import java.time.LocalDate;

public record Standing(int leagueId, int season, int rank, String teamName, int teamId, int points, int played, int won, int drawn, int lost, LocalDate lastUpdated) {

    public Standing withRank(int rank) {
        return new Standing(
                leagueId,
                season,
                rank,
                teamName,
                teamId,
                points,
                played,
                won,
                drawn,
                lost,
                lastUpdated
        );
    }
}
