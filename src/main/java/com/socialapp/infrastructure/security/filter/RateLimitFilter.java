package com.socialapp.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple sliding-window rate limiter.
 * Per-user limits configurable per endpoint pattern.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int  DEFAULT_MAX_REQUESTS = 60;
    private static final long WINDOW_MILLIS        = 60_000L; // 1 minute

    // key = "userId:endpoint-bucket" → [count, windowStart]
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String path   = request.getRequestURI();
        String method = request.getMethod();
        String bucket = resolveBucket(method, path);
        int    limit  = resolveLimit(bucket);

        String key = userId + ":" + bucket;
        long   now = Instant.now().toEpochMilli();

        long[] slot = counters.compute(key, (k, v) -> {
            if (v == null || now - v[1] > WINDOW_MILLIS) {
                return new long[]{ 1, now };
            }
            v[0]++;
            return v;
        });

        if (slot[0] > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many requests. Please slow down.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBucket(String method, String path) {
        if (path.contains("/like"))    return "like";
        if (path.contains("/comment")) return "comment";
        if (path.contains("/posts") && "POST".equals(method)) return "create-post";
        if (path.contains("/messages") && "POST".equals(method)) return "message";
        if (path.contains("/reports") && "POST".equals(method)) return "report";
        return "default";
    }

    private int resolveLimit(String bucket) {
        return switch (bucket) {
            case "like"        -> 100; // 100 likes/min
            case "comment"     -> 20;  // 20 comments/min
            case "create-post" -> 10;  // 10 posts/min
            case "message"     -> 60;  // 60 messages/min
            case "report"      -> 5;   // 5 reports/min
            default            -> DEFAULT_MAX_REQUESTS;
        };
    }
}