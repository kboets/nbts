package boets.be.nbts.results.domain.models;

public record Result(int resultId, String leagueName, String homeTeam, String awayTeam, Integer homeTeamScore, Integer awayTeamScore,
                     String matchDate, String matchStatus, boolean homeTeamHasWon, boolean homeTeamHasLost, int round, boolean isCurrent){

    public Result withCurrentRound(boolean currentRound) {
        return new Result(
                resultId,
                leagueName,
                homeTeam,
                awayTeam,
                homeTeamScore,
                awayTeamScore,
                matchDate,
                matchStatus,
                homeTeamHasWon,
                homeTeamHasLost,
                round,
                currentRound
        );
    }
}
