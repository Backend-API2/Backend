package backend_api.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "backend_api.Backend")
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		// Establecer la zona horaria por defecto de la aplicación
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
