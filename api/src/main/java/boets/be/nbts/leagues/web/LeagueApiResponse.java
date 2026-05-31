package boets.be.nbts.leagues.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueApiResponse(List<LeagueApiItem> response) {
    public record LeagueApiItem(
            LeagueApiLeague league,
            LeagueApiLeague.LeagueApiCountry country,
            List<LeagueApiLeague.LeagueApiCountry.LeagueApiSeason> seasons)
    {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record LeagueApiLeague(
                Integer id,
                String name,
                String logo)
        {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record LeagueApiCountry(
                    String code)
            {
                @JsonIgnoreProperties(ignoreUnknown = true)
                public record LeagueApiSeason(
                        Integer year,
                        LocalDate start,
                        LocalDate end,
                        Boolean current)
                {
                }
            }
        }
    }
}
