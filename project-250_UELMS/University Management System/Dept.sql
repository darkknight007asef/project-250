-- create database universitymanagementsystem;
use universitymanagementsystem;

select * from student_semester;
select * from student;
select * from department_credit;
select * from department_courses;





SELECT * FROM department_courses WHERE dept='PAD';
SELECT * FROM department_courses WHERE dept='ANP';
SELECT * FROM department_courses WHERE dept='SOC';


-- Core tables required by the application

CREATE TABLE IF NOT EXISTS login (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    name VARCHAR(100) NOT NULL,
    fname VARCHAR(100) NOT NULL,
    registration_no VARCHAR(30) PRIMARY KEY,
    dob VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(120),
    class_x VARCHAR(10),
    class_xii VARCHAR(10),
    nid VARCHAR(50),
    course VARCHAR(50),
    branch VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS teacher (
    name VARCHAR(100) NOT NULL,
    fname VARCHAR(100) NOT NULL,
    empId VARCHAR(30) PRIMARY KEY,
    dob VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(120),
    bsc_in_sub VARCHAR(100),
    msc_in_sub VARCHAR(100),
    NID VARCHAR(50),
    cgpa_in_bsc VARCHAR(10),
    cgpa_in_msc VARCHAR(10),
    PHD VARCHAR(10),
    department VARCHAR(30),
    position VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS department_credit (
    dept VARCHAR(20) PRIMARY KEY,
    total_credit INT,
    sem1_credit INT, sem2_credit INT, sem3_credit INT, sem4_credit INT,
    sem5_credit INT, sem6_credit INT, sem7_credit INT, sem8_credit INT
);

CREATE TABLE IF NOT EXISTS department_courses (
    dept VARCHAR(20) NOT NULL,
    sem INT NOT NULL,
    course_code VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(200) NOT NULL,
    credit DECIMAL(4,1) NOT NULL,
    type VARCHAR(20) DEFAULT 'Theory'
);

CREATE TABLE IF NOT EXISTS student_semester (
    registration_no VARCHAR(30) PRIMARY KEY,
    dept VARCHAR(20) NOT NULL,
    current_semester INT DEFAULT 1,
    FOREIGN KEY (registration_no) REFERENCES student(registration_no) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS result (
    regNo VARCHAR(20),
    dept VARCHAR(10),
    sem INT,
    cgpa DECIMAL(3,2),
    PRIMARY KEY(regNo, sem)
);




































CREATE TABLE IF NOT EXISTS student_marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    registration_no VARCHAR(20),
    semester INT,
    course_code VARCHAR(10),
    credit DECIMAL(4,1),
    grade_point DECIMAL(3,2),
    obtained_credit DECIMAL(4,1) GENERATED ALWAYS AS (CASE WHEN grade_point > 0 THEN credit ELSE 0 END) STORED,
    status VARCHAR(10) GENERATED ALWAYS AS (CASE WHEN grade_point > 0 THEN 'Passed' ELSE 'Failed' END) STORED,
    FOREIGN KEY (registration_no) REFERENCES student(registration_no),
    UNIQUE KEY unique_marks (registration_no, semester, course_code)
);

CREATE TABLE IF NOT EXISTS FORGET_PASS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) UNIQUE
);

-- Minimal seed data
INSERT IGNORE INTO login(username, password) VALUES ('admin', 'admin');
