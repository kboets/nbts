package boets.be.nbts.leagues.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("leagues.country")
public class CountryEntity {

    @Id
    private Integer id;

    private String countryCode;

    @Column("nameEN")
    private String name;

    @Column("nameNL")
    private String dutchName;

    private String flagUrl;
}
