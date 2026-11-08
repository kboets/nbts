package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Standing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Profile("test")
public class MockStandingClientService implements StandingClient {

    @Value("${classpath:/boets/be/nbts/standing/mock/belgian2026round10.json}")
    private Resource resource;

    private final JsonMapper jsonMapper;

    public MockStandingClientService() {
        this.jsonMapper = new JsonMapper();
    }

    @Override
    public List<Standing> getStandingsByLeagueAndSeason(int leagueId, int season) {
        return getStandings();
    }

    private List<Standing> getStandings() {
        try {
            String standingAsString = resource.getContentAsString(UTF_8);
            // map the content to a list of Standing objects using your preferred JSON library
            StandingApiResponse apiResponse = jsonMapper.readValue(standingAsString, StandingApiResponse.class);
            return apiResponse.response()
                    .stream()
                    .flatMap(responseItem -> responseItem.league()
                            .standings()
                            .stream()
                            .flatMap(List::stream)
                            .map(standingEntry -> mapToStanding(responseItem.league(), standingEntry)))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
