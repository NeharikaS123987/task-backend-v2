package com.example.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    // IMPORTANT: do NOT include /api/auth/me here; it should be authenticated
    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/v3/api-docs", "/v3/api-docs/**",
            "/swagger-ui/**", "/swagger-ui.html",
            "/api/auth/signup", "/api/auth/login",
            "/actuator/health", "/actuator/info",
            "/error", "/favicon.ico"
    );

    private final JwtService jwt;
    private final UserDetailsServiceImpl uds;

    private boolean isPublic(HttpServletRequest req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String uri = req.getRequestURI();
        for (String p : PUBLIC_PATTERNS) {
            if (MATCHER.match(p, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (isPublic(req)) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (token != null) {
            try {
                String username = jwt.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = uds.loadUserByUsername(username);
                    if (jwt.isValid(token, userDetails)) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                // Invalid/expired token -> leave context unauthenticated; controller will 401
            }
        }

        chain.doFilter(req, res);
    }
}