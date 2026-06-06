package boets.be.nbts.leagues.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Objects;

@Table(schema = "leagues", name = "league")
@Data
public class LeagueEntity {
    @Id
    private Integer id;
    private String name;
    private Integer leagueId;
    private String countryCode;
    private LocalDate startSeason;
    private LocalDate endSeason;
    private Integer season;
    private String logo;
    private boolean current;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LeagueEntity that = (LeagueEntity) o;
        return current == that.current && Objects.equals(name, that.name) && Objects.equals(leagueId, that.leagueId) && Objects.equals(countryCode, that.countryCode) && Objects.equals(startSeason, that.startSeason) && Objects.equals(endSeason, that.endSeason) && Objects.equals(logo, that.logo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, leagueId, countryCode, startSeason, endSeason, logo, current);
    }
}
