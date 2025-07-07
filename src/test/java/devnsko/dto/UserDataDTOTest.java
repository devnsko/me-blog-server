package devnsko.dto;

import devnsko.model.AppUserRole;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDataDTOTest {

    @Test
    void userDataDTO_ShouldSetAndGetProperties() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        List<AppUserRole> roles = Arrays.asList(AppUserRole.ROLE_CLIENT);

        // When
        UserDataDTO userDataDTO = new UserDataDTO();
        userDataDTO.setUsername(username);
        userDataDTO.setEmail(email);
        userDataDTO.setPassword(password);
        userDataDTO.setAppUserRoles(roles);

        // Then
        assertEquals(username, userDataDTO.getUsername());
        assertEquals(email, userDataDTO.getEmail());
        assertEquals(password, userDataDTO.getPassword());
        assertEquals(roles, userDataDTO.getAppUserRoles());
    }

    @Test
    void userDataDTO_ShouldBeEqualWhenPropertiesAreEqual() {
        // Given
        UserDataDTO userDataDTO1 = new UserDataDTO();
        userDataDTO1.setUsername("testuser");
        userDataDTO1.setEmail("test@example.com");
        userDataDTO1.setPassword("password123");
        userDataDTO1.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        UserDataDTO userDataDTO2 = new UserDataDTO();
        userDataDTO2.setUsername("testuser");
        userDataDTO2.setEmail("test@example.com");
        userDataDTO2.setPassword("password123");
        userDataDTO2.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        // When & Then
        assertEquals(userDataDTO1, userDataDTO2);
        assertEquals(userDataDTO1.hashCode(), userDataDTO2.hashCode());
    }

    @Test
    void userDataDTO_ShouldNotBeEqualWhenPropertiesAreDifferent() {
        // Given
        UserDataDTO userDataDTO1 = new UserDataDTO();
        userDataDTO1.setUsername("testuser1");
        userDataDTO1.setEmail("test1@example.com");

        UserDataDTO userDataDTO2 = new UserDataDTO();
        userDataDTO2.setUsername("testuser2");
        userDataDTO2.setEmail("test2@example.com");

        // When & Then
        assertNotEquals(userDataDTO1, userDataDTO2);
    }
}
