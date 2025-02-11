package ru.antonov.securitytest;

import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.antonov.securitytest.auth.service.AuthenticationService;
import ru.antonov.securitytest.auth.token.TokenRepository;


@SpringBootApplication
@RequiredArgsConstructor
public class SecuritytestApplication {

	private final PasswordEncoder passwordEncoder;
	private final TokenRepository tokenRepository;


	public static void main(String[] args) {
		SpringApplication.run(SecuritytestApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(AuthenticationService authService) {
		final Log logger = LogFactory.getLog(getClass());

//		return args -> {
//			var admin = RegisterRequest.builder()
//					.name("Admin")
//					.surname("Admin")
//					.patronymic("Admin")
//					.email("admin123@gmail.com")
//					.password("admin123")
//					.gr(null)
//					.build();
//			logger.info("Admin token: " + authService.registerAdmin(admin).getAccessToken());
//		};
		return args -> {

		};
	}
}
