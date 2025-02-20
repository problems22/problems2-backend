package org.example.problems2backend.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    // Using ConcurrentHashMap to handle concurrent requests safely
    private final Map<String, TokenBucket> requestCountPerKey = new ConcurrentHashMap<>();

    private static class TokenBucket {
        private final AtomicInteger tokens;
        private final long lastResetTime;

        public TokenBucket() {
            this.tokens = new AtomicInteger(MAX_REQUESTS_PER_MINUTE);
            this.lastResetTime = System.currentTimeMillis();
        }

        public boolean tryConsume() {
            return tokens.decrementAndGet() >= 0;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastResetTime > 60000;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String key;
        Cookie[] cookies = httpServletRequest.getCookies();

        // Determine the rate limiting key based on whether the request is authenticated
        if (cookies == null || Arrays.stream(cookies).noneMatch(c -> "access_token".equals(c.getName()))) {
            // For unauthenticated requests (like login), use IP address only
            key = getClientIpAddress(httpServletRequest);
        } else {
            // For authenticated requests, use combination of access token and IP
            String accessToken = Arrays.stream(cookies)
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(""); // Handle empty token gracefully
            key = accessToken + ":" + getClientIpAddress(httpServletRequest);
        }

        // Get or create bucket for this key
        TokenBucket bucket = requestCountPerKey.compute(key, (k, v) -> {
            if (v == null || v.isExpired()) {
                return new TokenBucket();
            }
            return v;
        });

        // Check if request can be processed within rate limit
        if (!bucket.tryConsume()) {
            httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpServletResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredBuckets() {
        requestCountPerKey.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}