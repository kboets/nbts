package boets.be.nbts.leagues.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "leagues", name="country")
@lombok.Data
public class CountryEntity {
    @Id
    private Integer id;
    private String countryCode;
    @Column("name_en")
    private String name;
    @Column("name_nl")
    private String dutchName;
    private String flagUrl;
    private String region;
}
