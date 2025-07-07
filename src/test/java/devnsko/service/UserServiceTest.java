package devnsko.service;

import devnsko.exception.CustomException;
import devnsko.model.AppUser;
import devnsko.model.AppUserRole;
import devnsko.repository.UserRepository;
import devnsko.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserService userService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(1);
        appUser.setUsername("testuser");
        appUser.setEmail("test@example.com");
        appUser.setPassword("encodedpassword");
        appUser.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));
    }

    @Test
    void signin_ShouldReturnToken_WhenCredentialsAreValid() {
        // Given
        String username = "testuser";
        String password = "password123";
        String expectedToken = "jwt-token-123";
        
        when(userRepository.findByUsername(username)).thenReturn(appUser);
        when(jwtTokenProvider.createToken(username, appUser.getAppUserRoles())).thenReturn(expectedToken);
        
        // When
        String actualToken = userService.signin(username, password);
        
        // Then
        assertEquals(expectedToken, actualToken);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername(username);
        verify(jwtTokenProvider, times(1)).createToken(username, appUser.getAppUserRoles());
    }

    @Test
    void signin_ShouldThrowCustomException_WhenCredentialsAreInvalid() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new AuthenticationException("Invalid credentials") {});
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.signin(username, password);
        });
        
        assertEquals("Invalid username/password supplied", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getHttpStatus());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(username);
        verify(jwtTokenProvider, never()).createToken(anyString(), any());
    }

    @Test
    void signup_ShouldReturnToken_WhenUsernameIsNew() {
        // Given
        String expectedToken = "jwt-token-123";
        String encodedPassword = "encodedpassword";
        
        when(userRepository.existsByUsername(appUser.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(appUser.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(appUser)).thenReturn(appUser);
        when(jwtTokenProvider.createToken(appUser.getUsername(), appUser.getAppUserRoles())).thenReturn(expectedToken);
        
        // When
        String actualToken = userService.signup(appUser);
        
        // Then
        assertEquals(expectedToken, actualToken);
        assertEquals(encodedPassword, appUser.getPassword());
        verify(userRepository, times(1)).existsByUsername(appUser.getUsername());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(appUser);
        verify(jwtTokenProvider, times(1)).createToken(appUser.getUsername(), appUser.getAppUserRoles());
    }

    @Test
    void signup_ShouldThrowCustomException_WhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(appUser.getUsername())).thenReturn(true);
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.signup(appUser);
        });
        
        assertEquals("Username is already in use", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getHttpStatus());
        verify(userRepository, times(1)).existsByUsername(appUser.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(AppUser.class));
        verify(jwtTokenProvider, never()).createToken(anyString(), any());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // Given
        String username = "testuser";
        
        // When
        userService.delete(username);
        
        // Then
        verify(userRepository, times(1)).deleteByUsername(username);
    }

    @Test
    void search_ShouldReturnAppUser_WhenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(appUser);
        
        // When
        AppUser result = userService.search(username);
        
        // Then
        assertEquals(appUser, result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void search_ShouldThrowCustomException_WhenUserDoesNotExist() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(null);
        
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.search(username);
        });
        
        assertEquals("The user doesn't exist", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void whoami_ShouldReturnAppUser_WhenTokenIsValid() {
        // Given
        String token = "jwt-token-123";
        String username = "testuser";
        
        when(jwtTokenProvider.resolveToken(httpServletRequest)).thenReturn(token);
        when(jwtTokenProvider.getUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(appUser);
        
        // When
        AppUser result = userService.whoami(httpServletRequest);
        
        // Then
        assertEquals(appUser, result);
        verify(jwtTokenProvider, times(1)).resolveToken(httpServletRequest);
        verify(jwtTokenProvider, times(1)).getUsername(token);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void refresh_ShouldReturnNewToken() {
        // Given
        String username = "testuser";
        String expectedToken = "new-jwt-token-456";
        
        when(userRepository.findByUsername(username)).thenReturn(appUser);
        when(jwtTokenProvider.createToken(username, appUser.getAppUserRoles())).thenReturn(expectedToken);
        
        // When
        String actualToken = userService.refresh(username);
        
        // Then
        assertEquals(expectedToken, actualToken);
        verify(userRepository, times(1)).findByUsername(username);
        verify(jwtTokenProvider, times(1)).createToken(username, appUser.getAppUserRoles());
    }
}
