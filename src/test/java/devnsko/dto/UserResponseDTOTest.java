package devnsko.dto;

import devnsko.model.AppUserRole;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    @Test
    void userResponseDTO_ShouldSetAndGetProperties() {
        // Given
        Integer id = 1;
        String username = "testuser";
        String email = "test@example.com";
        List<AppUserRole> roles = Arrays.asList(AppUserRole.ROLE_CLIENT);

        // When
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(id);
        userResponseDTO.setUsername(username);
        userResponseDTO.setEmail(email);
        userResponseDTO.setAppUserRoles(roles);

        // Then
        assertEquals(id, userResponseDTO.getId());
        assertEquals(username, userResponseDTO.getUsername());
        assertEquals(email, userResponseDTO.getEmail());
        assertEquals(roles, userResponseDTO.getAppUserRoles());
    }

    @Test
    void userResponseDTO_ShouldBeEqualWhenPropertiesAreEqual() {
        // Given
        UserResponseDTO userResponseDTO1 = new UserResponseDTO();
        userResponseDTO1.setId(1);
        userResponseDTO1.setUsername("testuser");
        userResponseDTO1.setEmail("test@example.com");
        userResponseDTO1.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        UserResponseDTO userResponseDTO2 = new UserResponseDTO();
        userResponseDTO2.setId(1);
        userResponseDTO2.setUsername("testuser");
        userResponseDTO2.setEmail("test@example.com");
        userResponseDTO2.setAppUserRoles(Arrays.asList(AppUserRole.ROLE_CLIENT));

        // When & Then
        assertEquals(userResponseDTO1, userResponseDTO2);
        assertEquals(userResponseDTO1.hashCode(), userResponseDTO2.hashCode());
    }

    @Test
    void userResponseDTO_ShouldNotBeEqualWhenPropertiesAreDifferent() {
        // Given
        UserResponseDTO userResponseDTO1 = new UserResponseDTO();
        userResponseDTO1.setId(1);
        userResponseDTO1.setUsername("testuser1");
        userResponseDTO1.setEmail("test1@example.com");

        UserResponseDTO userResponseDTO2 = new UserResponseDTO();
        userResponseDTO2.setId(2);
        userResponseDTO2.setUsername("testuser2");
        userResponseDTO2.setEmail("test2@example.com");

        // When & Then
        assertNotEquals(userResponseDTO1, userResponseDTO2);
    }
}
