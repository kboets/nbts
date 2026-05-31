package boets.be.nbts.leagues.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

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
}
