
package com.attendance.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/login", "/api/logout"})
public class AdminServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();

        if ("/api/login".equals(path)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            // Demo credentials only. Replace with database-backed authentication.
            if ("admin".equals(username) && "Admin@123".equals(password)) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();

                HttpSession session = req.getSession(true);
                req.changeSessionId();
                session.setAttribute("admin", username);
                session.setMaxInactiveInterval(20 * 60);

                res.setStatus(200);
                res.getWriter().write("{\"message\":\"Login successful\"}");
            } else {
                res.setStatus(401);
                res.getWriter().write("{\"error\":\"Invalid username or password\"}");
            }
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();

        res.setStatus(200);
        res.getWriter().write("{\"message\":\"Logged out\"}");
    }
}
