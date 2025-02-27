package org.example.problems2backend.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.problems2backend.exceptions.InvalidRefreshTokenException;
import org.example.problems2backend.models.User;
import org.example.problems2backend.requests.AuthReq;
import org.example.problems2backend.responses.LeaderboardRes;
import org.example.problems2backend.requests.PasswordChangeReq;
import org.example.problems2backend.responses.AuthRes;
import org.example.problems2backend.responses.UserProfileRes;
import org.example.problems2backend.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    private ResponseEntity<Void> buildCookieResponse(AuthRes authRes, HttpStatus status) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", authRes.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", authRes.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/users/user/refresh-token")
                .maxAge(Duration.ofDays(5))
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/user/register")
    public ResponseEntity<Void> register(@RequestBody AuthReq authReq) {
        AuthRes authRes = userService.register(authReq.getUsername(), authReq.getPassword());
        return buildCookieResponse(authRes, HttpStatus.CREATED);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Void> login(@RequestBody AuthReq authReq) {
        AuthRes authRes = userService.login(authReq.getUsername(), authReq.getPassword());
        return buildCookieResponse(authRes, HttpStatus.OK);
    }

    @PostMapping("/user/refresh-token")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidRefreshTokenException("no cookies present in request");
        }

        String accessToken = null;
        String refreshToken = null;

        for (Cookie cookie : cookies) {
            switch (cookie.getName()) {
                case "access_token":
                    accessToken = cookie.getValue();
                    break;
                case "refresh_token":
                    refreshToken = cookie.getValue();
                    break;
            }
            if (accessToken != null && refreshToken != null) {
                break;
            }
        }

        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("refresh token not found");
        }

        AuthRes authRes = userService.refreshToken(accessToken, refreshToken);
        return buildCookieResponse(authRes, HttpStatus.OK);
    }

    @PostMapping("/user/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeReq passwordChangeRequest) {
        userService.changePassword(passwordChangeRequest.getUsername(),
                passwordChangeRequest.getOldPassword(),
                passwordChangeRequest.getNewPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/users/user/refresh-token")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }


    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileRes> getProfile(@AuthenticationPrincipal User user)
    {
        UserProfileRes userProfileRes = userService.getUserProfile(user.getUsername());
        return new ResponseEntity<>(userProfileRes, HttpStatus.OK);
    }

    @GetMapping("/user/leaderboard")
    public ResponseEntity<LeaderboardRes> getLeaderboard(@AuthenticationPrincipal User user)
    {
        LeaderboardRes leaderboardRes = userService.getLeaderboard(user);
        return new ResponseEntity<>(leaderboardRes, HttpStatus.OK);
    }

    @GetMapping("/user/leaderboard/profile/{username}")
    public ResponseEntity<UserProfileRes> getUserLeaderboardProfile(@PathVariable String username)
    {
        UserProfileRes userProfileRes = userService.getUserProfile(username);
        userProfileRes.setRecentResults(null);
        return new ResponseEntity<>(userProfileRes, HttpStatus.OK);
    }

    @PostMapping("/admin/quiz/crate")
    public ResponseEntity<Void> createQuiz(@AuthenticationPrincipal User user)
    {
        return null;
    }
    @DeleteMapping("/admin/quiz/delete/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String quizId, @AuthenticationPrincipal User user)
    {
        return null;
    }

    @PutMapping("/admin/promote/user/{username}")
    public ResponseEntity<Void> promoteUser(@PathVariable String username, @AuthenticationPrincipal User user)
    {
        return null;
    }

    @PutMapping("/admin/demote/admin/{username}")
    public ResponseEntity<Void> demoteAdmin(@PathVariable String username, @AuthenticationPrincipal User user)
    {
        return null;
    }

    @PostMapping("/admin/ban/user/{username}")
    public ResponseEntity<Void> banUser(@PathVariable String username, @AuthenticationPrincipal User user)
    {
        return null;
    }









}