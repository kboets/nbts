package boets.be.nbts.results.domain;

import boets.be.nbts.results.domain.models.Standing;
import boets.be.nbts.results.web.StandingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandingService {

    private final StandingClient standingClientService;

    @Cacheable(value = "standings", key = "#league + ':' + #season")
    public List<Standing> getStandingsByLeagueAndSeason(int league, int season) {
        log.info("Fetching standings for league {} and season {}", league, season);
        return standingClientService.getStandingsByLeagueAndSeason(league, season);
    }
}
