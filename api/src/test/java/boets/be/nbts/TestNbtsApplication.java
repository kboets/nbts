package boets.be.nbts;

import org.springframework.boot.SpringApplication;

public class TestNbtsApplication {

	static void main(String[] args) {
		SpringApplication.from(NbtsApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}

}
