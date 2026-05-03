package id.ac.ui.cs.advprog.auth_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.demo-seed.enabled=true")
@AutoConfigureMockMvc
class DemoAccountSeedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void seededAccountsExistWithExpectedRoles() {
        assertEquals("TITIPER", userRepository.findById(1001L).orElseThrow().getRole());
        assertEquals("JASTIPER", userRepository.findById(2001L).orElseThrow().getRole());
        assertEquals("JASTIPER", userRepository.findById(2002L).orElseThrow().getRole());
        assertEquals("JASTIPER", userRepository.findById(2003L).orElseThrow().getRole());
        assertEquals("ADMIN", userRepository.findById(9001L).orElseThrow().getRole());
    }

    @Test
    void seededAccountsCanLogIn() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@json.app",
                                  "password": "Demo123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.role").value("TITIPER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jastiper1@json.app",
                                  "password": "Demo123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2001))
                .andExpect(jsonPath("$.role").value("JASTIPER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@json.app",
                                  "password": "Demo123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void seededAccountsAreOptInOnly() {
        assertEquals(5, userRepository.count());
    }
}
