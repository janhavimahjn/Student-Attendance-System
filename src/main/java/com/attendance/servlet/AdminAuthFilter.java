
package com.attendance.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/api/*")
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Include pathInfo: important for /api/student/* mappings.
        String path = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if (pathInfo != null) {
            path += pathInfo;
        }

        // Public authentication endpoints.
        if ("/api/login".equals(path)
                || "/api/logout".equals(path)
                || path.startsWith("/api/student-auth/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null) {
            unauthorized(res);
            return;
        }

        Object role = session.getAttribute("role");

        // Student APIs: require a student session.
        if (path.startsWith("/api/student/")) {
            if ("STUDENT".equals(role)
                    && session.getAttribute("studentId") instanceof Integer) {
                chain.doFilter(request, response);
            } else {
                unauthorized(res);
            }
            return;
        }

        // Existing admin session.
        if (session.getAttribute("admin") != null
                || "ADMIN".equals(role)) {
            chain.doFilter(request, response);
        } else {
            unauthorized(res);
        }
    }

    private void unauthorized(HttpServletResponse res)
            throws IOException {

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(
            "{\"error\":\"Please log in with the correct account\"}"
        );
    }
}
