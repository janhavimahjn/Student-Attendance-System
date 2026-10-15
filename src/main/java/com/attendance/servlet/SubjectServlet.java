
package com.attendance.servlet;

import com.attendance.dao.SubjectDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/subjects")
public class SubjectServlet extends HttpServlet {

    private final SubjectDAO dao = new SubjectDAO();

    private void json(HttpServletResponse res, int status, String body)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(body);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try {
            StringBuilder out = new StringBuilder("[");
            for (Map<String, Object> s : dao.getAll()) {
                if (out.length() > 1) out.append(",");
                out.append("{\"subjectId\":").append(s.get("subjectId"))
                   .append(",\"subjectCode\":\"")
                   .append(esc((String) s.get("subjectCode")))
                   .append("\",\"subjectName\":\"")
                   .append(esc((String) s.get("subjectName")))
                   .append("\"}");
            }
            json(res, 200, out.append("]").toString());
        } catch (SQLException e) {
            log("Unable to load subjects", e);
            json(res, 500, "{\"error\":\"Unable to load subjects\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        req.setCharacterEncoding("UTF-8");

        try {
            String action = req.getParameter("action");

            if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("subjectId"));
                boolean ok = dao.delete(id);
                json(res, ok ? 200 : 404, ok
                    ? "{\"message\":\"Subject deleted\"}"
                    : "{\"error\":\"Subject not found\"}");
                return;
            }

            String code = req.getParameter("subjectCode");
            String name = req.getParameter("subjectName");

            if (code == null || code.isBlank()
                    || name == null || name.isBlank()) {
                json(res, 400, "{\"error\":\"Code and name are required\"}");
                return;
            }

            dao.add(code.trim(), name.trim());
            json(res, 201, "{\"message\":\"Subject added successfully\"}");

        } catch (NumberFormatException e) {
            json(res, 400, "{\"error\":\"Invalid subject ID\"}");
        } catch (SQLException e) {
            log("Subject operation failed", e);
            json(res, "23000".equals(e.getSQLState()) ? 409 : 500,
                "{\"error\":\"Subject operation failed; check for duplicate code or attendance references\"}");
        }
    }
}
