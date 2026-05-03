package id.ac.ui.cs.advprog.auth_profile.config;

import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAccountSeedConfig {

    @Bean
    public CommandLineRunner seedDemoAccounts(
            UserRepository userRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-accounts.enabled:false}") boolean enabled
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            ensureAccount(userRepository, jdbcTemplate, passwordEncoder, 1001L, "demo@json.app", "demo-buyer", "Demo Buyer", "TITIPER");
            ensureAccount(userRepository, jdbcTemplate, passwordEncoder, 2001L, "jastiper1@json.app", "jastiper-2001", "Jastiper 2001", "JASTIPER");
            ensureAccount(userRepository, jdbcTemplate, passwordEncoder, 2002L, "jastiper2@json.app", "jastiper-2002", "Jastiper 2002", "JASTIPER");
            ensureAccount(userRepository, jdbcTemplate, passwordEncoder, 2003L, "jastiper3@json.app", "jastiper-2003", "Jastiper 2003", "JASTIPER");
            ensureAccount(userRepository, jdbcTemplate, passwordEncoder, 9001L, "admin@json.app", "json-admin", "JSON Admin", "ADMIN");
        };
    }

    private void ensureAccount(
            UserRepository userRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            Long id,
            String email,
            String username,
            String fullName,
            String role
    ) {
        if (userRepository.findById(id).isPresent()) {
            return;
        }

        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO users (id, email, password, username, full_name, role) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                email,
                passwordEncoder.encode("Demo123!"),
                username,
                fullName,
                role
        );
    }
}
