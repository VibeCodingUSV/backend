package ro.usv.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		// Orele rezervărilor sunt salvate fără fus orar (ora locală a universității).
		// Serverul trebuie să compare cu aceeași oră locală, nu cu UTC.
		TimeZone.setDefault(TimeZone.getTimeZone(
				System.getenv().getOrDefault("APP_TIMEZONE", "Europe/Bucharest")));

		SpringApplication.run(BackendApplication.class, args);
	}
}