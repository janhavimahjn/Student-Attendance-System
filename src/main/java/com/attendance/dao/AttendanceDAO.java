
package com.attendance.dao;

import com.attendance.util.DBConnection;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class AttendanceDAO {

    public List<Map<String, Object>> getStudentsForAttendance(
            int subjectId, String date) throws SQLException {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT s.student_id, s.roll_number, s.full_name,
                   a.status, a.remarks
            FROM students s
            LEFT JOIN attendance a
              ON a.student_id = s.student_id
             AND a.subject_id = ?
             AND a.attendance_date = ?
            ORDER BY s.roll_number
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("studentId", rs.getInt("student_id"));
                    row.put("rollNumber", rs.getString("roll_number"));
                    row.put("fullName", rs.getString("full_name"));
                    row.put("status", rs.getString("status"));
                    row.put("remarks", rs.getString("remarks"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public void markAttendance(int studentId, int subjectId,
                               String date, String status,
                               String remarks) throws SQLException {

        String sql = """
            INSERT INTO attendance
                (student_id, subject_id, attendance_date, status, remarks)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                remarks = VALUES(remarks)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, status);
            ps.setString(5, remarks);
            ps.executeUpdate();
        }
    }
}
