package org.example.problems2backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.problems2backend.exceptions.InvalidCredentialsException;
import org.example.problems2backend.exceptions.InvalidPasswordFormatException;
import org.example.problems2backend.exceptions.InvalidRefreshTokenException;
import org.example.problems2backend.exceptions.InvalidUsernameFormatException;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.UserRepository;
import org.example.problems2backend.requests.AuthReq;
import org.example.problems2backend.requests.PasswordChangeRequest;
import org.example.problems2backend.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        // Delete specific test users
        List<String> testUsernames = Arrays.asList(
                "testUser1", "testUser2", "testUser3", "testUser4", "testUser5",
                "testUser6", "loginTest7", "loginTest8", "loginTest9", "refreshTest",
                "passwordChangeTest", "passwordChangeTest2", "passwordChangeTest3"
        );

        for (String username : testUsernames) {
            userRepository.deleteByUsername(username);
        }
    }


    private Cookie[] generateCookies(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        return new Cookie[] {
                new Cookie("access_token", jwtService.generateAccessToken(user)),
                new Cookie("refresh_token", jwtService.generateRefreshToken(user))
        };
    }

    @Test
    void shouldRegisterUserAndReturnTokensWithStatusCreated() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser1")
                .password("testPassword123A")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("access_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();

        String accessTokenCookie = result.getResponse().getCookie("access_token").getValue();
        String refreshTokenCookie = result.getResponse().getCookie("refresh_token").getValue();

        assertNotNull(accessTokenCookie);
        assertNotNull(refreshTokenCookie);
        assertEquals(3, accessTokenCookie.split("\\.").length);
        assertTrue(accessTokenCookie.startsWith("ey"));
        assertEquals(3, refreshTokenCookie.split("\\.").length);
        assertTrue(refreshTokenCookie.startsWith("ey"));

    }

    @Test
    void shouldFailRegistrationWhenUsernameExists() throws Exception {
        // First registration
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser2")
                .password("testPassword123A")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isCreated());

        // Attempt to register with same username
        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidCredentialsException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username already taken"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRegistrationWhenPasswordMissingUppercase() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser3")
                .password("testpassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidPasswordFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("password must contain at least one uppercase character"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRegistrationWhenPasswordMissingLowercase() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser4")
                .password("TESTPASSWORD123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidPasswordFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("password must contain at least one lowercase letter"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRegistrationWhenPasswordMissingDigit() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser5")
                .password("TestPassword")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidPasswordFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("password must contain at least one digit"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRegistrationWhenPasswordTooShort() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("testUser6")
                .password("Test1")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidPasswordFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("password must contain minimum 8 characters"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRegistrationWhenUsernameIsBlank() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("               ")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username can't be blank"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldFailRegistrationWhenUsernameTooLong() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("thisUsernameIsWayTooLongAndShouldNotBeAccepted")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("maximum 20 characters for username"))
                .andExpect(jsonPath("$.timestamp").exists());
    }



    @Test
    void shouldFailRegistrationWhenUsernameIsBlankAndPasswordInvalid() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("")
                .password("test")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username can't be blank"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldFailRegistrationWhenUsernameTooLongAndPasswordInvalid() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("thisUsernameIsWayTooLongAndShouldNotBeAccepted")
                .password("test")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("maximum 20 characters for username"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldFailRegistrationWhenUsernameAndPasswordEmpty() throws Exception {
        AuthReq authReq = AuthReq
                .builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username can't be blank"))
                .andExpect(jsonPath("$.timestamp").exists());
    }



    @Test
    void shouldLoginSuccessfullyAndReturnTokens() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq
                .builder()
                .username("loginTest7")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Then try to login
        AuthReq loginReq = AuthReq
                .builder()
                .username("loginTest7")
                .password("TestPassword123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("access_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();

        String accessTokenCookie = result.getResponse().getCookie("access_token").getValue();
        String refreshTokenCookie = result.getResponse().getCookie("refresh_token").getValue();

        assertNotNull(accessTokenCookie);
        assertNotNull(refreshTokenCookie);
        assertEquals(3, accessTokenCookie.split("\\.").length);
        assertTrue(accessTokenCookie.startsWith("ey"));
        assertEquals(3, refreshTokenCookie.split("\\.").length);
        assertTrue(refreshTokenCookie.startsWith("ey"));
    }



    @Test
    void shouldFailLoginWhenUsernameDoesntExist() throws Exception {
        AuthReq loginReq = AuthReq
                .builder()
                .username("loginTest8")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidCredentialsException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username doesn't exist"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldFailLoginWhenPasswordIsWrong() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq
                .builder()
                .username("loginTest9")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Then try to login with wrong password
        AuthReq loginReq = AuthReq
                .builder()
                .username("loginTest9")
                .password("WrongPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidCredentialsException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("wrong password"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldFailLoginWhenCredentialsEmpty() throws Exception {
        AuthReq loginReq = AuthReq
                .builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/api/users/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidUsernameFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("username can't be blank"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq.builder()
                .username("refreshTest")
                .password("TestPassword123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String initialRefreshToken = registerResult.getResponse().getCookie("refresh_token").getValue();

        // Perform token refresh with cookie
        MvcResult refreshResult = mockMvc.perform(post("/api/users/user/refresh-token")
                        .cookie(generateCookies(registerReq.getUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("access_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();

        String newAccessTokenCookie = refreshResult.getResponse().getCookie("access_token").getValue();
        String newRefreshTokenCookie = refreshResult.getResponse().getCookie("refresh_token").getValue();

        assertNotNull(newAccessTokenCookie);
        assertNotNull(newRefreshTokenCookie);
        assertEquals(initialRefreshToken, newRefreshTokenCookie);
    }

    @Test
    void shouldFailRefreshTokenWithMissingCookie() throws Exception {
        User user = User
                .builder()
                .username("noone")
                .passwordHash("not even saved in db")
                .build();

        mockMvc.perform(post("/api/users/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidRefreshTokenException.class.getSimpleName()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailRefreshTokenWithInvalidToken() throws Exception {
        User user = User  // fake user
                .builder()
                .username("fakeUsername")
                .passwordHash("fakePasswordHash")
                .build();

        String invalidToken = jwtService.generateRefreshToken(user);

        mockMvc.perform(post("/api/users/user/refresh-token")
                        .cookie(new Cookie[] {
                                new Cookie("refresh_token", invalidToken),
                                new Cookie("access_token", jwtService.generateAccessToken(user))
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidRefreshTokenException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq.builder()
                .username("passwordChangeTest")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Change password
        PasswordChangeRequest passwordChangeRequest = PasswordChangeRequest.builder()
                .username("passwordChangeTest")
                .oldPassword("TestPassword123")
                .newPassword("NewTestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isNoContent());

        // Try to login with new password
        AuthReq loginReq = AuthReq.builder()
                .username("passwordChangeTest")
                .password("NewTestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailPasswordChangeWithWrongOldPassword() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq.builder()
                .username("passwordChangeTest2")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Attempt to change password with wrong old password
        PasswordChangeRequest passwordChangeRequest = PasswordChangeRequest.builder()
                .username("passwordChangeTest2")
                .oldPassword("WrongPassword123")
                .newPassword("NewTestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(InvalidCredentialsException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").value("wrong password"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFailPasswordChangeWithInvalidNewPassword() throws Exception {
        // First register a user
        AuthReq registerReq = AuthReq.builder()
                .username("passwordChangeTest3")
                .password("TestPassword123")
                .build();

        mockMvc.perform(post("/api/users/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Attempt to change password with invalid new password
        PasswordChangeRequest passwordChangeRequest = PasswordChangeRequest.builder()
                .username("passwordChangeTest3")
                .oldPassword("TestPassword123")
                .newPassword("weak")
                .build();

        mockMvc.perform(post("/api/users/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(InvalidPasswordFormatException.class.getSimpleName()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.timestamp").exists());
    }


}