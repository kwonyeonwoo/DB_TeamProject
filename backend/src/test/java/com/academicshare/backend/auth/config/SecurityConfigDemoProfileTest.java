package com.academicshare.backend.auth.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
@Transactional
class SecurityConfigDemoProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void demoProfileAllowsStateChangingRequestWithoutCsrfToken() throws Exception {
        User user = userRepository.saveAndFlush(new User(
                "demo-csrf-user",
                passwordEncoder.encode("password123"),
                "Demo User",
                "demo-csrf@example.com"
        ));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSessionAttributes.CURRENT_USER_ID, user.getId());

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isNoContent());
    }
}
