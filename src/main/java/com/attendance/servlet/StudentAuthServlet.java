
package com.attendance.servlet;

import com.attendance.util.DBConnection;
import com.attendance.util.PasswordUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = {
    "/api/student-auth/register",
    "/api/student-auth/login",
    "/api/student-auth/logout"
})
public class StudentAuthServlet extends HttpServlet {

    private void json(HttpServletResponse res, int status, String body)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(body);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if (path.endsWith("/logout")) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            json(res, 200, "{\"message\":\"Logged out\"}");
            return;
        }

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            json(res, 400, "{\"error\":\"Email and password are required\"}");
            return;
        }

        email = email.trim();

        try (Connection con = DBConnection.getConnection()) {

            if (path.endsWith("/register")) {
                String roll = req.getParameter("rollNumber");

                if (roll == null || roll.isBlank() || password.length() < 8) {
                    json(res, 400, "{\"error\":\"Enter your roll number and a password of at least 8 characters\"}");
                    return;
                }

                int studentId;

                String find = """
                    SELECT student_id FROM students
                    WHERE roll_number = ? AND LOWER(email) = LOWER(?)
                    """;

                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setString(1, roll.trim());
                    ps.setString(2, email);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            json(res, 400,
                                "{\"error\":\"Roll number and email do not match an existing student\"}");
                            return;
                        }
                        studentId = rs.getInt("student_id");
                    }
                }

                String insert = """
                    INSERT INTO student_accounts(student_id, password_hash)
                    VALUES (?, ?)
                    """;

                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, PasswordUtil.hash(password));
                    ps.executeUpdate();
                }

                json(res, 201,
                    "{\"message\":\"Account created. You can now log in.\"}");
                return;
            }

            String sql = """
                SELECT s.student_id, s.full_name, sa.password_hash
                FROM students s
                JOIN student_accounts sa ON sa.student_id = s.student_id
                WHERE LOWER(s.email) = LOWER(?)
                """;

            int studentId;
            String fullName;
            String storedHash;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        json(res, 401,
                            "{\"error\":\"Invalid email or password\"}");
                        return;
                    }

                    studentId = rs.getInt("student_id");
                    fullName = rs.getString("full_name");
                    storedHash = rs.getString("password_hash");
                }
            }

            if (!PasswordUtil.verify(password, storedHash)) {
                json(res, 401,
                    "{\"error\":\"Invalid email or password\"}");
                return;
            }

            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();

            HttpSession session = req.getSession(true);
            req.changeSessionId();
            session.setAttribute("role", "STUDENT");
            session.setAttribute("studentId", studentId);
            session.setAttribute("studentName", fullName);
            session.setMaxInactiveInterval(20 * 60);

            json(res, 200, "{\"message\":\"Login successful\"}");

        } catch (java.security.GeneralSecurityException e) {
            log("Password hashing/verification failed", e);
            json(res, 500, "{\"error\":\"Authentication service failed\"}");
        } catch (SQLException e) {
            log("Student authentication failed", e);
            if ("23000".equals(e.getSQLState())) {
                json(res, 409,
                    "{\"error\":\"An account already exists for this student\"}");
            } else {
                json(res, 500, "{\"error\":\"Database operation failed\"}");
            }
        }
    }
}
