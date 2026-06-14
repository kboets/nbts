package boets.be.nbts;

import boets.be.nbts.leagues.web.CountryClientService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NbtsApplicationTests {

	@MockitoBean
	private CountryClientService countryClientService;

	@Test
	void contextLoads() {
	}

}
