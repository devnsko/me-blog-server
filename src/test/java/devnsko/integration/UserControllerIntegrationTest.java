package devnsko.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import devnsko.dto.UserDataDTO;
import devnsko.dto.UserSignInDTO;
import devnsko.model.AppUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.h2.console.enabled=true",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserSignInDTO userSignInDTO;
    private UserDataDTO userDataDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userSignInDTO = new UserSignInDTO();
        userSignInDTO.setUsername("testuser");
        userSignInDTO.setPassword("password123");

        userDataDTO = new UserDataDTO();
        userDataDTO.setUsername("testuser");
        userDataDTO.setEmail("test@example.com");
        userDataDTO.setPassword("password123");
        userDataDTO.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));
    }

    @Test
    void signup_ShouldCreateUserAndReturnToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }

    @Test
    void signin_ShouldReturnToken_AfterSuccessfulSignup() throws Exception {
        // First sign up
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andExpect(status().isOk());

        // Then sign in
        mockMvc.perform(post("/users/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignInDTO))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }

    @Test
    void signin_ShouldReturnError_WhenUserDoesNotExist() throws Exception {
        // Given
        UserSignInDTO nonExistentUser = new UserSignInDTO();
        nonExistentUser.setUsername("nonexistent");
        nonExistentUser.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/users/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentUser))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void signup_ShouldReturnError_WhenUsernameAlreadyExists() throws Exception {
        // First sign up
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andExpect(status().isOk());

        // Try to sign up again with the same username
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ShouldDeleteUser_WhenUserIsAdmin() throws Exception {
        // First sign up a user
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andExpect(status().isOk());

        // Then delete the user
        mockMvc.perform(delete("/users/" + userDataDTO.getUsername())
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(userDataDTO.getUsername()));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void delete_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(delete("/users/testuser")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_ShouldReturnUser_WhenUserExistsAndRequesterIsAdmin() throws Exception {
        // First sign up a user
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andExpect(status().isOk());

        // Then search for the user
        mockMvc.perform(get("/users/" + userDataDTO.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDataDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(userDataDTO.getEmail()));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void search_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/testuser"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whoami_ShouldReturnCurrentUser_WhenAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/me"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void refresh_ShouldReturnNewToken_WhenAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/refresh"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }
}
