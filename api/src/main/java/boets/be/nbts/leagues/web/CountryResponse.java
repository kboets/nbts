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


    public static final class CountryResponseBuilder {
        private Flags flags;
        private Name name;
        private Translations translations;
        private String cca2;

        private CountryResponseBuilder() {
        }

        public static CountryResponseBuilder aCountryResponse() {
            return new CountryResponseBuilder();
        }

        public CountryResponseBuilder withFlags(Flags flags) {
            this.flags = flags;
            return this;
        }

        public CountryResponseBuilder withName(Name name) {
            this.name = name;
            return this;
        }

        public CountryResponseBuilder withTranslations(Translations translations) {
            this.translations = translations;
            return this;
        }

        public CountryResponseBuilder withCca2(String cca2) {
            this.cca2 = cca2;
            return this;
        }

        public CountryResponse build() {
            return new CountryResponse(flags, name, translations, cca2);
        }
    }
}


