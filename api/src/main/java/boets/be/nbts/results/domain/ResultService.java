package boets.be.nbts.results.domain;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.web.ResultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultClientService resultClientService;

    @Cacheable(value = "results", key = "#league + ':' + #season")
    public List<Result> getResultsByLeagueAndSeason(int league, int season) {
        log.info("Fetching results for league {} and season {}", league, season);
        return resultClientService.getResultsByLeagueAndSeason(league, season);
    }
}
