package id.ac.ui.cs.advprog.auth_profile;

import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthProfileApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthProfileApplication.class);
	private static final String DEMO_PASSWORD = "Password123!";

	public static void main(String[] args) {
		SpringApplication.run(AuthProfileApplication.class, args);
	}

	@Bean
	CommandLineRunner demoAccountSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			seedDemoAccount(userRepository, passwordEncoder, "buyer.demo@auth.local", "demo-buyer", "Demo Buyer", "TITIPER");
			seedDemoAccount(userRepository, passwordEncoder, "jastiper.demo@auth.local", "demo-jastiper", "Demo Jastiper", "JASTIPER");
			seedDemoAccount(userRepository, passwordEncoder, "admin.demo@auth.local", "demo-admin", "Demo Admin", "ADMIN");
		};
	}

	private void seedDemoAccount(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			String email,
			String username,
			String fullName,
			String role
	) {
		if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
			LOGGER.info("Demo account already exists for role {}", role);
			return;
		}

		User user = new User();
		user.setEmail(email);
		user.setUsername(username);
		user.setFullName(fullName);
		user.setRole(role);
		user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));

		userRepository.save(user);
		LOGGER.info("Seeded demo account for role {}", role);
	}
}
