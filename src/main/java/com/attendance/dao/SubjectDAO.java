
package com.attendance.dao;

import com.attendance.util.DBConnection;
import java.sql.*;
import java.util.*;

public class SubjectDAO {

    public List<Map<String, Object>> getAll() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT subject_id, subject_code, subject_name "
                   + "FROM subjects ORDER BY subject_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subjectId", rs.getInt("subject_id"));
                row.put("subjectCode", rs.getString("subject_code"));
                row.put("subjectName", rs.getString("subject_name"));
                list.add(row);
            }
        }
        return list;
    }

    public boolean add(String code, String name) throws SQLException {
        String sql = "INSERT INTO subjects(subject_code, subject_name) "
                   + "VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
