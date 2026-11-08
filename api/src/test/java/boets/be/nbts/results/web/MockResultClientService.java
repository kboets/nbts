package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Profile("test")
public class MockResultClientService implements ResultClient {

    @Value("${classpath:/boets/be/nbts/results/mock/belgian2026.json}")
    private Resource resource;

    private final JsonMapper jsonMapper;

    public MockResultClientService() {
        this.jsonMapper = new JsonMapper();
    }

    @Override
    public List<Result> getResultsByLeagueAndSeason(int leagueId, int season) {
        return getResults();
    }

    private List<Result> getResults() {
        try {
            String contentAsString = resource.getContentAsString(UTF_8);
            // map the content to a list of Result objects using your preferred JSON library
            ResultApiResponse apiResponse = jsonMapper.readValue(contentAsString, new TypeReference<>() {});
            List<Result> results = apiResponse.response().stream()
                    .map(this::mapToResult)
                    .toList();
            return markCurrentRound(results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
