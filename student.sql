CREATE DATABASE student_attendance_db;

USE student_attendance_db;

CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    class_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subjects (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL
);

CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL,
    remarks VARCHAR(255),

    FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    FOREIGN KEY (subject_id)
        REFERENCES subjects(subject_id),

    UNIQUE (student_id, subject_id, attendance_date)
);

INSERT INTO students
(roll_number, full_name, email, class_name)
VALUES
('CS001', 'Aryan Mahajan', 'aryan@gmail.com', 'TE Computer'),
('CS002', 'Janhavi Mahajan', 'janhavi@gmail.com', 'TE Computer'),
('CS003', 'Shraddha Nehete', 'shra@gmail.com', 'TE Computer'),
('CS004', 'Pratik Mahajan', 'pratik@gmail.com', 'TE Computer'),
('CS005', 'Vaibhavi Nemade', 'vaibhavi@gmail.com', 'TE Computer');

INSERT INTO subjects (subject_code, subject_name)
VALUES
('JAVA101', 'Core Java'),
('DBMS101', 'Database Management Systems'),
('CN101', 'Computer Networks');

SELECT * FROM students;
SELECT * FROM subjects;
SHOW TABLES;
DESCRIBE attendance;

CREATE TABLE IF NOT EXISTS student_accounts (
    student_id INT PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE
);

SELECT s.student_id, s.roll_number, s.full_name, s.email,
       CASE WHEN sa.student_id IS NULL
            THEN 'NOT REGISTERED'
            ELSE 'REGISTERED'
       END AS account_status
FROM students s
LEFT JOIN student_accounts sa
    ON sa.student_id = s.student_id;