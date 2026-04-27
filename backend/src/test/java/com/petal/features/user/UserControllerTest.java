package com.petal.features.user;

import com.petal.features.user.dto.UserResponse;
import com.petal.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role("ROLE_BUYER")
                .build();
    }

    @Test
    void getCurrentUser_Success() {
        ResponseEntity<ApiResponse<UserResponse>> response = userController.getCurrentUser(mockUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User fetched successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        
        UserResponse userResponse = response.getBody().getData();
        assertEquals(mockUser.getId(), userResponse.getId());
        assertEquals(mockUser.getName(), userResponse.getName());
        assertEquals(mockUser.getEmail(), userResponse.getEmail());
        assertEquals(mockUser.getRole(), userResponse.getRole());
    }
}
