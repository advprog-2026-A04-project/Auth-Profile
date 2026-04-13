package id.ac.ui.cs.advprog.auth_profile;

import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthProfileApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@json.com").isEmpty()) {
				User admin = new User();
				admin.setEmail("admin@json.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setUsername("admin");
				admin.setRole("ADMIN");
				userRepository.save(admin);
				System.out.println(">>> Admin user created: admin@json.com / admin123");
			}
		};
	}
}
