package boets.be.nbts.leagues.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryResponse(Flags flags,
                              Name name,
                              Translations translations,
                              String cca2) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Flags(
            String png,
            String svg,
            String alt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(
            String common,
            String official
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Translations(
            Translation nld
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Translation(
            String common,
            String official
    ) {
    }
}


