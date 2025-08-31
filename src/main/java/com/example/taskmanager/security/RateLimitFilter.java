package com.example.taskmanager.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RateLimitFilter implements Filter {

    private final long WINDOW_MS;
    private final int MAX_REQUESTS;

    public RateLimitFilter() {
        // You can override via env/system props for testing:
        // RATELIMIT_WINDOW_MS, RATELIMIT_MAX
        this.WINDOW_MS = Long.getLong("RATELIMIT_WINDOW_MS",
                Long.parseLong(System.getenv().getOrDefault("RATELIMIT_WINDOW_MS",
                        String.valueOf(TimeUnit.MINUTES.toMillis(1)))));
        this.MAX_REQUESTS = Integer.getInteger("RATELIMIT_MAX",
                Integer.parseInt(System.getenv().getOrDefault("RATELIMIT_MAX", "100")));
        log.info("RateLimitFilter active: windowMs={} max={}", WINDOW_MS, MAX_REQUESTS);
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse resp = (HttpServletResponse) res;
        String ip = http.getRemoteAddr();

        long now = System.currentTimeMillis();
        Counter c = counters.compute(ip, (k, old) -> {
            if (old == null || now - old.start > WINDOW_MS) return new Counter(1, now);
            old.count++;
            return old;
        });

        if (c.count > MAX_REQUESTS) {
            resp.setStatus(429);
            resp.setHeader("Retry-After", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(WINDOW_MS)));
            resp.getWriter().write("Too Many Requests");
            return;
        }

        chain.doFilter(req, res);
    }

    private static class Counter {
        int count; long start;
        Counter(int c, long s) { this.count = c; this.start = s; }
    }
}