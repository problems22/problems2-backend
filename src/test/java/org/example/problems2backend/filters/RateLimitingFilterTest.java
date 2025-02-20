package org.example.problems2backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.problems2backend.exceptions.InvalidAccessTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter rateLimitingFilter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        rateLimitingFilter = new RateLimitingFilter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void whenNoCookies_thenThrowException() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(InvalidAccessTokenException.class, () ->
                rateLimitingFilter.doFilter(request, response, filterChain)
        );

        verifyNoInteractions(filterChain);
    }

    @Test
    void whenNoAccessTokenCookie_thenThrowException() {
        Cookie[] cookies = new Cookie[]{new Cookie("other_cookie", "value")};
        when(request.getCookies()).thenReturn(cookies);

        assertThrows(InvalidAccessTokenException.class, () ->
                rateLimitingFilter.doFilter(request, response, filterChain)
        );

        verifyNoInteractions(filterChain);
    }

    @Test
    void whenWithinRateLimit_thenAllowRequest() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("access_token", "test-token")};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenExceedRateLimit_thenBlock() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("access_token", "test-token")};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Exceed rate limit
        for (int i = 0; i <= 30; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertTrue(stringWriter.toString().contains("too many requests"));
    }

    @Test
    void whenRequestFromProxy_thenUseXForwardedForHeader() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("access_token", "test-token")};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}