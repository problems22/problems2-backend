package org.example.problems2backend.service;

import lombok.RequiredArgsConstructor;
import org.example.problems2backend.exceptions.*;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.UserRepository;
import org.example.problems2backend.responses.AuthRes;
import org.example.problems2backend.responses.UserProfileRes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthRes register(String username, String password) {

        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (password == null)
            throw new InvalidPasswordFormatException("password can't be null");


        if (username.isBlank())
            throw new InvalidUsernameFormatException("username can't be blank");

        if (username.length() > 20)
            throw new InvalidUsernameFormatException("maximum 20 characters for username");

        if (userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username already taken");

        validatePasswordFormat(password);

        User user = User
                .builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password)) // encoded password
                .avatar("https://api.dicebear.com/8.x/pixel-art/png?seed=" + username + password) // randomly generated with seed
                .build();

        userRepository.save(user);

        // generate jwt for the user
        return AuthRes
                .builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();

    }


    public void validatePasswordFormat(String password)
    {
        // validate password format
        String password_regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
        if (!Pattern.compile(password_regex).matcher(password).matches()) {
            // Determine which requirement failed and throw appropriate message
            if (!password.matches(".*[A-Z].*"))
                throw new InvalidPasswordFormatException("at least one uppercase character");
            if (!password.matches(".*[a-z].*"))
                throw new InvalidPasswordFormatException("at least one lowercase letter");
            if (!password.matches(".*\\d.*"))
                throw new InvalidPasswordFormatException("at least one digit");
            if (password.length() < 8)
                throw new InvalidPasswordFormatException("minimum 8 characters");
            if (password.length() > 20)
                throw new InvalidPasswordFormatException("password too long");
        }
    }


    public AuthRes login(String username, String password) {

        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (password == null)
            throw new InvalidPasswordFormatException("password can't be null");


        if (username.isBlank())
            throw new InvalidUsernameFormatException("username can't be blank");

        if (!userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username doesn't exist");

        User user = userRepository.findByUsername(username).get();

        // verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new InvalidCredentialsException("wrong password");

        return AuthRes
                .builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();


    }


    public AuthRes refreshToken(String accessToken, String refreshToken)
    {
        if (accessToken == null)
            throw new InvalidAccessTokenException("access token can't be null");

        if (refreshToken == null)
            throw new InvalidRefreshTokenException("refresh token can't be null");

        String username = jwtService.extractUsername(refreshToken, false);

        if (username == null) {
            throw new InvalidRefreshTokenException("is invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("username not found"));

        if (jwtService.isTokenExpired(refreshToken, false))
        {
            throw new InvalidRefreshTokenException("refresh token has expired, please login again");
        }

        if (!jwtService.isTokenValid(refreshToken, user, false))
        {
            throw new InvalidRefreshTokenException("token not valid");
        }

        if (jwtService.isTokenValid(accessToken, user, true)) {

            return AuthRes.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        else
        {
            return AuthRes.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .refreshToken(refreshToken)
                    .build();
        }

    }


    public void changePassword(String username, String oldPassword, String newPassword)
    {
        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (oldPassword == null)
            throw new InvalidPasswordFormatException("old password can't be null");

        if (newPassword == null)
            throw new InvalidPasswordFormatException("new password can't be null");


        if (!userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username doesn't exist");

        String passwordHash = userRepository.findPasswordHashByUsername(username).get().getPasswordHash();

        System.out.println(passwordHash);

        // verify password
        if (!passwordEncoder.matches(oldPassword, passwordHash))
            throw new InvalidCredentialsException("wrong password");

        validatePasswordFormat(newPassword);

        userRepository.updatePasswordHashByUsername(username, passwordEncoder.encode(newPassword));

    }


    public UserProfileRes getProfile(User user) {
        return null;
    }
}
