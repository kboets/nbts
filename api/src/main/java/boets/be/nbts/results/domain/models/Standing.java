package boets.be.nbts.results.domain.models;

import java.time.LocalDate;

public record Standing(Integer leagueId, Integer season, Integer rank, String teamName, Integer teamId, Integer points, Integer played, Integer won, Integer drawn, Integer lost, LocalDate lastUpdated) {

    public Standing withRank(Integer rank) {
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
