package id.ac.ui.cs.advprog.auth_profile;

import static org.junit.jupiter.api.Assertions.assertFalse;

import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.demo-seed.enabled=false")
@AutoConfigureMockMvc
class DemoAccountSeedDisabledIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void seededAccountsShouldNotExistWhenDemoSeedIsDisabled() {
        assertFalse(userRepository.findById(1001L).isPresent());
        assertFalse(userRepository.findById(2001L).isPresent());
        assertFalse(userRepository.findById(9001L).isPresent());
    }
}
