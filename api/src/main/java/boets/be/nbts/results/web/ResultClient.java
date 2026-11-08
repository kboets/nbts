package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Result;

import java.time.LocalDate;
import java.util.Comparator;
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

    default List<Result> markCurrentRound(List<Result> results) {
        LocalDate currentDate = LocalDate.now();
        List<Result> finishedResults = results.stream()
                .filter(result -> "FT".equalsIgnoreCase(result.matchStatus()))
                .filter(result -> result.matchDate().isBefore(currentDate))
                .sorted(Comparator.comparing(Result::matchDate).reversed())
                .toList();

        Result latestResult = finishedResults.isEmpty() ? results.getFirst() : finishedResults.get(2);
        int currentRound = latestResult.round();
        return results.stream()
                .map(result -> result.withCurrentRound(result.round() == currentRound))
                .toList();

    }

    private int extractRound(String round) {
        return Integer.parseInt(round.substring(round.lastIndexOf('-') + 1).trim());
    }
}
