
package com.attendance.dao;

import com.attendance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.YearMonth;

public class ReportDAO {

    public List<Map<String, Object>> getReport(
            int subjectId, String mode, String period)
            throws SQLException {

        if (subjectId <= 0 || period == null) {
            throw new IllegalArgumentException("Invalid report parameters");
        }

        LocalDate start;
        LocalDate end;

        if ("date".equals(mode)) {
            start = LocalDate.parse(period);
            end = start.plusDays(1);
        } else if ("month".equals(mode)) {
            YearMonth month = YearMonth.parse(period);
            start = month.atDay(1);
            end = month.plusMonths(1).atDay(1);
        } else {
            throw new IllegalArgumentException("Invalid report mode");
        }

        String sql = """
            SELECT
                s.student_id,
                s.roll_number,
                s.full_name,
                SUM(CASE WHEN a.status = 'PRESENT'
                         THEN 1 ELSE 0 END) AS present_count,
                SUM(CASE WHEN a.status = 'ABSENT'
                         THEN 1 ELSE 0 END) AS absent_count,
                SUM(CASE WHEN a.status = 'LATE'
                         THEN 1 ELSE 0 END) AS late_count,
                COUNT(*) AS total_count,
                ROUND(
                    100.0 * SUM(
                        CASE WHEN a.status IN ('PRESENT', 'LATE')
                             THEN 1 ELSE 0 END
                    ) / COUNT(*), 2
                ) AS attendance_percentage
            FROM students s
            JOIN attendance a
              ON a.student_id = s.student_id
            WHERE a.subject_id = ?
              AND a.attendance_date >= ?
              AND a.attendance_date < ?
            GROUP BY s.student_id, s.roll_number, s.full_name
            ORDER BY s.roll_number
            """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subjectId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("studentId", rs.getInt("student_id"));
                    row.put("rollNumber", rs.getString("roll_number"));
                    row.put("fullName", rs.getString("full_name"));
                    row.put("present", rs.getInt("present_count"));
                    row.put("absent", rs.getInt("absent_count"));
                    row.put("late", rs.getInt("late_count"));
                    row.put("total", rs.getInt("total_count"));
                    row.put("percentage",
                            rs.getDouble("attendance_percentage"));

                    list.add(row);
                }
            }
        }

        return list;
    }
}
