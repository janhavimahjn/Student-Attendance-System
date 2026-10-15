
package com.attendance.servlet;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/students")
public class StudentServlet extends HttpServlet {

    private final StudentDAO dao = new StudentDAO();

    private void sendJson(HttpServletResponse response,
                          int status, String json)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private String studentJson(Student s) {
        return "{\"studentId\":" + s.getStudentId()
            + ",\"rollNumber\":\"" + escape(s.getRollNumber())
            + "\",\"fullName\":\"" + escape(s.getFullName())
            + "\",\"email\":\"" + escape(s.getEmail())
            + "\",\"className\":\"" + escape(s.getClassName()) + "\"}";
    }

    private void sendStudents(HttpServletResponse response,
                              List<Student> students)
            throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (Student s : students) {
            if (json.length() > 1) json.append(",");
            json.append(studentJson(s));
        }
        json.append("]");
        sendJson(response, 200, json.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        try {
            String search = request.getParameter("search");
            if (search != null && !search.isBlank()) {
                sendStudents(response, dao.searchStudents(search.trim()));
            } else {
                sendStudents(response, dao.getAllStudents());
            }
        } catch (SQLException e) {
            log("Unable to retrieve students", e);
            sendJson(response, 500,
                "{\"error\":\"Unable to retrieve students\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("studentId"));
                boolean deleted = dao.deleteStudent(id);

                sendJson(response, deleted ? 200 : 404,
                    deleted ? "{\"message\":\"Student deleted\"}"
                            : "{\"error\":\"Student not found\"}");
                return;
            }

            String roll = request.getParameter("rollNumber");
            String name = request.getParameter("fullName");
            String email = request.getParameter("email");
            String className = request.getParameter("className");

            if (roll == null || roll.isBlank()
                    || name == null || name.isBlank()
                    || className == null || className.isBlank()) {
                sendJson(response, 400,
                    "{\"error\":\"Roll number, name and class are required\"}");
                return;
            }

            Student s = new Student(
                roll.trim(), name.trim(),
                email == null || email.isBlank() ? null : email.trim(),
                className.trim()
            );

            String idValue = request.getParameter("studentId");
            boolean success;

            if ("update".equals(action)) {
                if (idValue == null || idValue.isBlank()) {
                    sendJson(response, 400,
                        "{\"error\":\"Student ID is required\"}");
                    return;
                }
                s.setStudentId(Integer.parseInt(idValue));
                success = dao.updateStudent(s);
            } else {
                success = dao.addStudent(s);
            }

            if (success) {
                sendJson(response, "update".equals(action) ? 200 : 201,
                    "{\"message\":\"Student "
                    + ("update".equals(action) ? "updated" : "added")
                    + " successfully\"}");
            } else {
                sendJson(response, 404,
                    "{\"error\":\"Student not found or not saved\"}");
            }

        } catch (NumberFormatException e) {
            sendJson(response, 400, "{\"error\":\"Invalid student ID\"}");
        } catch (SQLException e) {
            log("Student database operation failed", e);
            if ("23000".equals(e.getSQLState())) {
                sendJson(response, 409,
                    "{\"error\":\"Roll number or email already exists\"}");
            } else {
                sendJson(response, 500,
                    "{\"error\":\"Database operation failed\"}");
            }
        }
    }
}
