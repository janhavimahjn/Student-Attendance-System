
package com.attendance.servlet;

import com.attendance.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.YearMonth;
import java.util.regex.Pattern;

@WebServlet("/api/student/*")
public class StudentPortalServlet extends HttpServlet {

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void json(HttpServletResponse res, int status, String body)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(body);
    }

    private int studentId(HttpServletRequest req) {
        return (Integer) req.getSession(false).getAttribute("studentId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        String path = req.getPathInfo();

        try (Connection con = DBConnection.getConnection()) {

            if ("/profile".equals(path)) {
                String sql = """
                    SELECT student_id, roll_number, full_name, email, class_name
                    FROM students WHERE student_id = ?
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, studentId(req));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            json(res, 404, "{\"error\":\"Student not found\"}");
                            return;
                        }

                        json(res, 200,
                            "{\"studentId\":" + rs.getInt("student_id")
                            + ",\"rollNumber\":\"" + esc(rs.getString("roll_number"))
                            + "\",\"fullName\":\"" + esc(rs.getString("full_name"))
                            + "\",\"email\":\"" + esc(rs.getString("email"))
                            + "\",\"className\":\"" + esc(rs.getString("class_name"))
                            + "\"}");
                    }
                }
                return;
            }

            if ("/subjects".equals(path)) {
                String sql = """
                    SELECT subject_id, subject_code, subject_name
                    FROM subjects ORDER BY subject_name
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    StringBuilder out = new StringBuilder("[");
                    while (rs.next()) {
                        if (out.length() > 1) out.append(",");
                        out.append("{\"subjectId\":")
                           .append(rs.getInt("subject_id"))
                           .append(",\"subjectCode\":\"")
                           .append(esc(rs.getString("subject_code")))
                           .append("\",\"subjectName\":\"")
                           .append(esc(rs.getString("subject_name")))
                           .append("\"}");
                    }

                    json(res, 200, out.append("]").toString());
                }
                return;
            }

            if ("/attendance".equals(path)) {
                String sql = """
                    SELECT a.attendance_date, a.status, a.remarks,
                           s.subject_code, s.subject_name
                    FROM attendance a
                    JOIN subjects s ON s.subject_id = a.subject_id
                    WHERE a.student_id = ?
                    ORDER BY a.attendance_date DESC, s.subject_name
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, studentId(req));

                    try (ResultSet rs = ps.executeQuery()) {
                        StringBuilder out = new StringBuilder("[");
                        while (rs.next()) {
                            if (out.length() > 1) out.append(",");
                            out.append("{\"date\":\"")
                               .append(rs.getDate("attendance_date"))
                               .append("\",\"status\":\"")
                               .append(esc(rs.getString("status")))
                               .append("\",\"remarks\":\"")
                               .append(esc(rs.getString("remarks")))
                               .append("\",\"subjectCode\":\"")
                               .append(esc(rs.getString("subject_code")))
                               .append("\",\"subjectName\":\"")
                               .append(esc(rs.getString("subject_name")))
                               .append("\"}");
                        }

                        json(res, 200, out.append("]").toString());
                    }
                }
                return;
            }

            if ("/reports".equals(path)) {
                String month = req.getParameter("month");

                if (month == null || !Pattern.matches("\\d{4}-\\d{2}", month)) {
                    json(res, 400, "{\"error\":\"Month must be YYYY-MM\"}");
                    return;
                }

                YearMonth ym;
                try {
                    ym = YearMonth.parse(month);
                } catch (Exception e) {
                    json(res, 400, "{\"error\":\"Invalid month\"}");
                    return;
                }

                String sql = """
                    SELECT s.subject_code, s.subject_name,
                        SUM(CASE WHEN a.status = 'PRESENT'
                                 THEN 1 ELSE 0 END) AS present_count,
                        SUM(CASE WHEN a.status = 'ABSENT'
                                 THEN 1 ELSE 0 END) AS absent_count,
                        SUM(CASE WHEN a.status = 'LATE'
                                 THEN 1 ELSE 0 END) AS late_count,
                        COUNT(*) AS total_count,
                        ROUND(
                            100.0 * SUM(
                                CASE WHEN a.status IN ('PRESENT','LATE')
                                     THEN 1 ELSE 0 END
                            ) / COUNT(*), 2
                        ) AS percentage
                    FROM attendance a
                    JOIN subjects s ON s.subject_id = a.subject_id
                    WHERE a.student_id = ?
                      AND a.attendance_date >= ?
                      AND a.attendance_date < ?
                    GROUP BY s.subject_id, s.subject_code, s.subject_name
                    ORDER BY s.subject_name
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, studentId(req));
                    ps.setDate(2, java.sql.Date.valueOf(ym.atDay(1)));
                    ps.setDate(3, java.sql.Date.valueOf(
                        ym.plusMonths(1).atDay(1)));

                    try (ResultSet rs = ps.executeQuery()) {
                        StringBuilder out = new StringBuilder("[");
                        while (rs.next()) {
                            if (out.length() > 1) out.append(",");
                            out.append("{\"subjectCode\":\"")
                               .append(esc(rs.getString("subject_code")))
                               .append("\",\"subjectName\":\"")
                               .append(esc(rs.getString("subject_name")))
                               .append("\",\"present\":")
                               .append(rs.getInt("present_count"))
                               .append(",\"absent\":")
                               .append(rs.getInt("absent_count"))
                               .append(",\"late\":")
                               .append(rs.getInt("late_count"))
                               .append(",\"total\":")
                               .append(rs.getInt("total_count"))
                               .append(",\"percentage\":")
                               .append(rs.getBigDecimal("percentage"))
                               .append("}");
                        }

                        json(res, 200, out.append("]").toString());
                    }
                }
                return;
            }

            json(res, 404, "{\"error\":\"Student endpoint not found\"}");

        } catch (SQLException e) {
            log("Student portal database error", e);
            json(res, 500, "{\"error\":\"Unable to load student information\"}");
        }
    }
}
