
package com.attendance.servlet;

import com.attendance.dao.AttendanceDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/api/attendance")
public class AttendanceServlet extends HttpServlet {

    private final AttendanceDAO dao = new AttendanceDAO();

    private void json(HttpServletResponse res, int status, String body)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(body);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try {
            int subjectId = Integer.parseInt(req.getParameter("subjectId"));
            String date = req.getParameter("date");
            if (date == null || date.isBlank()) {
                date = LocalDate.now().toString();
            }

            StringBuilder out = new StringBuilder("[");
            for (Map<String, Object> s :
                    dao.getStudentsForAttendance(subjectId, date)) {
                if (out.length() > 1) out.append(",");
                out.append("{\"studentId\":").append(s.get("studentId"))
                   .append(",\"rollNumber\":\"")
                   .append(esc((String) s.get("rollNumber")))
                   .append("\",\"fullName\":\"")
                   .append(esc((String) s.get("fullName")))
                   .append("\",\"status\":");
                if (s.get("status") == null) out.append("null");
                else out.append("\"").append(s.get("status")).append("\"");
                out.append(",\"remarks\":\"")
                   .append(esc((String) s.get("remarks"))).append("\"}");
            }
            json(res, 200, out.append("]").toString());
        } catch (Exception e) {
            log("Unable to load attendance", e);
            json(res, 400, "{\"error\":\"Invalid subject or date\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            int studentId = Integer.parseInt(req.getParameter("studentId"));
            int subjectId = Integer.parseInt(req.getParameter("subjectId"));
            String date = req.getParameter("date");
            String status = req.getParameter("status");
            String remarks = req.getParameter("remarks");

            if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")
                    || !Set.of("PRESENT", "ABSENT", "LATE").contains(status)) {
                json(res, 400, "{\"error\":\"Invalid date or attendance status\"}");
                return;
            }

            dao.markAttendance(studentId, subjectId, date, status, remarks);
            json(res, 200, "{\"message\":\"Attendance saved\"}");
        } catch (Exception e) {
            log("Unable to save attendance", e);
            json(res, 400, "{\"error\":\"Could not save attendance\"}");
        }
    }
}
