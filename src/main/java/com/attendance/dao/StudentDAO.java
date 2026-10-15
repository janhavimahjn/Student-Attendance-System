
package com.attendance.dao;

import com.attendance.model.Student;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();

        String sql = """
            SELECT student_id, roll_number, full_name,
                   email, class_name
            FROM students
            ORDER BY student_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student();

                s.setStudentId(rs.getInt("student_id"));
                s.setRollNumber(rs.getString("roll_number"));
                s.setFullName(rs.getString("full_name"));
                s.setEmail(rs.getString("email"));
                s.setClassName(rs.getString("class_name"));

                students.add(s);
            }
        }

        return students;
    }

    public boolean addStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO students
                (roll_number, full_name, email, class_name)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getRollNumber());
            ps.setString(2, s.getFullName());

            if (s.getEmail() == null || s.getEmail().isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, s.getEmail());
            }

            ps.setString(4, s.getClassName());

            return ps.executeUpdate() == 1;
        }
    }
    

    public boolean updateStudent(Student s) throws SQLException {
    	String sql = """
    			UPDATE students
    			SET roll_number = ?, full_name = ?, email = ?, class_name = ?
    			WHERE student_id = ?
    			""";

    	try (Connection con = DBConnection.getConnection();
    			PreparedStatement ps = con.prepareStatement(sql)) {

    		ps.setString(1, s.getRollNumber());
    		ps.setString(2, s.getFullName());

    		if (s.getEmail() == null || s.getEmail().isBlank()) {
    			ps.setNull(3, Types.VARCHAR);
    		} else {
    			ps.setString(3, s.getEmail());
    		}

    		ps.setString(4, s.getClassName());
    		ps.setInt(5, s.getStudentId());

    		return ps.executeUpdate() == 1;
    	}
    }

    public boolean deleteStudent(int studentId) throws SQLException {
    	String sql = "DELETE FROM students WHERE student_id = ?";

    	try (Connection con = DBConnection.getConnection();
    			PreparedStatement ps = con.prepareStatement(sql)) {

    		ps.setInt(1, studentId);
    		return ps.executeUpdate() == 1;
    	}
    }

    public List<Student> searchStudents(String keyword) throws SQLException {
    	List<Student> students = new ArrayList<>();

    	String sql = """
    			SELECT student_id, roll_number, full_name, email, class_name
    			FROM students
    			WHERE roll_number LIKE ?
    			   OR full_name LIKE ?
    			   OR class_name LIKE ?
    			ORDER BY student_id DESC
    			""";

    	try (Connection con = DBConnection.getConnection();
    			PreparedStatement ps = con.prepareStatement(sql)) {

    		String pattern = "%" + keyword + "%";
    		ps.setString(1, pattern);
    		ps.setString(2, pattern);
    		ps.setString(3, pattern);

    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next()) {
    				Student s = new Student();
    				s.setStudentId(rs.getInt("student_id"));
    				s.setRollNumber(rs.getString("roll_number"));
    				s.setFullName(rs.getString("full_name"));
    				s.setEmail(rs.getString("email"));
    				s.setClassName(rs.getString("class_name"));
    				students.add(s);
    			}
    		}
    	}

    	return students;
    }

}
