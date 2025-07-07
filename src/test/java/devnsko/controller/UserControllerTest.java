package devnsko.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import devnsko.dto.UserDataDTO;
import devnsko.dto.UserResponseDTO;
import devnsko.dto.UserSignInDTO;
import devnsko.model.AppUser;
import devnsko.model.AppUserRole;
import devnsko.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private UserSignInDTO userSignInDTO;
    private UserDataDTO userDataDTO;
    private AppUser appUser;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize test data
        userSignInDTO = new UserSignInDTO();
        userSignInDTO.setUsername("testuser");
        userSignInDTO.setPassword("password123");

        userDataDTO = new UserDataDTO();
        userDataDTO.setUsername("testuser");
        userDataDTO.setEmail("test@example.com");
        userDataDTO.setPassword("password123");
        userDataDTO.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        appUser = new AppUser();
        appUser.setId(1);
        appUser.setUsername("testuser");
        appUser.setEmail("test@example.com");
        appUser.setPassword("encodedpassword");
        appUser.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1);
        userResponseDTO.setUsername("testuser");
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));
    }

    @Test
    void signin_ShouldReturnJwtToken_WhenCredentialsAreValid() throws Exception {
        // Given
        String expectedToken = "jwt-token-123";
        when(userService.signin(eq("testuser"), eq("password123"))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/users/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignInDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(userService, times(1)).signin("testuser", "password123");
    }

    @Test
    void signup_ShouldReturnJwtToken_WhenUserDataIsValid() throws Exception {
        // Given
        String expectedToken = "jwt-token-123";
        when(modelMapper.map(any(UserDataDTO.class), eq(AppUser.class))).thenReturn(appUser);
        when(userService.signup(any(AppUser.class))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(modelMapper, times(1)).map(any(UserDataDTO.class), eq(AppUser.class));
        verify(userService, times(1)).signup(any(AppUser.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ShouldReturnUsername_WhenUserIsAdmin() throws Exception {
        // Given
        String username = "testuser";
        doNothing().when(userService).delete(username);

        // When & Then
        mockMvc.perform(delete("/users/" + username)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(username));

        verify(userService, times(1)).delete(username);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void delete_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // Given
        String username = "testuser";

        // When & Then
        mockMvc.perform(delete("/users/" + username)
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).delete(username);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_ShouldReturnUserResponseDTO_WhenUserExists() throws Exception {
        // Given
        String username = "testuser";
        when(userService.search(username)).thenReturn(appUser);
        when(modelMapper.map(appUser, UserResponseDTO.class)).thenReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(get("/users/" + username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).search(username);
        verify(modelMapper, times(1)).map(appUser, UserResponseDTO.class);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void search_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // Given
        String username = "testuser";

        // When & Then
        mockMvc.perform(get("/users/" + username))
                .andExpect(status().isForbidden());

        verify(userService, never()).search(username);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whoami_ShouldReturnCurrentUser_WhenUserIsAdmin() throws Exception {
        // Given
        when(userService.whoami(any(HttpServletRequest.class))).thenReturn(appUser);
        when(modelMapper.map(appUser, UserResponseDTO.class)).thenReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).whoami(any(HttpServletRequest.class));
        verify(modelMapper, times(1)).map(appUser, UserResponseDTO.class);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void whoami_ShouldReturnCurrentUser_WhenUserIsClient() throws Exception {
        // Given
        when(userService.whoami(any(HttpServletRequest.class))).thenReturn(appUser);
        when(modelMapper.map(appUser, UserResponseDTO.class)).thenReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).whoami(any(HttpServletRequest.class));
        verify(modelMapper, times(1)).map(appUser, UserResponseDTO.class);
    }

    @Test
    void whoami_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).whoami(any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void refresh_ShouldReturnNewToken_WhenUserIsAdmin() throws Exception {
        // Given
        String expectedToken = "new-jwt-token-456";
        when(userService.refresh(any(String.class))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(get("/users/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(userService, times(1)).refresh(any(String.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void refresh_ShouldReturnNewToken_WhenUserIsClient() throws Exception {
        // Given
        String expectedToken = "new-jwt-token-456";
        when(userService.refresh(any(String.class))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(get("/users/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(userService, times(1)).refresh(any(String.class));
    }

    @Test
    void refresh_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/refresh"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).refresh(any(String.class));
    }

    @Test
    void signin_ShouldReturnBadRequest_WhenRequestBodyIsInvalid() throws Exception {
        // Given
        String invalidJson = "{\"username\": \"\"}";

        // When & Then
        mockMvc.perform(post("/users/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signin(any(String.class), any(String.class));
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenRequestBodyIsInvalid() throws Exception {
        // Given
        String invalidJson = "{\"username\": \"\"}";

        // When & Then
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(AppUser.class));
    }
}
