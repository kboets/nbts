package boets.be.nbts.results.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoundApiResponse(
        @JsonProperty("get") String request,
        Parameters parameters,
        List<String> errors,
        Integer results,
        Paging paging,
        List<String> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parameters(
            String league,
            String current,
            String season
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            Integer current,
            Integer total
    ) {
    }
}