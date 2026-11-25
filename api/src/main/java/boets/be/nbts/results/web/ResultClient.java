package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Result;

import java.util.List;

public interface ResultClient {

    List<Result> getResultsByLeagueAndSeason(int leagueId, int season);

    default Result mapToResult(ResultApiResponse.ResultApiItem item) {
        return new Result(
                item.fixture().id(),
                item.league().name(),
                item.teams().home().name(),
                item.teams().away().name(),
                item.goals().home(),
                item.goals().away(),
                item.fixture().date().toLocalDate(),
                item.fixture().status().shortStatus(),
                Boolean.TRUE.equals(item.teams().home().winner()),
                Boolean.TRUE.equals(item.teams().away().winner()),
                extractRound(item.league().round()),
                false
        );
    }

    default List<Result> markCurrentRound(List<Result> results, int currentRound) {
        return results.stream()
                .map(result -> result.withCurrentRound(result.round() == currentRound))
                .toList();

    }

    private int extractRound(String round) {
        return Integer.parseInt(round.substring(round.lastIndexOf('-') + 1).trim());
    }
}
