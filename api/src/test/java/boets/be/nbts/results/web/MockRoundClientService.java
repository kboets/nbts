package boets.be.nbts.results.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Profile("test")
public class MockRoundClientService implements RoundClient {

    @Value("classpath:/boets/be/nbts/round/roundsByLeagueAndSeason.json")
    private Resource resource;

    private final JsonMapper jsonMapper;

    public MockRoundClientService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }


    @Override
    public int getRoundByLeagueAndSeason(int leagueId, int season) {
        try {
            String contentAsString = resource.getContentAsString(UTF_8);
            // map the content to a list of Result objects using your preferred JSON library
            RoundApiResponse apiResponse = jsonMapper.readValue(contentAsString, new TypeReference<>() {});
            String roundString = apiResponse.response().getFirst();
            return Integer.parseInt(roundString.substring(roundString.lastIndexOf('-') + 1).trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
