
package com.attendance.servlet;

import com.attendance.dao.ReportDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/reports")
public class ReportServlet extends HttpServlet {

    private final ReportDAO dao = new ReportDAO();

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try {
            int subjectId = Integer.parseInt(req.getParameter("subjectId"));
            String mode = req.getParameter("mode");
            String period = req.getParameter("period");

            if (period == null
                    || ("date".equals(mode)
                        && !period.matches("\\d{4}-\\d{2}-\\d{2}"))
                    || ("month".equals(mode)
                        && !period.matches("\\d{4}-\\d{2}"))
                    || !Set.of("date", "month").contains(mode)) {
                res.setStatus(400);
                res.getWriter().write("{\"error\":\"Invalid report filters\"}");
                return;
            }

            StringBuilder out = new StringBuilder("[");
            for (Map<String, Object> r :
                    dao.getReport(subjectId, mode, period)) {
                if (out.length() > 1) out.append(",");
                out.append("{\"studentId\":").append(r.get("studentId"))
                   .append(",\"rollNumber\":\"")
                   .append(esc((String) r.get("rollNumber")))
                   .append("\",\"fullName\":\"")
                   .append(esc((String) r.get("fullName")))
                   .append("\",\"present\":").append(r.get("present"))
                   .append(",\"absent\":").append(r.get("absent"))
                   .append(",\"late\":").append(r.get("late"))
                   .append(",\"total\":").append(r.get("total"))
                   .append(",\"percentage\":")
                   .append(r.get("percentage") == null
                        ? "null" : r.get("percentage"))
                   .append("}");
            }

            res.getWriter().write(out.append("]").toString());

        } catch (IllegalArgumentException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"Invalid subject or period\"}");
        } catch (SQLException e) {
            log("Unable to generate attendance report", e);
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Unable to generate report\"}");
        }
    }
}
