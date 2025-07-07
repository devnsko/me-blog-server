package devnsko.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSignInDTOTest {

    @Test
    void userSignInDTO_ShouldSetAndGetProperties() {
        // Given
        String username = "testuser";
        String password = "password123";

        // When
        UserSignInDTO userSignInDTO = new UserSignInDTO();
        userSignInDTO.setUsername(username);
        userSignInDTO.setPassword(password);

        // Then
        assertEquals(username, userSignInDTO.getUsername());
        assertEquals(password, userSignInDTO.getPassword());
    }

    @Test
    void userSignInDTO_ShouldBeEqualWhenPropertiesAreEqual() {
        // Given
        UserSignInDTO userSignInDTO1 = new UserSignInDTO();
        userSignInDTO1.setUsername("testuser");
        userSignInDTO1.setPassword("password123");

        UserSignInDTO userSignInDTO2 = new UserSignInDTO();
        userSignInDTO2.setUsername("testuser");
        userSignInDTO2.setPassword("password123");

        // When & Then
        assertEquals(userSignInDTO1, userSignInDTO2);
        assertEquals(userSignInDTO1.hashCode(), userSignInDTO2.hashCode());
    }

    @Test
    void userSignInDTO_ShouldNotBeEqualWhenPropertiesAreDifferent() {
        // Given
        UserSignInDTO userSignInDTO1 = new UserSignInDTO();
        userSignInDTO1.setUsername("testuser1");
        userSignInDTO1.setPassword("password123");

        UserSignInDTO userSignInDTO2 = new UserSignInDTO();
        userSignInDTO2.setUsername("testuser2");
        userSignInDTO2.setPassword("password123");

        // When & Then
        assertNotEquals(userSignInDTO1, userSignInDTO2);
    }
}
