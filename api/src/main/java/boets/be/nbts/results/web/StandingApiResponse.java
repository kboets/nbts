package boets.be.nbts.results.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingApiResponse(
        @JsonProperty("get") String request,
        Parameters parameters,
        List<String> errors,
        Integer results,
        Paging paging,
        List<StandingApiItem> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parameters(
            String league,
            String season
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            Integer current,
            Integer total
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingApiItem(
            League league
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record League(
            Integer id,
            String name,
            String country,
            String logo,
            String flag,
            Integer season,
            List<List<StandingEntry>> standings
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingEntry(
            Integer rank,
            Team team,
            Integer points,
            Integer goalsDiff,
            String group,
            String form,
            String status,
            String description,
            StandingStats all,
            StandingStats home,
            StandingStats away,
            OffsetDateTime update
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(
            Integer id,
            String name,
            String logo
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingStats(
            Integer played,
            Integer win,
            Integer draw,
            Integer lose,
            Goals goals
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(
            @JsonProperty("for") Integer goalsFor,
            Integer against
    ) {
    }
}
