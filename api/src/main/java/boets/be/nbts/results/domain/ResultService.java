package boets.be.nbts.results.domain;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.web.ResultClient;
import boets.be.nbts.results.web.RoundClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultClient resultClientService;
    private final RoundClient roundClientService;

    @Cacheable(value = "results", key = "#leagueId + ':' + #season")
    public List<Result> getResultsByLeagueAndSeason(int leagueId, int season) {
        log.info("Fetching results for league {} and season {}", leagueId, season);
        // Call the ResultClient to fetch results
        List<Result> resultsByLeagueAndSeason = resultClientService.getResultsByLeagueAndSeason(leagueId, season);
        // get the current round from the RoundClient
        int currentRound = roundClientService.getRoundByLeagueAndSeason(leagueId, season);
        // Mark the current round in the results
        resultsByLeagueAndSeason = resultClientService.markCurrentRound(resultsByLeagueAndSeason, currentRound);
        return resultsByLeagueAndSeason;
    }


}
