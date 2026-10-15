
package com.attendance.model;

public class Student {

    private int studentId;
    private String rollNumber;
    private String fullName;
    private String email;
    private String className;

    public Student() {
    }

    public Student(String rollNumber, String fullName,
                   String email, String className) {
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.className = className;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
