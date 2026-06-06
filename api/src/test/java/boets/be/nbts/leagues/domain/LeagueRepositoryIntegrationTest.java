package boets.be.nbts.leagues.domain;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJdbcTest
@Sql("/db/testdata/insert_test_data.sql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        CleanFlywayTestConfiguration.class,
        TestcontainersConfiguration.class
})
@Testcontainers
class LeagueRepositoryIntegrationTest {

    @Autowired
    private LeagueRepository leagueRepository;

    @Test
    public void getCurrentlyActiveLeagues_shouldReturnCorrectly() {
        List<LeagueEntity> byCurrent = leagueRepository.findByCurrent(true);
        assertThat(byCurrent.size()).isEqualTo(2);
        assertThat(byCurrent.getFirst().getLeagueId()).isEqualTo(1);
    }

    @Test
    public void getInActiveLeagues_shouldReturnCorrectly() {
        List<LeagueEntity> byCurrent = leagueRepository.findByCurrent(false);
        assertThat(byCurrent.size()).isEqualTo(1);
        assertThat(byCurrent.getFirst().getLeagueId()).isEqualTo(3);
    }
}
