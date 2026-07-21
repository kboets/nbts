package boets.be.nbts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NbtsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NbtsApplication.class, args);
	}

}
