package boets.be.nbts.results.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultApiResponse(
        @JsonProperty("get") String request,
        Parameters parameters,
        List<String> errors,
        Integer results,
        Paging paging,
        List<ResultApiItem> response)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parameters(
            String league,
            String season)
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            Integer current,
            Integer total)
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultApiItem(
            Fixture fixture,
            League league,
            Teams teams,
            Goals goals,
            Score score)
    {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Fixture(
                Integer id,
                String referee,
                String timezone,
                OffsetDateTime date,
                Long timestamp,
                Periods periods,
                Venue venue,
                Status status)
        {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Periods(
                    Long first,
                    Long second)
            {
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Venue(
                    Integer id,
                    String name,
                    String city)
            {
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Status(
                    @JsonProperty("long") String longStatus,
                    @JsonProperty("short") String shortStatus,
                    Integer elapsed,
                    Integer extra)
            {
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record League(
                Integer id,
                String name,
                String country,
                String logo,
                String flag,
                Integer season,
                String round,
                Boolean standings)
        {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Teams(
                Team home,
                Team away)
        {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Team(
                    Integer id,
                    String name,
                    String logo,
                    Boolean winner)
            {
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Goals(
                Integer home,
                Integer away)
        {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Score(
                ScoreValue halftime,
                ScoreValue fulltime,
                ScoreValue extratime,
                ScoreValue penalty)
        {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record ScoreValue(
                    Integer home,
                    Integer away)
            {
            }
        }
    }
}
