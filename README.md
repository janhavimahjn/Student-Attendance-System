# AttendEase – Student Attendance Management System

AttendEase is a web-based Student Attendance Management System developed using **Java, Jakarta Servlets, JDBC, MySQL, HTML, CSS, and JavaScript**. It simplifies attendance tracking by providing separate interfaces for administrators and students.

Administrators can manage student records, subjects, attendance, and reports, while students can register, log in, view their profiles, check subject-wise attendance, and monitor monthly attendance reports.

## Features

### Admin Module
- Secure admin login and logout.
- Add, view, update, and delete student records.
- Manage subjects.
- Mark student attendance as Present, Absent, or Late.
- View attendance records and generate attendance reports.
- Filter reports by date and month.

### Student Module
- Student registration using existing student details.
- Student login and logout.
- View personal profile information.
- View available subjects.
- Check subject-wise attendance records.
- View monthly attendance reports.

### Security and Data Management
- Session-based authentication and access control.
- Password hashing using PBKDF2 with HMAC-SHA256.
- JDBC connectivity with MySQL.
- Database-backed storage for student information, subjects, and attendance records.

## Technology Stack

- **Backend:** Java 21, Jakarta Servlets
- **Frontend:** HTML5, CSS3, JavaScript
- **Database:** MySQL
- **Database Connectivity:** JDBC
- **Build Tool:** Apache Maven
- **IDE:** Eclipse
- **Server:** Apache Tomcat 10.1

## Project Modules

1. **Admin Authentication** – Manages administrator access.
2. **Student Management** – Maintains student records.
3. **Subject Management** – Manages subjects.
4. **Attendance Management** – Records and tracks attendance.
5. **Attendance Reports** – Displays date-wise and monthly reports.
6. **Student Portal** – Allows students to view their own attendance and profile.

## Prerequisites

Before running the project, install:

- JDK 21
- Eclipse IDE
- Apache Tomcat 10.1
- MySQL Server
- Apache Maven

## Setup and Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/janhavimahjn/Student-Attendance-Management-System.git
   ```

2. Import the project into Eclipse as an **Existing Maven Project**.

3. Create a MySQL database named `student_attendance_db`.

4. Create the required database tables and configure the database connection in `DBConnection.java` using your local MySQL credentials.

5. Update Maven dependencies if required:

   ```bash
   mvn clean package
   ```

6. Configure Apache Tomcat 10.1 in Eclipse and deploy the project.

7. Start Tomcat and open the application in your browser using the local URL provided by your server configuration.

## Database

The application uses MySQL to store student records, student accounts, subjects, and attendance information. Configure the database connection and create the required tables before running the application.

**Important:** Do not commit real database passwords or other sensitive credentials to GitHub.

## Learning Outcomes

This project demonstrates practical implementation of Java web development, servlet-based request handling, JDBC database connectivity, CRUD operations, session management, authentication, password hashing, and report generation.

## Future Enhancements

- Attendance percentage calculation and visualization.
- Export attendance reports to PDF or Excel.
- Email notifications for low attendance.
- Improved role-based access control.
- Enhanced mobile-responsive user interface.
