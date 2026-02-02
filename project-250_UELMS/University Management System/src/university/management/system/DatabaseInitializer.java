package university.management.system;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Initializes the database schema for the University Management System.
 * Creates all necessary tables if they don't already exist.
 * This ensures the application works with an empty database.
 */
public final class DatabaseInitializer {

    private static volatile boolean initialized = false;
    private static final Object INIT_LOCK = new Object();

    private DatabaseInitializer() {}

    /**
     * Ensure all required database tables are created.
     * Safe to call multiple times - only creates tables on first call.
     */
    public static void initializeDatabase() throws SQLException {
        if (initialized) {
            // Double-check that tables actually exist
            if (verifyTablesExist()) {
                return;
            } else {
                // Tables don't exist, reset flag and reinitialize
                synchronized (INIT_LOCK) {
                    initialized = false;
                }
            }
        }

        synchronized (INIT_LOCK) {
            if (initialized) {
                return;
            }

            try (Conn conn = new Conn()) {
                if (!conn.isConnected()) {
                    throw new SQLException("Cannot initialize database: connection failed.");
                }

                System.out.println("Initializing database tables...");
                createTables(conn.c);
                insertDefaultData(conn.c);
                
                // Verify tables were created
                if (!verifyTablesExist()) {
                    throw new SQLException("Database initialization failed: tables were not created successfully.");
                }
                
                System.out.println("Database initialization complete!");
                initialized = true;
            } catch (SQLException e) {
                System.err.println("Database initialization error: " + e.getMessage());
                e.printStackTrace();
                initialized = false;
                throw e;
            }
        }
    }
    
    /**
     * Verify that critical tables exist in the database.
     */
    private static boolean verifyTablesExist() {
        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                return false;
            }
            try (java.sql.Statement st = conn.c.createStatement()) {
                // Check for critical tables
                java.sql.ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) as cnt FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() " +
                    "AND table_name IN ('student', 'department_credit', 'teacher', 'department_courses', 'subject', 'student_result', 'result_summary')"
                );
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    return count >= 4; // At least 4 critical tables should exist (new tables are optional)
                }
            }
        } catch (Exception e) {
            System.err.println("Error verifying tables: " + e.getMessage());
            return false;
        }
        return false;
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            // Disable foreign key checks temporarily to avoid dependency issues
            st.execute("SET FOREIGN_KEY_CHECKS=0");

            try {
                // Core student table
                st.execute("CREATE TABLE IF NOT EXISTS student (" +
                        "name VARCHAR(100)," +
                        "fname VARCHAR(100)," +
                        "registration_no VARCHAR(30) PRIMARY KEY," +
                        "dob VARCHAR(20)," +
                        "address VARCHAR(255)," +
                        "phone VARCHAR(30)," +
                        "email VARCHAR(120)," +
                        "class_x VARCHAR(10)," +
                        "class_xii VARCHAR(10)," +
                        "course VARCHAR(50)," +
                        "branch VARCHAR(20)," +
                        "photo_path VARCHAR(255)" +
                        ")");
                System.out.println("✓ Created student table");

                // Teacher table
                st.execute("CREATE TABLE IF NOT EXISTS teacher (" +
                        "name VARCHAR(100) NOT NULL," +
                        "fname VARCHAR(100) NOT NULL," +
                        "empId VARCHAR(30) PRIMARY KEY," +
                        "dob VARCHAR(20)," +
                        "address VARCHAR(255)," +
                        "phone VARCHAR(30)," +
                        "email VARCHAR(120)," +
                        "bsc_in_sub VARCHAR(100)," +
                        "msc_in_sub VARCHAR(100)," +
                        "cgpa_in_bsc VARCHAR(10)," +
                        "cgpa_in_msc VARCHAR(10)," +
                        "phd VARCHAR(10)," +
                        "department VARCHAR(30)," +
                        "position VARCHAR(50)," +
                        "photo_path VARCHAR(255)" +
                        ")");
                System.out.println("✓ Created teacher table");

                // Add photo_path column for existing databases that were created before this feature
                try {
                    st.execute("ALTER TABLE student ADD COLUMN photo_path VARCHAR(255)");
                } catch (SQLException ignore) {
                    // column likely already exists
                }
                try {
                    st.execute("ALTER TABLE teacher ADD COLUMN photo_path VARCHAR(255)");
                } catch (SQLException ignore) {
                    // column likely already exists
                }

                // Department credit table
                st.execute("CREATE TABLE IF NOT EXISTS department_credit (" +
                        "dept VARCHAR(20) PRIMARY KEY," +
                        "total_credit INT," +
                        "sem1_credit INT, sem2_credit INT, sem3_credit INT, sem4_credit INT," +
                        "sem5_credit INT, sem6_credit INT, sem7_credit INT, sem8_credit INT" +
                        ")");
                System.out.println("✓ Created department_credit table");

                // Department courses table
                st.execute("CREATE TABLE IF NOT EXISTS department_courses (" +
                        "dept VARCHAR(20) NOT NULL," +
                        "sem INT NOT NULL," +
                        "course_code VARCHAR(20) PRIMARY KEY," +
                        "course_name VARCHAR(200) NOT NULL," +
                        "credit DECIMAL(4,1) NOT NULL," +
                        "type VARCHAR(20) DEFAULT 'Theory'" +
                        ")");
                System.out.println("✓ Created department_courses table");

                // Student semester table (with foreign key)
                try {
                    st.execute("CREATE TABLE IF NOT EXISTS student_semester (" +
                            "registration_no VARCHAR(30) PRIMARY KEY," +
                            "dept VARCHAR(20) NOT NULL," +
                            "current_semester INT DEFAULT 1," +
                            "FOREIGN KEY (registration_no) REFERENCES student(registration_no) ON DELETE CASCADE" +
                            ")");
                    System.out.println("✓ Created student_semester table");
                } catch (SQLException e) {
                    // If foreign key fails, create without it
                    System.out.println("Warning: Creating student_semester without foreign key constraint");
                    st.execute("DROP TABLE IF EXISTS student_semester");
                    st.execute("CREATE TABLE IF NOT EXISTS student_semester (" +
                            "registration_no VARCHAR(30) PRIMARY KEY," +
                            "dept VARCHAR(20) NOT NULL," +
                            "current_semester INT DEFAULT 1" +
                            ")");
                    System.out.println("✓ Created student_semester table (without FK)");
                }

                // Student marks table (with foreign key)
                try {
                    st.execute("CREATE TABLE IF NOT EXISTS student_marks (" +
                            "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "registration_no VARCHAR(30) NOT NULL," +
                            "semester INT," +
                            "course_code VARCHAR(20)," +
                            "credit DECIMAL(4,1)," +
                            "grade_point DECIMAL(3,2)," +
                            "UNIQUE KEY unique_marks (registration_no, semester, course_code)," +
                            "FOREIGN KEY (registration_no) REFERENCES student(registration_no) ON DELETE CASCADE" +
                            ")");
                    System.out.println("✓ Created student_marks table");
                } catch (SQLException e) {
                    // If foreign key fails, create without it
                    System.out.println("Warning: Creating student_marks without foreign key constraint");
                    st.execute("DROP TABLE IF EXISTS student_marks");
                    st.execute("CREATE TABLE IF NOT EXISTS student_marks (" +
                            "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "registration_no VARCHAR(30) NOT NULL," +
                            "semester INT," +
                            "course_code VARCHAR(20)," +
                            "credit DECIMAL(4,1)," +
                            "grade_point DECIMAL(3,2)," +
                            "UNIQUE KEY unique_marks (registration_no, semester, course_code)" +
                            ")");
                    System.out.println("✓ Created student_marks table (without FK)");
                }

                // Fee structure table
                st.execute("CREATE TABLE IF NOT EXISTS fee (" +
                        "course VARCHAR(20)," +
                        "semester1 VARCHAR(20), semester2 VARCHAR(20), semester3 VARCHAR(20), semester4 VARCHAR(20)," +
                        "semester5 VARCHAR(20), semester6 VARCHAR(20), semester7 VARCHAR(20), semester8 VARCHAR(20)" +
                        ")");
                System.out.println("✓ Created fee table");

                // College fee table
                st.execute("CREATE TABLE IF NOT EXISTS collegefee (" +
                        "rollno VARCHAR(20)," +
                        "course VARCHAR(20)," +
                        "branch VARCHAR(20)," +
                        "semester VARCHAR(20)," +
                        "total VARCHAR(20)" +
                        ")");
                System.out.println("✓ Created collegefee table");

                // Login table (legacy)
                st.execute("CREATE TABLE IF NOT EXISTS login (" +
                        "username VARCHAR(50)," +
                        "password VARCHAR(100)" +
                        ")");
                System.out.println("✓ Created login table");

                // Result table
                st.execute("CREATE TABLE IF NOT EXISTS result (" +
                        "regNo VARCHAR(20)," +
                        "dept VARCHAR(10)," +
                        "sem INT," +
                        "cgpa DECIMAL(3,2)," +
                        "PRIMARY KEY(regNo, sem)" +
                        ")");
                System.out.println("✓ Created result table");

                // Subject table (for Result & Grade Management)
                st.execute("CREATE TABLE IF NOT EXISTS subject (" +
                        "subject_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "subject_code VARCHAR(20) NOT NULL UNIQUE," +
                        "subject_name VARCHAR(255) NOT NULL," +
                        "semester INT NOT NULL," +
                        "department VARCHAR(255) NOT NULL," +
                        "full_marks INT NOT NULL DEFAULT 100," +
                        "pass_marks INT NOT NULL DEFAULT 40" +
                        ")");
                System.out.println("✓ Created subject table");

                // Student result table (enhanced marks storage)
                try {
                    st.execute("CREATE TABLE IF NOT EXISTS student_result (" +
                            "result_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "registration_no VARCHAR(30) NOT NULL," +
                            "subject_code VARCHAR(20) NOT NULL," +
                            "marks_obtained DECIMAL(5,2) NOT NULL," +
                            "exam_type ENUM('Regular','Re-Take','Improvement') DEFAULT 'Regular'," +
                            "exam_year YEAR NOT NULL," +
                            "semester INT NOT NULL," +
                            "grade VARCHAR(5)," +
                            "grade_point DECIMAL(3,2)," +
                            "status ENUM('PASS','FAIL')," +
                            "is_approved BOOLEAN DEFAULT FALSE," +
                            "marks_attendance DECIMAL(5,2) DEFAULT 0," +
                            "marks_eval DECIMAL(5,2) DEFAULT 0," +
                            "marks_term DECIMAL(5,2) DEFAULT 0," +
                            "marks_final DECIMAL(5,2) DEFAULT 0," +
                            "UNIQUE KEY unique_result (registration_no, subject_code, exam_year, semester, exam_type)" +
                            ")");
                    System.out.println("✓ Created student_result table");
                } catch (SQLException e) {
                    System.out.println("Warning: Creating student_result without foreign key constraint");
                    st.execute("DROP TABLE IF EXISTS student_result");
                    st.execute("CREATE TABLE IF NOT EXISTS student_result (" +
                            "result_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "registration_no VARCHAR(30) NOT NULL," +
                            "subject_code VARCHAR(20) NOT NULL," +
                            "marks_obtained DECIMAL(5,2) NOT NULL," +
                            "exam_type ENUM('Regular','Re-Take','Improvement') DEFAULT 'Regular'," +
                            "exam_year YEAR NOT NULL," +
                            "semester INT NOT NULL," +
                            "grade VARCHAR(5)," +
                            "grade_point DECIMAL(3,2)," +
                            "status ENUM('PASS','FAIL')," +
                            "is_approved BOOLEAN DEFAULT FALSE," +
                            "marks_attendance DECIMAL(5,2) DEFAULT 0," +
                            "marks_eval DECIMAL(5,2) DEFAULT 0," +
                            "marks_term DECIMAL(5,2) DEFAULT 0," +
                            "marks_final DECIMAL(5,2) DEFAULT 0," +
                            "UNIQUE KEY unique_result (registration_no, subject_code, exam_year, semester, exam_type)" +
                            ")");
                    System.out.println("✓ Created student_result table (without FK)");
                }

                // Ensure detailed marks columns exist for existing databases
                try { st.execute("ALTER TABLE student_result ADD COLUMN marks_attendance DECIMAL(5,2) DEFAULT 0"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE student_result ADD COLUMN marks_eval DECIMAL(5,2) DEFAULT 0"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE student_result ADD COLUMN marks_term DECIMAL(5,2) DEFAULT 0"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE student_result ADD COLUMN marks_final DECIMAL(5,2) DEFAULT 0"); } catch (SQLException ignore) {}

                // Recheck / re-evaluation workflow table
                st.execute("CREATE TABLE IF NOT EXISTS recheck_request (" +
                        "request_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "registration_no VARCHAR(30) NOT NULL," +
                        "subject_code VARCHAR(20) NOT NULL," +
                        "semester INT NOT NULL," +
                        "exam_year YEAR NOT NULL," +
                        "request_type ENUM('RECOUNT','REEVALUATION') DEFAULT 'RECOUNT'," +
                        "reason VARCHAR(255)," +
                        "status VARCHAR(30) DEFAULT 'SUBMITTED'," +
                        "admin_comment VARCHAR(255)," +
                        "teacher_comment VARCHAR(255)," +
                        "proposed_marks DECIMAL(6,2) DEFAULT NULL," +
                        "proposed_grade VARCHAR(5) DEFAULT NULL," +
                        "proposed_grade_point DECIMAL(3,2) DEFAULT NULL," +
                        "proposed_status VARCHAR(10) DEFAULT NULL," +
                        "teacher_no_change BOOLEAN DEFAULT FALSE," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY unique_recheck (registration_no, subject_code, semester, exam_year)," +
                        "FOREIGN KEY (registration_no) REFERENCES student(registration_no) ON DELETE CASCADE" +
                        ")");
                System.out.println("✓ Created/Verified recheck_request table");

                // Upgrade older recheck_request schema (SchemaUpdater created enum status + fewer columns)
                try { st.execute("ALTER TABLE recheck_request MODIFY COLUMN status VARCHAR(30) DEFAULT 'SUBMITTED'"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN request_type ENUM('RECOUNT','REEVALUATION') DEFAULT 'RECOUNT'"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN teacher_comment VARCHAR(255)"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN proposed_marks DECIMAL(6,2) DEFAULT NULL"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN proposed_grade VARCHAR(5) DEFAULT NULL"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN proposed_grade_point DECIMAL(3,2) DEFAULT NULL"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN proposed_status VARCHAR(10) DEFAULT NULL"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN teacher_no_change BOOLEAN DEFAULT FALSE"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (SQLException ignore) {}
                try { st.execute("ALTER TABLE recheck_request ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"); } catch (SQLException ignore) {}

                // Audit log for any approved result changes
                st.execute("CREATE TABLE IF NOT EXISTS result_change_audit (" +
                        "audit_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "result_id INT NOT NULL," +
                        "request_id INT DEFAULT NULL," +
                        "changed_by_role VARCHAR(20) DEFAULT NULL," +
                        "old_marks DECIMAL(6,2) DEFAULT NULL," +
                        "new_marks DECIMAL(6,2) DEFAULT NULL," +
                        "old_grade VARCHAR(5) DEFAULT NULL," +
                        "new_grade VARCHAR(5) DEFAULT NULL," +
                        "old_grade_point DECIMAL(3,2) DEFAULT NULL," +
                        "new_grade_point DECIMAL(3,2) DEFAULT NULL," +
                        "old_status VARCHAR(10) DEFAULT NULL," +
                        "new_status VARCHAR(10) DEFAULT NULL," +
                        "comment VARCHAR(255) DEFAULT NULL," +
                        "changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");
                System.out.println("✓ Created/Verified result_change_audit table");

                // Result summary table
                st.execute("CREATE TABLE IF NOT EXISTS result_summary (" +
                        "summary_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "registration_no VARCHAR(30) NOT NULL," +
                        "semester INT NOT NULL," +
                        "exam_year YEAR NOT NULL," +
                        "total_marks DECIMAL(6,2)," +
                        "obtained_marks DECIMAL(6,2)," +
                        "percentage DECIMAL(5,2)," +
                        "gpa DECIMAL(3,2)," +
                        "result ENUM('PASS','FAIL')," +
                        "UNIQUE KEY unique_summary (registration_no, semester, exam_year)" +
                        ")");
                System.out.println("✓ Created result_summary table");

            } finally {
                // Re-enable foreign key checks
                st.execute("SET FOREIGN_KEY_CHECKS=1");
            }

            System.out.println("✓ All tables created successfully");
        }
    }

    private static void insertDefaultData(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {

            // Check if department data exists, if not insert defaults
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM department_credit");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting default department credit data...");

                // Insert department credits
                st.execute("INSERT IGNORE INTO department_credit VALUES " +
                        "('CSE', 170, 22, 24, 21, 22, 22, 23, 18, 18)," +
                        "('EEE', 172, 22, 23, 22, 22, 21, 22, 20, 20)," +
                        "('ME', 170, 22, 22, 22, 22, 22, 20, 20, 20)," +
                        "('CE', 168, 21, 22, 21, 22, 21, 21, 20, 20)," +
                        "('CHE', 162, 20, 21, 21, 20, 20, 20, 20, 20)," +
                        "('SWE', 168, 21, 22, 22, 22, 21, 20, 20, 20)," +
                        "('BAN', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('ENG', 162, 21, 21, 20, 20, 20, 20, 20, 20)," +
                        "('BMB', 164, 21, 21, 21, 21, 20, 20, 20, 20)," +
                        "('GE', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('CEP', 166, 21, 21, 21, 21, 22, 20, 20, 20)," +
                        "('ANP', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('PAD', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('SOC', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('MATH', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('PHY', 165, 21, 21, 21, 21, 21, 20, 20, 20)," +
                        "('GEO', 160, 20, 20, 20, 20, 20, 20, 20, 20)," +
                        "('FET', 160, 20, 20, 20, 20, 20, 20, 20, 20)");

                System.out.println("✓ Default department credits inserted");
            }

            // Check if fee data exists, if not insert defaults
            rs = st.executeQuery("SELECT COUNT(*) FROM fee");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting default fee structure...");

                st.execute("INSERT IGNORE INTO fee VALUES " +
                        "('BTech', '48000', '43000', '43000', '43000', '43000', '48000', '43000', '43000')," +
                        "('Bsc', '40000', '35000', '35000', '35000', '35000', '35000', '', '')," +
                        "('BCA', '35000', '34000', '34000', '34000', '34000', '34000', '', '')," +
                        "('MTech', '65000', '60000', '60000', '60000', '', '', '', '')," +
                        "('MSc', '47500', '45000', '45000', '45000', '', '', '', '')," +
                        "('MCA', '43000', '42000', '42000', '49000', '', '', '', '')," +
                        "('Bcom', '22000', '20000', '20000', '20000', '20000', '20000', '', '')," +
                        "('Mcom', '36000', '30000', '30000', '30000', '', '', '', '')");

                System.out.println("✓ Default fee structure inserted");
            }

            // Check if department_courses data exists, if not insert defaults
            rs = st.executeQuery("SELECT COUNT(*) FROM department_courses");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting comprehensive department courses for all departments and semesters...");
                
                // CSE - Computer Science and Engineering (5-7 courses per semester)
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('CSE', 1, 'CSE101', 'Introduction to Programming', 3.0, 'Theory')," +
                        "('CSE', 1, 'CSE102', 'Programming Lab', 2.0, 'Lab')," +
                        "('CSE', 1, 'CSE103', 'Discrete Mathematics', 3.0, 'Theory')," +
                        "('CSE', 1, 'CSE104', 'Basic Electronics', 3.0, 'Theory')," +
                        "('CSE', 1, 'CSE105', 'English Communication', 3.0, 'Theory')," +
                        "('CSE', 1, 'CSE106', 'Linear Algebra', 3.0, 'Theory')," +
                        "('CSE', 2, 'CSE201', 'Data Structures', 3.0, 'Theory')," +
                        "('CSE', 2, 'CSE202', 'Data Structures Lab', 2.0, 'Lab')," +
                        "('CSE', 2, 'CSE203', 'Object Oriented Programming', 3.0, 'Theory')," +
                        "('CSE', 2, 'CSE204', 'OOP Lab', 2.0, 'Lab')," +
                        "('CSE', 2, 'CSE205', 'Digital Logic Design', 3.0, 'Theory')," +
                        "('CSE', 2, 'CSE206', 'Calculus', 3.0, 'Theory')," +
                        "('CSE', 3, 'CSE301', 'Database Management Systems', 3.0, 'Theory')," +
                        "('CSE', 3, 'CSE302', 'DBMS Lab', 2.0, 'Lab')," +
                        "('CSE', 3, 'CSE303', 'Computer Networks', 3.0, 'Theory')," +
                        "('CSE', 3, 'CSE304', 'Networking Lab', 2.0, 'Lab')," +
                        "('CSE', 3, 'CSE305', 'Operating Systems', 3.0, 'Theory')," +
                        "('CSE', 3, 'CSE306', 'Design and Analysis of Algorithms', 3.0, 'Theory')," +
                        "('CSE', 4, 'CSE401', 'Software Engineering', 3.0, 'Theory')," +
                        "('CSE', 4, 'CSE402', 'Web Technologies', 3.0, 'Theory')," +
                        "('CSE', 4, 'CSE403', 'Web Development Lab', 2.0, 'Lab')," +
                        "('CSE', 4, 'CSE404', 'Algorithm Design', 3.0, 'Theory')," +
                        "('CSE', 4, 'CSE405', 'Artificial Intelligence', 3.0, 'Theory')," +
                        "('CSE', 5, 'CSE501', 'Machine Learning', 3.0, 'Theory')," +
                        "('CSE', 5, 'CSE502', 'ML Lab', 2.0, 'Lab')," +
                        "('CSE', 5, 'CSE503', 'Cloud Computing', 3.0, 'Theory')," +
                        "('CSE', 5, 'CSE504', 'Mobile App Development', 3.0, 'Theory')," +
                        "('CSE', 5, 'CSE505', 'Advanced DBMS', 3.0, 'Theory')," +
                        "('CSE', 6, 'CSE601', 'Artificial Intelligence II', 3.0, 'Theory')," +
                        "('CSE', 6, 'CSE602', 'AI Lab', 2.0, 'Lab')," +
                        "('CSE', 6, 'CSE603', 'Cybersecurity', 3.0, 'Theory')," +
                        "('CSE', 6, 'CSE604', 'Big Data Analytics', 3.0, 'Theory')," +
                        "('CSE', 6, 'CSE605', 'Data Science', 3.0, 'Theory')," +
                        "('CSE', 7, 'CSE701', 'Software Architecture', 3.0, 'Theory')," +
                        "('CSE', 7, 'CSE702', 'Capstone Project I', 4.0, 'Lab')," +
                        "('CSE', 7, 'CSE703', 'IoT Development', 3.0, 'Theory')," +
                        "('CSE', 7, 'CSE704', 'Advanced Web Technologies', 3.0, 'Theory')," +
                        "('CSE', 8, 'CSE801', 'Final Project', 5.0, 'Lab')," +
                        "('CSE', 8, 'CSE802', 'Advanced Topics', 3.0, 'Theory')," +
                        "('CSE', 8, 'CSE803', 'Professional Ethics', 2.0, 'Theory')," +
                        "('CSE', 8, 'CSE804', 'Research Methodology', 3.0, 'Theory')");
                
                // EEE - Electrical and Electronics Engineering
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('EEE', 1, 'EEE101', 'Basic Electrical Engineering', 3.0, 'Theory')," +
                        "('EEE', 1, 'EEE102', 'Electrical Lab I', 2.0, 'Lab')," +
                        "('EEE', 1, 'EEE103', 'Mathematics I', 3.0, 'Theory')," +
                        "('EEE', 1, 'EEE104', 'Physics for Engineers', 3.0, 'Theory')," +
                        "('EEE', 1, 'EEE105', 'Chemistry for Engineers', 3.0, 'Theory')," +
                        "('EEE', 1, 'EEE106', 'English Communication', 3.0, 'Theory')," +
                        "('EEE', 2, 'EEE201', 'Circuit Theory', 3.0, 'Theory')," +
                        "('EEE', 2, 'EEE202', 'Circuit Lab', 2.0, 'Lab')," +
                        "('EEE', 2, 'EEE203', 'Digital Electronics', 3.0, 'Theory')," +
                        "('EEE', 2, 'EEE204', 'Digital Lab', 2.0, 'Lab')," +
                        "('EEE', 2, 'EEE205', 'Mathematics II', 3.0, 'Theory')," +
                        "('EEE', 2, 'EEE206', 'Programming in C', 3.0, 'Theory')," +
                        "('EEE', 3, 'EEE301', 'Electromagnetic Field Theory', 3.0, 'Theory')," +
                        "('EEE', 3, 'EEE302', 'Power Systems I', 3.0, 'Theory')," +
                        "('EEE', 3, 'EEE303', 'Control Systems', 3.0, 'Theory')," +
                        "('EEE', 3, 'EEE304', 'Electronics Devices', 3.0, 'Theory')," +
                        "('EEE', 3, 'EEE305', 'Signals and Systems', 3.0, 'Theory')," +
                        "('EEE', 4, 'EEE401', 'Electrical Machines', 3.0, 'Theory')," +
                        "('EEE', 4, 'EEE402', 'Machine Lab', 2.0, 'Lab')," +
                        "('EEE', 4, 'EEE403', 'Power Electronics', 3.0, 'Theory')," +
                        "('EEE', 4, 'EEE404', 'Microprocessors', 3.0, 'Theory')," +
                        "('EEE', 4, 'EEE405', 'Digital Signal Processing', 3.0, 'Theory')," +
                        "('EEE', 5, 'EEE501', 'Power System Analysis', 3.0, 'Theory')," +
                        "('EEE', 5, 'EEE502', 'Renewable Energy', 3.0, 'Theory')," +
                        "('EEE', 5, 'EEE503', 'Electrical Machines II', 3.0, 'Theory')," +
                        "('EEE', 5, 'EEE504', 'Measurement and Instrumentation', 3.0, 'Theory')," +
                        "('EEE', 6, 'EEE601', 'High Voltage Engineering', 3.0, 'Theory')," +
                        "('EEE', 6, 'EEE602', 'Electrical Drives', 3.0, 'Theory')," +
                        "('EEE', 6, 'EEE603', 'Power System Protection', 3.0, 'Theory')," +
                        "('EEE', 6, 'EEE604', 'Control Systems II', 3.0, 'Theory')," +
                        "('EEE', 7, 'EEE701', 'Power System Operation', 3.0, 'Theory')," +
                        "('EEE', 7, 'EEE702', 'Project Work', 4.0, 'Lab')," +
                        "('EEE', 7, 'EEE703', 'Industrial Electronics', 3.0, 'Theory')," +
                        "('EEE', 7, 'EEE704', 'Energy Management', 3.0, 'Theory')," +
                        "('EEE', 8, 'EEE801', 'Final Project', 5.0, 'Lab')," +
                        "('EEE', 8, 'EEE802', 'Advanced Power Systems', 3.0, 'Theory')," +
                        "('EEE', 8, 'EEE803', 'Electrical Design', 3.0, 'Theory')");
                
                // SWE - Software Engineering
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('SWE', 1, 'SWE101', 'Introduction to Software Engineering', 3.0, 'Theory')," +
                        "('SWE', 1, 'SWE102', 'Programming Fundamentals', 3.0, 'Theory')," +
                        "('SWE', 1, 'SWE103', 'Programming Lab I', 2.0, 'Lab')," +
                        "('SWE', 1, 'SWE104', 'Discrete Mathematics', 3.0, 'Theory')," +
                        "('SWE', 1, 'SWE105', 'English Communication', 3.0, 'Theory')," +
                        "('SWE', 2, 'SWE201', 'Data Structures & Algorithms', 3.0, 'Theory')," +
                        "('SWE', 2, 'SWE202', 'DSA Lab', 2.0, 'Lab')," +
                        "('SWE', 2, 'SWE203', 'Object Oriented Design', 3.0, 'Theory')," +
                        "('SWE', 2, 'SWE204', 'OOP Lab', 2.0, 'Lab')," +
                        "('SWE', 2, 'SWE205', 'Computer Organization', 3.0, 'Theory')," +
                        "('SWE', 3, 'SWE301', 'Database Systems', 3.0, 'Theory')," +
                        "('SWE', 3, 'SWE302', 'DBMS Lab', 2.0, 'Lab')," +
                        "('SWE', 3, 'SWE303', 'Web Development', 3.0, 'Theory')," +
                        "('SWE', 3, 'SWE304', 'Web Lab', 2.0, 'Lab')," +
                        "('SWE', 3, 'SWE305', 'Software Engineering Principles', 3.0, 'Theory')," +
                        "('SWE', 4, 'SWE401', 'Software Architecture', 3.0, 'Theory')," +
                        "('SWE', 4, 'SWE402', 'Mobile Development', 3.0, 'Theory')," +
                        "('SWE', 4, 'SWE403', 'Operating Systems', 3.0, 'Theory')," +
                        "('SWE', 4, 'SWE404', 'Computer Networks', 3.0, 'Theory')," +
                        "('SWE', 5, 'SWE501', 'Software Testing', 3.0, 'Theory')," +
                        "('SWE', 5, 'SWE502', 'Cloud Computing', 3.0, 'Theory')," +
                        "('SWE', 5, 'SWE503', 'Advanced Database', 3.0, 'Theory')," +
                        "('SWE', 5, 'SWE504', 'Human Computer Interaction', 3.0, 'Theory')," +
                        "('SWE', 6, 'SWE601', 'Machine Learning for Software', 3.0, 'Theory')," +
                        "('SWE', 6, 'SWE602', 'Mini Project', 4.0, 'Lab')," +
                        "('SWE', 6, 'SWE603', 'Cybersecurity', 3.0, 'Theory')," +
                        "('SWE', 6, 'SWE604', 'Software Project Management', 3.0, 'Theory')," +
                        "('SWE', 7, 'SWE701', 'Software Quality Assurance', 3.0, 'Theory')," +
                        "('SWE', 7, 'SWE702', 'Capstone Project', 4.0, 'Lab')," +
                        "('SWE', 7, 'SWE703', 'Big Data Analytics', 3.0, 'Theory')," +
                        "('SWE', 8, 'SWE801', 'Final Project', 5.0, 'Lab')," +
                        "('SWE', 8, 'SWE802', 'Advanced Software Topics', 3.0, 'Theory')," +
                        "('SWE', 8, 'SWE803', 'Cloud Integration', 3.0, 'Theory')");
                
                // ME - Mechanical Engineering
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('ME', 1, 'ME101', 'Engineering Mechanics', 3.0, 'Theory')," +
                        "('ME', 1, 'ME102', 'Mechanical Workshop', 2.0, 'Lab')," +
                        "('ME', 1, 'ME103', 'Mathematics I', 3.0, 'Theory')," +
                        "('ME', 1, 'ME104', 'Physics for Engineers', 3.0, 'Theory')," +
                        "('ME', 1, 'ME105', 'Chemistry for Engineers', 3.0, 'Theory')," +
                        "('ME', 1, 'ME106', 'English Communication', 3.0, 'Theory')," +
                        "('ME', 2, 'ME201', 'Thermodynamics', 3.0, 'Theory')," +
                        "('ME', 2, 'ME202', 'Materials Science', 3.0, 'Theory')," +
                        "('ME', 2, 'ME203', 'Mathematics II', 3.0, 'Theory')," +
                        "('ME', 2, 'ME204', 'Basic Electrical Engineering', 3.0, 'Theory')," +
                        "('ME', 2, 'ME205', 'Mechanical Drawing', 3.0, 'Theory')," +
                        "('ME', 3, 'ME301', 'Fluid Mechanics', 3.0, 'Theory')," +
                        "('ME', 3, 'ME302', 'Strength of Materials', 3.0, 'Theory')," +
                        "('ME', 3, 'ME303', 'Manufacturing Processes', 3.0, 'Theory')," +
                        "('ME', 3, 'ME304', 'Material Testing Lab', 2.0, 'Lab')," +
                        "('ME', 3, 'ME305', 'Thermodynamics II', 3.0, 'Theory')," +
                        "('ME', 4, 'ME401', 'Heat Transfer', 3.0, 'Theory')," +
                        "('ME', 4, 'ME402', 'Dynamics of Machines', 3.0, 'Theory')," +
                        "('ME', 4, 'ME403', 'Manufacturing Processes II', 3.0, 'Theory')," +
                        "('ME', 4, 'ME404', 'Mechanical Lab II', 2.0, 'Lab')," +
                        "('ME', 5, 'ME501', 'Design of Machine Elements', 3.0, 'Theory')," +
                        "('ME', 5, 'ME502', 'Thermal Engineering', 3.0, 'Theory')," +
                        "('ME', 5, 'ME503', 'CAD/CAM', 3.0, 'Theory')," +
                        "('ME', 5, 'ME504', 'CAD Lab', 2.0, 'Lab')," +
                        "('ME', 6, 'ME601', 'Control Engineering', 3.0, 'Theory')," +
                        "('ME', 6, 'ME602', 'Mechatronics', 3.0, 'Theory')," +
                        "('ME', 6, 'ME603', 'Mechatronics Lab', 2.0, 'Lab')," +
                        "('ME', 6, 'ME604', 'Design of Machine Elements II', 3.0, 'Theory')," +
                        "('ME', 7, 'ME701', 'Refrigeration and Air Conditioning', 3.0, 'Theory')," +
                        "('ME', 7, 'ME702', 'Finite Element Analysis', 3.0, 'Theory')," +
                        "('ME', 7, 'ME703', 'Project Management', 3.0, 'Theory')," +
                        "('ME', 7, 'ME704', 'Industrial Training', 3.0, 'Lab')," +
                        "('ME', 8, 'ME801', 'Mechanical Design Project', 5.0, 'Lab')," +
                        "('ME', 8, 'ME802', 'Maintenance Engineering', 3.0, 'Theory')," +
                        "('ME', 8, 'ME803', 'Entrepreneurship', 3.0, 'Theory')");
                
                // CE - Civil Engineering
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('CE', 1, 'CE101', 'Engineering Mechanics', 3.0, 'Theory')," +
                        "('CE', 1, 'CE102', 'Surveying', 3.0, 'Theory')," +
                        "('CE', 1, 'CE103', 'Mathematics I', 3.0, 'Theory')," +
                        "('CE', 1, 'CE104', 'Physics for Engineers', 3.0, 'Theory')," +
                        "('CE', 1, 'CE105', 'Chemistry for Engineers', 3.0, 'Theory')," +
                        "('CE', 1, 'CE106', 'Computer Programming', 3.0, 'Theory')," +
                        "('CE', 2, 'CE201', 'Structural Analysis', 3.0, 'Theory')," +
                        "('CE', 2, 'CE202', 'Building Materials', 3.0, 'Theory')," +
                        "('CE', 2, 'CE203', 'Surveying Lab', 2.0, 'Lab')," +
                        "('CE', 2, 'CE204', 'Mathematics II', 3.0, 'Theory')," +
                        "('CE', 2, 'CE205', 'Basic Electrical Engineering', 3.0, 'Theory')," +
                        "('CE', 3, 'CE301', 'Fluid Mechanics', 3.0, 'Theory')," +
                        "('CE', 3, 'CE302', 'Soil Mechanics', 3.0, 'Theory')," +
                        "('CE', 3, 'CE303', 'Strength of Materials', 3.0, 'Theory')," +
                        "('CE', 3, 'CE304', 'Construction Technology', 3.0, 'Theory')," +
                        "('CE', 3, 'CE305', 'Materials Testing Lab', 2.0, 'Lab')," +
                        "('CE', 4, 'CE401', 'Structural Analysis II', 3.0, 'Theory')," +
                        "('CE', 4, 'CE402', 'Concrete Technology', 3.0, 'Theory')," +
                        "('CE', 4, 'CE403', 'Surveying II', 3.0, 'Theory')," +
                        "('CE', 4, 'CE404', 'Civil Lab II', 2.0, 'Lab')," +
                        "('CE', 5, 'CE501', 'Design of Reinforced Concrete', 3.0, 'Theory')," +
                        "('CE', 5, 'CE502', 'Environmental Engineering', 3.0, 'Theory')," +
                        "('CE', 5, 'CE503', 'Transportation Engineering', 3.0, 'Theory')," +
                        "('CE', 5, 'CE504', 'CAD for Civil Engineering', 3.0, 'Theory')," +
                        "('CE', 6, 'CE601', 'Design of Steel Structures', 3.0, 'Theory')," +
                        "('CE', 6, 'CE602', 'Environmental Engineering II', 3.0, 'Theory')," +
                        "('CE', 6, 'CE603', 'Hydrology and Water Resources', 3.0, 'Theory')," +
                        "('CE', 6, 'CE604', 'Surveying Lab II', 2.0, 'Lab')," +
                        "('CE', 7, 'CE701', 'Geotechnical Engineering', 3.0, 'Theory')," +
                        "('CE', 7, 'CE702', 'Construction Management', 3.0, 'Theory')," +
                        "('CE', 7, 'CE703', 'Project Planning', 3.0, 'Theory')," +
                        "('CE', 7, 'CE704', 'Industrial Training', 3.0, 'Lab')," +
                        "('CE', 8, 'CE801', 'Civil Engineering Project', 5.0, 'Lab')," +
                        "('CE', 8, 'CE802', 'Advanced Structural Design', 3.0, 'Theory')," +
                        "('CE', 8, 'CE803', 'Entrepreneurship', 3.0, 'Theory')");
                
                // MATH - Mathematics
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('MATH', 1, 'MATH101', 'Calculus I', 3.0, 'Theory')," +
                        "('MATH', 1, 'MATH102', 'Linear Algebra', 3.0, 'Theory')," +
                        "('MATH', 1, 'MATH103', 'Chemistry I', 3.0, 'Theory')," +
                        "('MATH', 1, 'MATH104', 'Physics I', 3.0, 'Theory')," +
                        "('MATH', 1, 'MATH105', 'English Communication', 3.0, 'Theory')," +
                        "('MATH', 2, 'MATH201', 'Calculus II', 3.0, 'Theory')," +
                        "('MATH', 2, 'MATH202', 'Differential Equations', 3.0, 'Theory')," +
                        "('MATH', 2, 'MATH203', 'Probability and Statistics', 3.0, 'Theory')," +
                        "('MATH', 2, 'MATH204', 'Physics II', 3.0, 'Theory')," +
                        "('MATH', 2, 'MATH205', 'Physics Lab', 2.0, 'Lab')," +
                        "('MATH', 3, 'MATH301', 'Abstract Algebra', 3.0, 'Theory')," +
                        "('MATH', 3, 'MATH302', 'Real Analysis', 3.0, 'Theory')," +
                        "('MATH', 3, 'MATH303', 'Numerical Methods', 3.0, 'Theory')," +
                        "('MATH', 3, 'MATH304', 'Programming Basics', 3.0, 'Theory')," +
                        "('MATH', 3, 'MATH305', 'Programming Lab', 2.0, 'Lab')," +
                        "('MATH', 4, 'MATH401', 'Abstract Algebra II', 3.0, 'Theory')," +
                        "('MATH', 4, 'MATH402', 'Real Analysis II', 3.0, 'Theory')," +
                        "('MATH', 4, 'MATH403', 'Complex Analysis', 3.0, 'Theory')," +
                        "('MATH', 4, 'MATH404', 'Data Structures', 3.0, 'Theory')," +
                        "('MATH', 5, 'MATH501', 'Topology', 3.0, 'Theory')," +
                        "('MATH', 5, 'MATH502', 'Differential Geometry', 3.0, 'Theory')," +
                        "('MATH', 5, 'MATH503', 'Operations Research', 3.0, 'Theory')," +
                        "('MATH', 5, 'MATH504', 'Mathematical Modelling', 3.0, 'Theory')," +
                        "('MATH', 6, 'MATH601', 'Functional Analysis', 3.0, 'Theory')," +
                        "('MATH', 6, 'MATH602', 'Numerical Linear Algebra', 3.0, 'Theory')," +
                        "('MATH', 6, 'MATH603', 'Mathematical Statistics', 3.0, 'Theory')," +
                        "('MATH', 6, 'MATH604', 'Scientific Computing Lab', 2.0, 'Lab')," +
                        "('MATH', 7, 'MATH701', 'Partial Differential Equations', 3.0, 'Theory')," +
                        "('MATH', 7, 'MATH702', 'Advanced Algebra', 3.0, 'Theory')," +
                        "('MATH', 7, 'MATH703', 'Mathematical Software Lab', 2.0, 'Lab')," +
                        "('MATH', 7, 'MATH704', 'Capstone Project', 4.0, 'Lab')," +
                        "('MATH', 8, 'MATH801', 'Topology II', 3.0, 'Theory')," +
                        "('MATH', 8, 'MATH802', 'Advanced Analysis', 3.0, 'Theory')," +
                        "('MATH', 8, 'MATH803', 'Research Project', 4.0, 'Lab')," +
                        "('MATH', 8, 'MATH804', 'Seminar', 2.0, 'Lab')," +
                        "('MATH', 8, 'MATH805', 'Advanced Mathematical Methods', 3.0, 'Theory')");
                
                // PHY - Physics
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('PHY', 1, 'PHY101', 'Mechanics', 3.0, 'Theory')," +
                        "('PHY', 1, 'PHY102', 'Physics Lab I', 2.0, 'Lab')," +
                        "('PHY', 1, 'PHY103', 'Chemistry I', 3.0, 'Theory')," +
                        "('PHY', 1, 'PHY104', 'Mathematics I', 3.0, 'Theory')," +
                        "('PHY', 1, 'PHY105', 'English Communication', 3.0, 'Theory')," +
                        "('PHY', 2, 'PHY201', 'Electromagnetism', 3.0, 'Theory')," +
                        "('PHY', 2, 'PHY202', 'Electromagnetism Lab', 2.0, 'Lab')," +
                        "('PHY', 2, 'PHY203', 'Chemistry II', 3.0, 'Theory')," +
                        "('PHY', 2, 'PHY204', 'Mathematics II', 3.0, 'Theory')," +
                        "('PHY', 3, 'PHY301', 'Thermodynamics', 3.0, 'Theory')," +
                        "('PHY', 3, 'PHY302', 'Thermodynamics Lab', 2.0, 'Lab')," +
                        "('PHY', 3, 'PHY303', 'Optics', 3.0, 'Theory')," +
                        "('PHY', 3, 'PHY304', 'Electronics I', 3.0, 'Theory')," +
                        "('PHY', 3, 'PHY305', 'Electronics Lab', 2.0, 'Lab')," +
                        "('PHY', 4, 'PHY401', 'Quantum Mechanics', 3.0, 'Theory')," +
                        "('PHY', 4, 'PHY402', 'Quantum Mechanics Lab', 2.0, 'Lab')," +
                        "('PHY', 4, 'PHY403', 'Modern Physics', 3.0, 'Theory')," +
                        "('PHY', 4, 'PHY404', 'Electronics II', 3.0, 'Theory')," +
                        "('PHY', 5, 'PHY501', 'Quantum Mechanics II', 3.0, 'Theory')," +
                        "('PHY', 5, 'PHY502', 'Nuclear Physics', 3.0, 'Theory')," +
                        "('PHY', 5, 'PHY503', 'Statistical Mechanics', 3.0, 'Theory')," +
                        "('PHY', 5, 'PHY504', 'Computational Physics', 3.0, 'Theory')," +
                        "('PHY', 6, 'PHY601', 'Solid State Physics', 3.0, 'Theory')," +
                        "('PHY', 6, 'PHY602', 'Plasma Physics', 3.0, 'Theory')," +
                        "('PHY', 6, 'PHY603', 'Advanced Electronics Lab', 2.0, 'Lab')," +
                        "('PHY', 6, 'PHY604', 'Research Project', 3.0, 'Lab')," +
                        "('PHY', 7, 'PHY701', 'Particle Physics', 3.0, 'Theory')," +
                        "('PHY', 7, 'PHY702', 'Atomic and Molecular Physics', 3.0, 'Theory')," +
                        "('PHY', 7, 'PHY703', 'Capstone Project', 4.0, 'Lab')," +
                        "('PHY', 7, 'PHY704', 'Advanced Lab', 2.0, 'Lab')," +
                        "('PHY', 8, 'PHY801', 'Condensed Matter Physics', 3.0, 'Theory')," +
                        "('PHY', 8, 'PHY802', 'Nuclear and Particle Lab', 2.0, 'Lab')," +
                        "('PHY', 8, 'PHY803', 'Final Project', 4.0, 'Lab')," +
                        "('PHY', 8, 'PHY804', 'Seminar', 2.0, 'Lab')," +
                        "('PHY', 8, 'PHY805', 'Advanced Quantum Mechanics', 3.0, 'Theory')");
                
                // CHE - Chemistry
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('CHE', 1, 'CHE101', 'General Chemistry I', 3.0, 'Theory')," +
                        "('CHE', 1, 'CHE102', 'Chemistry Lab I', 2.0, 'Lab')," +
                        "('CHE', 1, 'CHE103', 'Mathematics I', 3.0, 'Theory')," +
                        "('CHE', 1, 'CHE104', 'Physics I', 3.0, 'Theory')," +
                        "('CHE', 1, 'CHE105', 'English Communication', 3.0, 'Theory')," +
                        "('CHE', 2, 'CHE201', 'Organic Chemistry I', 3.0, 'Theory')," +
                        "('CHE', 2, 'CHE202', 'Organic Chemistry Lab', 2.0, 'Lab')," +
                        "('CHE', 2, 'CHE203', 'Mathematics II', 3.0, 'Theory')," +
                        "('CHE', 2, 'CHE204', 'Physics II', 3.0, 'Theory')," +
                        "('CHE', 2, 'CHE205', 'Physics Lab II', 2.0, 'Lab')," +
                        "('CHE', 3, 'CHE301', 'Physical Chemistry I', 3.0, 'Theory')," +
                        "('CHE', 3, 'CHE302', 'Physical Chemistry Lab', 2.0, 'Lab')," +
                        "('CHE', 3, 'CHE303', 'Analytical Chemistry', 3.0, 'Theory')," +
                        "('CHE', 3, 'CHE304', 'Inorganic Chemistry I', 3.0, 'Theory')," +
                        "('CHE', 3, 'CHE305', 'Chemistry Lab II', 2.0, 'Lab')," +
                        "('CHE', 4, 'CHE401', 'Physical Chemistry II', 3.0, 'Theory')," +
                        "('CHE', 4, 'CHE402', 'Physical Chemistry Lab II', 2.0, 'Lab')," +
                        "('CHE', 4, 'CHE403', 'Organic Chemistry II', 3.0, 'Theory')," +
                        "('CHE', 4, 'CHE404', 'Inorganic Chemistry II', 3.0, 'Theory')," +
                        "('CHE', 5, 'CHE501', 'Industrial Chemistry', 3.0, 'Theory')," +
                        "('CHE', 5, 'CHE502', 'Polymer Chemistry', 3.0, 'Theory')," +
                        "('CHE', 5, 'CHE503', 'Electrochemistry', 3.0, 'Theory')," +
                        "('CHE', 5, 'CHE504', 'Spectroscopy', 3.0, 'Theory')," +
                        "('CHE', 6, 'CHE601', 'Medicinal Chemistry', 3.0, 'Theory')," +
                        "('CHE', 6, 'CHE602', 'Environmental Chemistry', 3.0, 'Theory')," +
                        "('CHE', 6, 'CHE603', 'Advanced Organic Lab', 2.0, 'Lab')," +
                        "('CHE', 6, 'CHE604', 'Research Project', 3.0, 'Lab')," +
                        "('CHE', 7, 'CHE701', 'Computational Chemistry', 3.0, 'Theory')," +
                        "('CHE', 7, 'CHE702', 'Nanochemistry', 3.0, 'Theory')," +
                        "('CHE', 7, 'CHE703', 'Advanced Physical Lab', 2.0, 'Lab')," +
                        "('CHE', 7, 'CHE704', 'Capstone Project', 3.0, 'Lab')," +
                        "('CHE', 8, 'CHE801', 'Advanced Organic Chemistry', 3.0, 'Theory')," +
                        "('CHE', 8, 'CHE802', 'Advanced Inorganic Chemistry', 3.0, 'Theory')," +
                        "('CHE', 8, 'CHE803', 'Final Project', 4.0, 'Lab')," +
                        "('CHE', 8, 'CHE804', 'Advanced Physical Chemistry', 3.0, 'Theory')," +
                        "('CHE', 8, 'CHE805', 'Seminar', 2.0, 'Lab')");
                
                System.out.println("✓ CSE, EEE, SWE, ME, CE, MATH, PHY, CHE courses inserted");
                
                // Continue with remaining departments...
                // BAN - Bangla
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('BAN', 1, 'BAN101', 'Bangla Language I', 3.0, 'Theory')," +
                        "('BAN', 1, 'BAN102', 'Bangla Literature I', 3.0, 'Theory')," +
                        "('BAN', 1, 'BAN103', 'Bangla Grammar', 3.0, 'Theory')," +
                        "('BAN', 1, 'BAN104', 'Bangla Writing Lab', 2.0, 'Lab')," +
                        "('BAN', 1, 'BAN105', 'English Communication I', 3.0, 'Theory')," +
                        "('BAN', 2, 'BAN201', 'Bangla Language II', 3.0, 'Theory')," +
                        "('BAN', 2, 'BAN202', 'Bangla Literature II', 3.0, 'Theory')," +
                        "('BAN', 2, 'BAN203', 'Bangla Poetry', 3.0, 'Theory')," +
                        "('BAN', 2, 'BAN204', 'Bangla Drama Lab', 2.0, 'Lab')," +
                        "('BAN', 2, 'BAN205', 'English Communication II', 3.0, 'Theory')," +
                        "('BAN', 3, 'BAN301', 'Bangla Prose', 3.0, 'Theory')," +
                        "('BAN', 3, 'BAN302', 'Modern Bangla Literature', 3.0, 'Theory')," +
                        "('BAN', 3, 'BAN303', 'Bangla Essay Writing', 3.0, 'Theory')," +
                        "('BAN', 3, 'BAN304', 'Literature Lab', 2.0, 'Lab')," +
                        "('BAN', 4, 'BAN401', 'Bangla Classical Literature', 3.0, 'Theory')," +
                        "('BAN', 4, 'BAN402', 'Bangla Novel', 3.0, 'Theory')," +
                        "('BAN', 4, 'BAN403', 'Bangla Short Story', 3.0, 'Theory')," +
                        "('BAN', 4, 'BAN404', 'Bangla Writing Lab II', 2.0, 'Lab')," +
                        "('BAN', 5, 'BAN501', 'Bangla Linguistics', 3.0, 'Theory')," +
                        "('BAN', 5, 'BAN502', 'Bangla Criticism', 3.0, 'Theory')," +
                        "('BAN', 5, 'BAN503', 'Research Methodology', 3.0, 'Theory')," +
                        "('BAN', 5, 'BAN504', 'Bangla Drama', 3.0, 'Theory')," +
                        "('BAN', 5, 'BAN505', 'Bangla Prose Analysis', 3.0, 'Theory')," +
                        "('BAN', 6, 'BAN601', 'Advanced Bangla Grammar', 3.0, 'Theory')," +
                        "('BAN', 6, 'BAN602', 'Bangla Literature History', 3.0, 'Theory')," +
                        "('BAN', 6, 'BAN603', 'Lab III', 2.0, 'Lab')," +
                        "('BAN', 6, 'BAN604', 'Modern Bangla Poetry', 3.0, 'Theory')," +
                        "('BAN', 6, 'BAN605', 'Bangla Literary Criticism', 3.0, 'Theory')," +
                        "('BAN', 7, 'BAN701', 'Bangla Modern Poetry', 3.0, 'Theory')," +
                        "('BAN', 7, 'BAN702', 'Bangla Novel Analysis', 3.0, 'Theory')," +
                        "('BAN', 7, 'BAN703', 'Project Work I', 3.0, 'Lab')," +
                        "('BAN', 7, 'BAN704', 'Bangla Short Story Analysis', 3.0, 'Theory')," +
                        "('BAN', 7, 'BAN705', 'Bangla Essay Writing II', 3.0, 'Theory')," +
                        "('BAN', 8, 'BAN801', 'Project Work II', 5.0, 'Lab')," +
                        "('BAN', 8, 'BAN802', 'Seminar', 3.0, 'Theory')," +
                        "('BAN', 8, 'BAN803', 'Advanced Bangla Literature', 3.0, 'Theory')," +
                        "('BAN', 8, 'BAN804', 'Bangla Research Methods', 3.0, 'Theory')");
                
                // ENG - English
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('ENG', 1, 'ENG101', 'English Language I', 3.0, 'Theory')," +
                        "('ENG', 1, 'ENG102', 'English Literature I', 3.0, 'Theory')," +
                        "('ENG', 1, 'ENG103', 'Communication Skills I', 3.0, 'Theory')," +
                        "('ENG', 1, 'ENG104', 'English Lab I', 2.0, 'Lab')," +
                        "('ENG', 1, 'ENG105', 'Grammar I', 3.0, 'Theory')," +
                        "('ENG', 2, 'ENG201', 'English Language II', 3.0, 'Theory')," +
                        "('ENG', 2, 'ENG202', 'English Literature II', 3.0, 'Theory')," +
                        "('ENG', 2, 'ENG203', 'Communication Skills II', 3.0, 'Theory')," +
                        "('ENG', 2, 'ENG204', 'English Lab II', 2.0, 'Lab')," +
                        "('ENG', 3, 'ENG301', 'British Literature', 3.0, 'Theory')," +
                        "('ENG', 3, 'ENG302', 'American Literature', 3.0, 'Theory')," +
                        "('ENG', 3, 'ENG303', 'Creative Writing', 3.0, 'Theory')," +
                        "('ENG', 3, 'ENG304', 'Literature Lab', 2.0, 'Lab')," +
                        "('ENG', 4, 'ENG401', 'Linguistics', 3.0, 'Theory')," +
                        "('ENG', 4, 'ENG402', 'Modern Poetry', 3.0, 'Theory')," +
                        "('ENG', 4, 'ENG403', 'Drama Studies', 3.0, 'Theory')," +
                        "('ENG', 4, 'ENG404', 'Lab II', 2.0, 'Lab')," +
                        "('ENG', 5, 'ENG501', 'Research Methods', 3.0, 'Theory')," +
                        "('ENG', 5, 'ENG502', 'Advanced Grammar', 3.0, 'Theory')," +
                        "('ENG', 5, 'ENG503', 'Elective I', 3.0, 'Theory')," +
                        "('ENG', 5, 'ENG504', 'Elective II', 3.0, 'Theory')," +
                        "('ENG', 5, 'ENG505', 'Literary Theory', 3.0, 'Theory')," +
                        "('ENG', 6, 'ENG601', 'Shakespearean Studies', 3.0, 'Theory')," +
                        "('ENG', 6, 'ENG602', 'Linguistics II', 3.0, 'Theory')," +
                        "('ENG', 6, 'ENG603', 'Lab III', 2.0, 'Lab')," +
                        "('ENG', 6, 'ENG604', 'Modern Drama', 3.0, 'Theory')," +
                        "('ENG', 6, 'ENG605', 'Postcolonial Literature', 3.0, 'Theory')," +
                        "('ENG', 7, 'ENG701', 'World Literature', 3.0, 'Theory')," +
                        "('ENG', 7, 'ENG702', 'Project Work I', 3.0, 'Lab')," +
                        "('ENG', 7, 'ENG703', 'Elective III', 3.0, 'Theory')," +
                        "('ENG', 7, 'ENG704', 'Seminar I', 3.0, 'Theory')," +
                        "('ENG', 8, 'ENG801', 'Project Work II', 5.0, 'Lab')," +
                        "('ENG', 8, 'ENG802', 'Seminar', 3.0, 'Theory')," +
                        "('ENG', 8, 'ENG803', 'Advanced Literary Criticism', 3.0, 'Theory')," +
                        "('ENG', 8, 'ENG804', 'Contemporary Literature', 3.0, 'Theory')");
                
                // SOC - Sociology
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('SOC', 1, 'SOC101', 'Introduction to Sociology', 3.0, 'Theory')," +
                        "('SOC', 1, 'SOC102', 'Social Anthropology', 3.0, 'Theory')," +
                        "('SOC', 1, 'SOC103', 'Sociology Lab I', 2.0, 'Lab')," +
                        "('SOC', 1, 'SOC104', 'Economics Fundamentals', 3.0, 'Theory')," +
                        "('SOC', 1, 'SOC105', 'English Communication', 3.0, 'Theory')," +
                        "('SOC', 2, 'SOC201', 'Sociological Theories', 3.0, 'Theory')," +
                        "('SOC', 2, 'SOC202', 'Research Methodology', 3.0, 'Theory')," +
                        "('SOC', 2, 'SOC203', 'Lab II', 2.0, 'Lab')," +
                        "('SOC', 2, 'SOC204', 'Social Problems', 3.0, 'Theory')," +
                        "('SOC', 2, 'SOC205', 'Development Studies', 3.0, 'Theory')," +
                        "('SOC', 3, 'SOC301', 'Urban Sociology', 3.0, 'Theory')," +
                        "('SOC', 3, 'SOC302', 'Rural Sociology', 3.0, 'Theory')," +
                        "('SOC', 3, 'SOC303', 'Lab III', 2.0, 'Lab')," +
                        "('SOC', 3, 'SOC304', 'Elective I', 3.0, 'Theory')," +
                        "('SOC', 3, 'SOC305', 'Social Psychology', 3.0, 'Theory')," +
                        "('SOC', 4, 'SOC401', 'Political Sociology', 3.0, 'Theory')," +
                        "('SOC', 4, 'SOC402', 'Sociology of Religion', 3.0, 'Theory')," +
                        "('SOC', 4, 'SOC403', 'Field Work I', 3.0, 'Lab')," +
                        "('SOC', 4, 'SOC404', 'Elective II', 3.0, 'Theory')," +
                        "('SOC', 4, 'SOC405', 'Gender Studies', 3.0, 'Theory')," +
                        "('SOC', 5, 'SOC501', 'Industrial Sociology', 3.0, 'Theory')," +
                        "('SOC', 5, 'SOC502', 'Social Research', 3.0, 'Theory')," +
                        "('SOC', 5, 'SOC503', 'Elective III', 3.0, 'Theory')," +
                        "('SOC', 5, 'SOC504', 'Project Work I', 3.0, 'Lab')," +
                        "('SOC', 5, 'SOC505', 'Criminology', 3.0, 'Theory')," +
                        "('SOC', 6, 'SOC601', 'Population Studies', 3.0, 'Theory')," +
                        "('SOC', 6, 'SOC602', 'Social Policy', 3.0, 'Theory')," +
                        "('SOC', 6, 'SOC603', 'Lab IV', 2.0, 'Lab')," +
                        "('SOC', 6, 'SOC604', 'Elective IV', 3.0, 'Theory')," +
                        "('SOC', 6, 'SOC605', 'Environmental Sociology', 3.0, 'Theory')," +
                        "('SOC', 7, 'SOC701', 'Project Work II', 5.0, 'Lab')," +
                        "('SOC', 7, 'SOC702', 'Seminar I', 3.0, 'Theory')," +
                        "('SOC', 7, 'SOC703', 'Elective V', 3.0, 'Theory')," +
                        "('SOC', 7, 'SOC704', 'Advanced Research Methods', 3.0, 'Theory')," +
                        "('SOC', 8, 'SOC801', 'Project Work III', 5.0, 'Lab')," +
                        "('SOC', 8, 'SOC802', 'Seminar II', 3.0, 'Theory')," +
                        "('SOC', 8, 'SOC803', 'Elective VI', 3.0, 'Theory')," +
                        "('SOC', 8, 'SOC804', 'Contemporary Social Issues', 3.0, 'Theory')");
                
                // ANP - Anthropology
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('ANP', 1, 'ANP101', 'Introduction to Anthropology', 3.0, 'Theory')," +
                        "('ANP', 1, 'ANP102', 'Human Evolution', 3.0, 'Theory')," +
                        "('ANP', 1, 'ANP103', 'Anthropology Lab I', 2.0, 'Lab')," +
                        "('ANP', 1, 'ANP104', 'Biology Fundamentals', 3.0, 'Theory')," +
                        "('ANP', 1, 'ANP105', 'Sociology Basics', 3.0, 'Theory')," +
                        "('ANP', 2, 'ANP201', 'Cultural Anthropology', 3.0, 'Theory')," +
                        "('ANP', 2, 'ANP202', 'Social Anthropology', 3.0, 'Theory')," +
                        "('ANP', 2, 'ANP203', 'Lab II', 2.0, 'Lab')," +
                        "('ANP', 2, 'ANP204', 'History of Civilization', 3.0, 'Theory')," +
                        "('ANP', 2, 'ANP205', 'Sociological Methods', 3.0, 'Theory')," +
                        "('ANP', 3, 'ANP301', 'Archaeology', 3.0, 'Theory')," +
                        "('ANP', 3, 'ANP302', 'Anthropometry', 2.0, 'Lab')," +
                        "('ANP', 3, 'ANP303', 'Linguistic Anthropology', 3.0, 'Theory')," +
                        "('ANP', 3, 'ANP304', 'Research Methodology', 3.0, 'Theory')," +
                        "('ANP', 4, 'ANP401', 'Medical Anthropology', 3.0, 'Theory')," +
                        "('ANP', 4, 'ANP402', 'Forensic Anthropology', 3.0, 'Theory')," +
                        "('ANP', 4, 'ANP403', 'Lab III', 2.0, 'Lab')," +
                        "('ANP', 4, 'ANP404', 'Applied Anthropology', 3.0, 'Theory')," +
                        "('ANP', 5, 'ANP501', 'Population Studies', 3.0, 'Theory')," +
                        "('ANP', 5, 'ANP502', 'Anthropological Theories', 3.0, 'Theory')," +
                        "('ANP', 5, 'ANP503', 'Field Study I', 3.0, 'Lab')," +
                        "('ANP', 5, 'ANP504', 'Elective I', 3.0, 'Theory')," +
                        "('ANP', 5, 'ANP505', 'Medical Anthropology', 3.0, 'Theory')," +
                        "('ANP', 6, 'ANP601', 'Urban Anthropology', 3.0, 'Theory')," +
                        "('ANP', 6, 'ANP602', 'Ethnography', 3.0, 'Theory')," +
                        "('ANP', 6, 'ANP603', 'Lab IV', 2.0, 'Lab')," +
                        "('ANP', 6, 'ANP604', 'Elective II', 3.0, 'Theory')," +
                        "('ANP', 6, 'ANP605', 'Forensic Anthropology', 3.0, 'Theory')," +
                        "('ANP', 7, 'ANP701', 'Project Work I', 5.0, 'Lab')," +
                        "('ANP', 7, 'ANP702', 'Seminar I', 3.0, 'Theory')," +
                        "('ANP', 7, 'ANP703', 'Elective III', 3.0, 'Theory')," +
                        "('ANP', 7, 'ANP704', 'Applied Anthropology', 3.0, 'Theory')," +
                        "('ANP', 8, 'ANP801', 'Project Work II', 5.0, 'Lab')," +
                        "('ANP', 8, 'ANP802', 'Seminar II', 3.0, 'Theory')," +
                        "('ANP', 8, 'ANP803', 'Elective IV', 3.0, 'Theory')," +
                        "('ANP', 8, 'ANP804', 'Advanced Research Methods', 3.0, 'Theory')");
                
                // PAD - Public Administration
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('PAD', 1, 'PAD101', 'Introduction to Public Administration', 3.0, 'Theory')," +
                        "('PAD', 1, 'PAD102', 'Political Science Basics', 3.0, 'Theory')," +
                        "('PAD', 1, 'PAD103', 'Governance Lab I', 2.0, 'Lab')," +
                        "('PAD', 1, 'PAD104', 'Economics Fundamentals', 3.0, 'Theory')," +
                        "('PAD', 1, 'PAD105', 'Sociology Basics', 3.0, 'Theory')," +
                        "('PAD', 2, 'PAD201', 'Organizational Theory', 3.0, 'Theory')," +
                        "('PAD', 2, 'PAD202', 'Public Policy I', 3.0, 'Theory')," +
                        "('PAD', 2, 'PAD203', 'Lab II', 2.0, 'Lab')," +
                        "('PAD', 2, 'PAD204', 'Development Economics', 3.0, 'Theory')," +
                        "('PAD', 3, 'PAD301', 'Public Policy II', 3.0, 'Theory')," +
                        "('PAD', 3, 'PAD302', 'Human Resource Management', 3.0, 'Theory')," +
                        "('PAD', 3, 'PAD303', 'Project Planning', 3.0, 'Theory')," +
                        "('PAD', 3, 'PAD304', 'Lab III', 2.0, 'Lab')," +
                        "('PAD', 4, 'PAD401', 'Administrative Law', 3.0, 'Theory')," +
                        "('PAD', 4, 'PAD402', 'Ethics in Public Service', 3.0, 'Theory')," +
                        "('PAD', 4, 'PAD403', 'Field Work I', 3.0, 'Lab')," +
                        "('PAD', 5, 'PAD501', 'Urban Governance', 3.0, 'Theory')," +
                        "('PAD', 5, 'PAD502', 'Rural Administration', 3.0, 'Theory')," +
                        "('PAD', 5, 'PAD503', 'Elective II', 3.0, 'Theory')," +
                        "('PAD', 5, 'PAD504', 'Research Methodology', 3.0, 'Theory')," +
                        "('PAD', 5, 'PAD505', 'Public Policy Analysis', 3.0, 'Theory')," +
                        "('PAD', 6, 'PAD601', 'Public Finance', 3.0, 'Theory')," +
                        "('PAD', 6, 'PAD602', 'Project Work I', 3.0, 'Lab')," +
                        "('PAD', 6, 'PAD603', 'Elective III', 3.0, 'Theory')," +
                        "('PAD', 6, 'PAD604', 'Lab IV', 2.0, 'Lab')," +
                        "('PAD', 6, 'PAD605', 'Local Government', 3.0, 'Theory')," +
                        "('PAD', 7, 'PAD701', 'Project Work II', 5.0, 'Lab')," +
                        "('PAD', 7, 'PAD702', 'Seminar I', 3.0, 'Theory')," +
                        "('PAD', 7, 'PAD703', 'Elective IV', 3.0, 'Theory')," +
                        "('PAD', 7, 'PAD704', 'Public Administration Ethics', 3.0, 'Theory')," +
                        "('PAD', 8, 'PAD801', 'Project Work III', 5.0, 'Lab')," +
                        "('PAD', 8, 'PAD802', 'Seminar II', 3.0, 'Theory')," +
                        "('PAD', 8, 'PAD803', 'Elective V', 3.0, 'Theory')," +
                        "('PAD', 8, 'PAD804', 'Advanced Public Policy', 3.0, 'Theory')");
                
                // BMB - Biochemistry and Molecular Biology
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('BMB', 1, 'BMB101', 'Introduction to Biochemistry', 3.0, 'Theory')," +
                        "('BMB', 1, 'BMB102', 'Biochemistry Lab I', 2.0, 'Lab')," +
                        "('BMB', 1, 'BMB103', 'Cell Biology', 3.0, 'Theory')," +
                        "('BMB', 1, 'BMB104', 'Chemistry I', 3.0, 'Theory')," +
                        "('BMB', 1, 'BMB105', 'Chemistry Lab I', 2.0, 'Lab')," +
                        "('BMB', 2, 'BMB201', 'Molecular Biology I', 3.0, 'Theory')," +
                        "('BMB', 2, 'BMB202', 'Molecular Biology Lab I', 2.0, 'Lab')," +
                        "('BMB', 2, 'BMB203', 'Genetics I', 3.0, 'Theory')," +
                        "('BMB', 2, 'BMB204', 'Genetics Lab I', 2.0, 'Lab')," +
                        "('BMB', 3, 'BMB301', 'Enzymology', 3.0, 'Theory')," +
                        "('BMB', 3, 'BMB302', 'Protein Chemistry Lab', 2.0, 'Lab')," +
                        "('BMB', 3, 'BMB303', 'Metabolism I', 3.0, 'Theory')," +
                        "('BMB', 3, 'BMB304', 'Advanced Molecular Lab', 2.0, 'Lab')," +
                        "('BMB', 4, 'BMB401', 'Metabolism II', 3.0, 'Theory')," +
                        "('BMB', 4, 'BMB402', 'Metabolism Lab', 2.0, 'Lab')," +
                        "('BMB', 4, 'BMB403', 'Immunology I', 3.0, 'Theory')," +
                        "('BMB', 4, 'BMB404', 'Advanced Biochemistry Lab', 2.0, 'Lab')," +
                        "('BMB', 4, 'BMB405', 'Biostatistics', 3.0, 'Theory')," +
                        "('BMB', 5, 'BMB501', 'Molecular Genetics', 3.0, 'Theory')," +
                        "('BMB', 5, 'BMB502', 'Cell Signaling', 3.0, 'Theory')," +
                        "('BMB', 5, 'BMB503', 'Lab Techniques I', 2.0, 'Lab')," +
                        "('BMB', 5, 'BMB504', 'Protein Engineering', 3.0, 'Theory')," +
                        "('BMB', 5, 'BMB505', 'Advanced Lab I', 2.0, 'Lab')," +
                        "('BMB', 6, 'BMB601', 'Immunology II', 3.0, 'Theory')," +
                        "('BMB', 6, 'BMB602', 'Metabolomics Lab', 2.0, 'Lab')," +
                        "('BMB', 6, 'BMB603', 'Molecular Diagnostics', 3.0, 'Theory')," +
                        "('BMB', 6, 'BMB604', 'Advanced Bioinformatics', 3.0, 'Theory')," +
                        "('BMB', 6, 'BMB605', 'Biochemistry Research Methods', 3.0, 'Theory')," +
                        "('BMB', 7, 'BMB701', 'Research Project I', 4.0, 'Lab')," +
                        "('BMB', 7, 'BMB702', 'Clinical Biochemistry', 3.0, 'Theory')," +
                        "('BMB', 7, 'BMB703', 'Advanced Molecular Lab II', 2.0, 'Lab')," +
                        "('BMB', 7, 'BMB704', 'Seminar', 2.0, 'Lab')," +
                        "('BMB', 8, 'BMB801', 'Capstone Project II', 4.0, 'Lab')," +
                        "('BMB', 8, 'BMB802', 'Recent Advances in BMB', 3.0, 'Theory')," +
                        "('BMB', 8, 'BMB803', 'Seminar II', 2.0, 'Lab')," +
                        "('BMB', 8, 'BMB804', 'Ethics in Biochemistry', 3.0, 'Theory')");
                
                // GE - Genetic Engineering
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('GE', 1, 'GE101', 'Introduction to Genetics', 3.0, 'Theory')," +
                        "('GE', 1, 'GE102', 'Genetics Lab I', 2.0, 'Lab')," +
                        "('GE', 1, 'GE103', 'Cell Biology', 3.0, 'Theory')," +
                        "('GE', 1, 'GE104', 'Chemistry I', 3.0, 'Theory')," +
                        "('GE', 1, 'GE105', 'Chemistry Lab I', 2.0, 'Lab')," +
                        "('GE', 2, 'GE201', 'Molecular Biology I', 3.0, 'Theory')," +
                        "('GE', 2, 'GE202', 'Molecular Biology Lab I', 2.0, 'Lab')," +
                        "('GE', 2, 'GE203', 'Biochemistry I', 3.0, 'Theory')," +
                        "('GE', 2, 'GE204', 'Biochemistry Lab I', 2.0, 'Lab')," +
                        "('GE', 3, 'GE301', 'Genomics', 3.0, 'Theory')," +
                        "('GE', 3, 'GE302', 'Proteomics Lab', 2.0, 'Lab')," +
                        "('GE', 3, 'GE303', 'Cell Culture Techniques', 2.0, 'Lab')," +
                        "('GE', 3, 'GE304', 'Bioinformatics I', 3.0, 'Theory')," +
                        "('GE', 4, 'GE401', 'Molecular Biology II', 3.0, 'Theory')," +
                        "('GE', 4, 'GE402', 'Molecular Biology Lab II', 2.0, 'Lab')," +
                        "('GE', 4, 'GE403', 'Genetic Engineering Techniques', 3.0, 'Theory')," +
                        "('GE', 4, 'GE404', 'Bioinformatics II', 3.0, 'Theory')," +
                        "('GE', 4, 'GE405', 'Lab Techniques II', 2.0, 'Lab')," +
                        "('GE', 5, 'GE501', 'Recombinant DNA Technology', 3.0, 'Theory')," +
                        "('GE', 5, 'GE502', 'Immunology', 3.0, 'Theory')," +
                        "('GE', 5, 'GE503', 'Molecular Diagnostics', 3.0, 'Theory')," +
                        "('GE', 5, 'GE504', 'Lab Research I', 2.0, 'Lab')," +
                        "('GE', 5, 'GE505', 'Biostatistics', 3.0, 'Theory')," +
                        "('GE', 6, 'GE601', 'Plant Genetic Engineering', 3.0, 'Theory')," +
                        "('GE', 6, 'GE602', 'Animal Genetic Engineering', 3.0, 'Theory')," +
                        "('GE', 6, 'GE603', 'Lab Research II', 2.0, 'Lab')," +
                        "('GE', 6, 'GE604', 'Advanced Bioinformatics', 3.0, 'Theory')," +
                        "('GE', 6, 'GE605', 'Genomics Applications', 3.0, 'Theory')," +
                        "('GE', 7, 'GE701', 'Clinical Genetics', 3.0, 'Theory')," +
                        "('GE', 7, 'GE702', 'Research Project I', 4.0, 'Lab')," +
                        "('GE', 7, 'GE703', 'Advanced Molecular Lab', 2.0, 'Lab')," +
                        "('GE', 7, 'GE704', 'Ethics in Genetics', 3.0, 'Theory')," +
                        "('GE', 8, 'GE801', 'Capstone Project II', 4.0, 'Lab')," +
                        "('GE', 8, 'GE802', 'Advanced Techniques in Genetics', 3.0, 'Theory')," +
                        "('GE', 8, 'GE803', 'Seminar', 2.0, 'Lab')," +
                        "('GE', 8, 'GE804', 'Recent Trends in Genetic Engineering', 3.0, 'Theory')");
                
                // GEO - Geology
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('GEO', 1, 'GEO101', 'Physical Geology', 3.0, 'Theory')," +
                        "('GEO', 1, 'GEO102', 'Geology Lab I', 2.0, 'Lab')," +
                        "('GEO', 1, 'GEO103', 'Mathematics I', 3.0, 'Theory')," +
                        "('GEO', 1, 'GEO104', 'Physics I', 3.0, 'Theory')," +
                        "('GEO', 1, 'GEO105', 'Physics Lab I', 2.0, 'Lab')," +
                        "('GEO', 2, 'GEO201', 'Mineralogy', 3.0, 'Theory')," +
                        "('GEO', 2, 'GEO202', 'Mineralogy Lab', 2.0, 'Lab')," +
                        "('GEO', 2, 'GEO203', 'Chemistry I', 3.0, 'Theory')," +
                        "('GEO', 2, 'GEO204', 'Chemistry Lab I', 2.0, 'Lab')," +
                        "('GEO', 3, 'GEO301', 'Petrology', 3.0, 'Theory')," +
                        "('GEO', 3, 'GEO302', 'Petrology Lab', 2.0, 'Lab')," +
                        "('GEO', 3, 'GEO303', 'Structural Geology', 3.0, 'Theory')," +
                        "('GEO', 3, 'GEO304', 'Geomorphology', 3.0, 'Theory')," +
                        "('GEO', 4, 'GEO401', 'Sedimentology', 3.0, 'Theory')," +
                        "('GEO', 4, 'GEO402', 'Sedimentology Lab', 2.0, 'Lab')," +
                        "('GEO', 4, 'GEO403', 'Paleontology', 3.0, 'Theory')," +
                        "('GEO', 4, 'GEO404', 'Geochemistry', 3.0, 'Theory')," +
                        "('GEO', 4, 'GEO405', 'Field Work II', 2.0, 'Lab')," +
                        "('GEO', 5, 'GEO501', 'Environmental Geology', 3.0, 'Theory')," +
                        "('GEO', 5, 'GEO502', 'Geophysics I', 3.0, 'Theory')," +
                        "('GEO', 5, 'GEO503', 'Remote Sensing I', 3.0, 'Theory')," +
                        "('GEO', 5, 'GEO504', 'Field Survey', 2.0, 'Lab')," +
                        "('GEO', 5, 'GEO505', 'Mineral Exploration Lab', 2.0, 'Lab')," +
                        "('GEO', 6, 'GEO601', 'Geophysics II', 3.0, 'Theory')," +
                        "('GEO', 6, 'GEO602', 'Remote Sensing II', 3.0, 'Theory')," +
                        "('GEO', 6, 'GEO603', 'Hydrogeology', 3.0, 'Theory')," +
                        "('GEO', 6, 'GEO604', 'Field Work III', 2.0, 'Lab')," +
                        "('GEO', 6, 'GEO605', 'Geological Mapping', 3.0, 'Theory')," +
                        "('GEO', 7, 'GEO701', 'Engineering Geology', 3.0, 'Theory')," +
                        "('GEO', 7, 'GEO702', 'Capstone Project I', 4.0, 'Lab')," +
                        "('GEO', 7, 'GEO703', 'Environmental Field Lab', 2.0, 'Lab')," +
                        "('GEO', 7, 'GEO704', 'Advanced Geochemistry', 3.0, 'Theory')," +
                        "('GEO', 8, 'GEO801', 'Capstone Project II', 4.0, 'Lab')," +
                        "('GEO', 8, 'GEO802', 'Advanced Structural Geology', 3.0, 'Theory')," +
                        "('GEO', 8, 'GEO803', 'Seminar', 2.0, 'Lab')," +
                        "('GEO', 8, 'GEO804', 'Research Methodology', 3.0, 'Theory')");
                
                // FET - Food Engineering and Technology
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('FET', 1, 'FET101', 'Introduction to Food Engineering', 3.0, 'Theory')," +
                        "('FET', 1, 'FET102', 'Basic Food Lab', 2.0, 'Lab')," +
                        "('FET', 1, 'FET103', 'General Biology', 3.0, 'Theory')," +
                        "('FET', 1, 'FET104', 'General Chemistry', 3.0, 'Theory')," +
                        "('FET', 1, 'FET105', 'Mathematics I', 3.0, 'Theory')," +
                        "('FET', 2, 'FET201', 'Food Chemistry I', 3.0, 'Theory')," +
                        "('FET', 2, 'FET202', 'Food Microbiology I', 3.0, 'Theory')," +
                        "('FET', 2, 'FET203', 'Unit Operations in Food Eng', 3.0, 'Theory')," +
                        "('FET', 2, 'FET204', 'Food Lab II', 2.0, 'Lab')," +
                        "('FET', 3, 'FET301', 'Food Chemistry II', 3.0, 'Theory')," +
                        "('FET', 3, 'FET302', 'Food Microbiology II', 3.0, 'Theory')," +
                        "('FET', 3, 'FET303', 'Thermodynamics in Food Process', 3.0, 'Theory')," +
                        "('FET', 3, 'FET304', 'Mass and Heat Transfer', 3.0, 'Theory')," +
                        "('FET', 4, 'FET401', 'Food Engineering Equipments', 3.0, 'Theory')," +
                        "('FET', 4, 'FET402', 'Quality Assurance', 3.0, 'Theory')," +
                        "('FET', 4, 'FET403', 'Unit Operations II', 3.0, 'Theory')," +
                        "('FET', 4, 'FET404', 'Instrumentation Lab', 2.0, 'Lab')," +
                        "('FET', 4, 'FET405', 'Process Control', 3.0, 'Theory')," +
                        "('FET', 5, 'FET501', 'Food Packaging', 3.0, 'Theory')," +
                        "('FET', 5, 'FET502', 'Food Biotechnology', 3.0, 'Theory')," +
                        "('FET', 5, 'FET503', 'Quality Control Lab', 2.0, 'Lab')," +
                        "('FET', 5, 'FET504', 'Process Design', 3.0, 'Theory')," +
                        "('FET', 5, 'FET505', 'Elective I', 3.0, 'Theory')," +
                        "('FET', 6, 'FET601', 'Advanced Food Processing', 3.0, 'Theory')," +
                        "('FET', 6, 'FET602', 'Industrial Microbiology', 3.0, 'Theory')," +
                        "('FET', 6, 'FET603', 'Food Product Development', 3.0, 'Theory')," +
                        "('FET', 6, 'FET604', 'Food Lab VI', 2.0, 'Lab')," +
                        "('FET', 6, 'FET605', 'Food Preservation Technology', 3.0, 'Theory')," +
                        "('FET', 7, 'FET701', 'Entrepreneurship in Food Industry', 3.0, 'Theory')," +
                        "('FET', 7, 'FET702', 'Project Management', 3.0, 'Theory')," +
                        "('FET', 7, 'FET703', 'Elective I', 3.0, 'Theory')," +
                        "('FET', 7, 'FET704', 'Industrial Training', 3.0, 'Lab')," +
                        "('FET', 8, 'FET801', 'Final Project', 5.0, 'Lab')," +
                        "('FET', 8, 'FET802', 'Food Safety and Regulations', 3.0, 'Theory')," +
                        "('FET', 8, 'FET803', 'Elective II', 3.0, 'Theory')," +
                        "('FET', 8, 'FET804', 'Advanced Quality Assurance', 3.0, 'Theory')");
                
                // CEP - Chemical Engineering (Polymer)
                st.execute("INSERT IGNORE INTO department_courses VALUES " +
                        "('CEP', 1, 'CEP101', 'Introduction to Chemical Engineering', 3.0, 'Theory')," +
                        "('CEP', 1, 'CEP102', 'Chemical Engineering Lab I', 2.0, 'Lab')," +
                        "('CEP', 1, 'CEP103', 'Chemistry I', 3.0, 'Theory')," +
                        "('CEP', 1, 'CEP104', 'Mathematics I', 3.0, 'Theory')," +
                        "('CEP', 1, 'CEP105', 'Physics I', 3.0, 'Theory')," +
                        "('CEP', 2, 'CEP201', 'Material Science', 3.0, 'Theory')," +
                        "('CEP', 2, 'CEP202', 'Material Science Lab', 2.0, 'Lab')," +
                        "('CEP', 2, 'CEP203', 'Organic Chemistry', 3.0, 'Theory')," +
                        "('CEP', 2, 'CEP204', 'Organic Chemistry Lab', 2.0, 'Lab')," +
                        "('CEP', 3, 'CEP301', 'Fluid Mechanics', 3.0, 'Theory')," +
                        "('CEP', 3, 'CEP302', 'Fluid Mechanics Lab', 2.0, 'Lab')," +
                        "('CEP', 3, 'CEP303', 'Thermodynamics I', 3.0, 'Theory')," +
                        "('CEP', 3, 'CEP304', 'Polymer Science I', 3.0, 'Theory')," +
                        "('CEP', 4, 'CEP401', 'Heat and Mass Transfer', 3.0, 'Theory')," +
                        "('CEP', 4, 'CEP402', 'Heat and Mass Transfer Lab', 2.0, 'Lab')," +
                        "('CEP', 4, 'CEP403', 'Thermodynamics II', 3.0, 'Theory')," +
                        "('CEP', 4, 'CEP404', 'Polymer Science II', 3.0, 'Theory')," +
                        "('CEP', 4, 'CEP405', 'Chemical Process Lab', 2.0, 'Lab')," +
                        "('CEP', 5, 'CEP501', 'Reaction Engineering I', 3.0, 'Theory')," +
                        "('CEP', 5, 'CEP502', 'Chemical Process Equipment', 3.0, 'Theory')," +
                        "('CEP', 5, 'CEP503', 'Polymer Processing I', 3.0, 'Theory')," +
                        "('CEP', 5, 'CEP504', 'Lab Techniques I', 2.0, 'Lab')," +
                        "('CEP', 5, 'CEP505', 'Process Control I', 3.0, 'Theory')," +
                        "('CEP', 6, 'CEP601', 'Reaction Engineering II', 3.0, 'Theory')," +
                        "('CEP', 6, 'CEP602', 'Polymer Processing II', 3.0, 'Theory')," +
                        "('CEP', 6, 'CEP603', 'Process Control II', 3.0, 'Theory')," +
                        "('CEP', 6, 'CEP604', 'Lab Techniques II', 2.0, 'Lab')," +
                        "('CEP', 6, 'CEP605', 'Chemical Plant Design', 3.0, 'Theory')," +
                        "('CEP', 7, 'CEP701', 'Process Optimization', 3.0, 'Theory')," +
                        "('CEP', 7, 'CEP702', 'Research Project I', 4.0, 'Lab')," +
                        "('CEP', 7, 'CEP703', 'Advanced Polymer Lab', 2.0, 'Lab')," +
                        "('CEP', 7, 'CEP704', 'Environmental Engineering', 3.0, 'Theory')," +
                        "('CEP', 8, 'CEP801', 'Capstone Project II', 4.0, 'Lab')," +
                        "('CEP', 8, 'CEP802', 'Recent Trends in Chemical Engineering', 3.0, 'Theory')," +
                        "('CEP', 8, 'CEP803', 'Seminar', 2.0, 'Lab')," +
                        "('CEP', 8, 'CEP804', 'Industrial Safety and Ethics', 3.0, 'Theory')");
                
                System.out.println("✓ All department courses inserted (5-7 courses per semester for all departments)");
            }

            // Insert sample students (5 students) - always try to insert (INSERT IGNORE prevents duplicates)
            System.out.println("Inserting sample student data...");
            st.execute("INSERT IGNORE INTO student (name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path) VALUES " +
                    "('Ahmed Rahman', 'Mohammad Rahman', '2022-331-001', '2004-01-15', 'Dhaka, Bangladesh', '01712345678', 'ahmed.rahman@university.edu', '4.50', '4.80', 'B.Tech', 'CSE', '')," +
                    "('Fatima Khan', 'Hassan Khan', '2022-331-002', '2004-03-20', 'Chittagong, Bangladesh', '01712345679', 'fatima.khan@university.edu', '4.60', '4.75', 'B.Tech', 'CSE', '')," +
                    "('Karim Ali', 'Rashid Ali', '2022-331-003', '2004-05-10', 'Sylhet, Bangladesh', '01712345680', 'karim.ali@university.edu', '4.40', '4.70', 'B.Tech', 'CSE', '')," +
                    "('Sara Ahmed', 'Ibrahim Ahmed', '2022-332-001', '2004-02-25', 'Rajshahi, Bangladesh', '01712345681', 'sara.ahmed@university.edu', '4.55', '4.85', 'B.Tech', 'EEE', '')," +
                    "('Rashid Hasan', 'Nurul Hasan', '2022-332-002', '2004-04-12', 'Khulna, Bangladesh', '01712345682', 'rashid.hasan@university.edu', '4.45', '4.65', 'B.Tech', 'EEE', '')");
            System.out.println("✓ Sample students inserted");

            // Insert sample teachers
            rs = st.executeQuery("SELECT COUNT(*) FROM teacher");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting sample teacher data...");
                st.execute("INSERT IGNORE INTO teacher (name, fname, empId, dob, address, phone, email, bsc_in_sub, msc_in_sub, cgpa_in_bsc, cgpa_in_msc, phd, department, position, photo_path) VALUES " +
                        "('Dr. Mohammad Ali', 'Ali Rahman', 'T001', '1975-05-15', 'Dhaka, Bangladesh', '01711111111', 'mohammad.ali@university.edu', 'Computer Science', 'Computer Science', '3.80', '3.90', 'Yes', 'CSE', 'Professor', '')," +
                        "('Dr. Fatima Rahman', 'Rahman Khan', 'T002', '1978-08-20', 'Chittagong, Bangladesh', '01711111112', 'fatima.rahman@university.edu', 'Electrical Engineering', 'Electrical Engineering', '3.75', '3.85', 'Yes', 'EEE', 'Associate Professor', '')," +
                        "('Dr. Karim Hassan', 'Hassan Ali', 'T003', '1976-03-10', 'Sylhet, Bangladesh', '01711111113', 'karim.hassan@university.edu', 'Mechanical Engineering', 'Mechanical Engineering', '3.70', '3.80', 'Yes', 'ME', 'Professor', '')," +
                        "('Dr. Ayesha Khan', 'Khan Ahmed', 'T004', '1980-11-25', 'Rajshahi, Bangladesh', '01711111114', 'ayesha.khan@university.edu', 'Software Engineering', 'Software Engineering', '3.85', '3.95', 'Yes', 'CSE', 'Assistant Professor', '')," +
                        "('Dr. Rashid Ahmed', 'Ahmed Hasan', 'T005', '1977-07-12', 'Khulna, Bangladesh', '01711111115', 'rashid.ahmed@university.edu', 'Electrical Engineering', 'Power Systems', '3.75', '3.85', 'Yes', 'EEE', 'Professor', '')," +
                        "('Dr. Sara Islam', 'Islam Chowdhury', 'T006', '1979-02-18', 'Barisal, Bangladesh', '01711111116', 'sara.islam@university.edu', 'Mathematics', 'Mathematics', '3.80', '3.90', 'Yes', 'MATH', 'Associate Professor', '')," +
                        "('Dr. Tariq Chowdhury', 'Chowdhury Begum', 'T007', '1975-09-30', 'Comilla, Bangladesh', '01711111117', 'tariq.chowdhury@university.edu', 'Physics', 'Physics', '3.70', '3.80', 'Yes', 'PHY', 'Professor', '')," +
                        "('Dr. Nadia Hossain', 'Hossain Nesa', 'T008', '1981-04-05', 'Gazipur, Bangladesh', '01711111118', 'nadia.hossain@university.edu', 'Chemistry', 'Chemistry', '3.85', '3.95', 'Yes', 'CHE', 'Assistant Professor', '')," +
                        "('Dr. Imran Begum', 'Begum Islam', 'T009', '1978-12-22', 'Narayanganj, Bangladesh', '01711111119', 'imran.begum@university.edu', 'Biochemistry', 'Biochemistry', '3.75', '3.85', 'Yes', 'BMB', 'Associate Professor', '')," +
                        "('Dr. Meherun Nesa', 'Nesa Rahman', 'T010', '1976-06-15', 'Rangpur, Bangladesh', '01711111120', 'meherun.nesa@university.edu', 'Civil Engineering', 'Civil Engineering', '3.70', '3.80', 'Yes', 'CE', 'Professor', '')");
                System.out.println("✓ Sample teachers inserted");
            }

            // Insert sample user accounts for students - always try to insert (INSERT IGNORE prevents duplicates)
            System.out.println("Inserting sample student user accounts...");
            st.execute("INSERT IGNORE INTO users (registration_no, password, role, is_active) VALUES " +
                    "('2022-331-001', 'student123', 'STUDENT', 1)," +
                    "('2022-331-002', 'student123', 'STUDENT', 1)," +
                    "('2022-331-003', 'student123', 'STUDENT', 1)," +
                    "('2022-332-001', 'student123', 'STUDENT', 1)," +
                    "('2022-332-002', 'student123', 'STUDENT', 1)");
            System.out.println("✓ Sample student accounts inserted (password: student123)");

            // Insert sample user accounts for teachers
            rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'TEACHER'");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting sample teacher user accounts...");
                st.execute("INSERT IGNORE INTO users (username, password, role, is_active) VALUES " +
                        "('T001', 'teacher123', 'TEACHER', 1)," +
                        "('T002', 'teacher123', 'TEACHER', 1)," +
                        "('T003', 'teacher123', 'TEACHER', 1)," +
                        "('T004', 'teacher123', 'TEACHER', 1)," +
                        "('T005', 'teacher123', 'TEACHER', 1)");
                System.out.println("✓ Sample teacher accounts inserted (password: teacher123)");
            }

            // Insert sample semester tracking - always try to insert (INSERT IGNORE prevents duplicates)
            System.out.println("Inserting sample semester tracking data...");
            st.execute("INSERT IGNORE INTO student_semester (registration_no, dept, current_semester) VALUES " +
                    "('2022-331-001', 'CSE', 4), ('2022-331-002', 'CSE', 3), ('2022-331-003', 'CSE', 2)," +
                    "('2022-332-001', 'EEE', 4), ('2022-332-002', 'EEE', 2)");
            System.out.println("✓ Sample semester tracking inserted");

            // Insert sample marks for students (semester 1 and 2 for a few students) - always try to insert
            System.out.println("Inserting sample marks data...");
            rs = st.executeQuery("SELECT COUNT(*) FROM student_marks WHERE registration_no IN ('2022-331-001', '2022-331-002', '2022-331-003', '2022-332-001', '2022-332-002')");
            if (rs.next() && rs.getInt(1) == 0) {
                // Student 2022-331-001 - Semester 1 (CSE)
                st.execute("INSERT IGNORE INTO student_marks (registration_no, semester, course_code, credit, grade_point) VALUES " +
                        "('2022-331-001', 1, 'CSE101', 3.0, 3.75), ('2022-331-001', 1, 'CSE102', 2.0, 4.00), " +
                        "('2022-331-001', 1, 'CSE103', 3.0, 3.50), ('2022-331-001', 1, 'CSE104', 3.0, 3.25), " +
                        "('2022-331-001', 1, 'CSE105', 3.0, 3.75), ('2022-331-001', 1, 'CSE106', 3.0, 3.50)," +
                        // Semester 2
                        "('2022-331-001', 2, 'CSE201', 3.0, 3.75), ('2022-331-001', 2, 'CSE202', 2.0, 4.00), " +
                        "('2022-331-001', 2, 'CSE203', 3.0, 3.50), ('2022-331-001', 2, 'CSE204', 2.0, 3.75), " +
                        "('2022-331-001', 2, 'CSE205', 3.0, 3.25), ('2022-331-001', 2, 'CSE206', 3.0, 3.50)," +
                        // Student 2022-331-002 - Semester 1
                        "('2022-331-002', 1, 'CSE101', 3.0, 4.00), ('2022-331-002', 1, 'CSE102', 2.0, 4.00), " +
                        "('2022-331-002', 1, 'CSE103', 3.0, 3.75), ('2022-331-002', 1, 'CSE104', 3.0, 3.50), " +
                        "('2022-331-002', 1, 'CSE105', 3.0, 4.00), ('2022-331-002', 1, 'CSE106', 3.0, 3.75)," +
                        // Semester 2
                        "('2022-331-002', 2, 'CSE201', 3.0, 3.75), ('2022-331-002', 2, 'CSE202', 2.0, 4.00), " +
                        "('2022-331-002', 2, 'CSE203', 3.0, 3.50), ('2022-331-002', 2, 'CSE204', 2.0, 3.75), " +
                        "('2022-331-002', 2, 'CSE205', 3.0, 3.25), ('2022-331-002', 2, 'CSE206', 3.0, 3.50)," +
                        // Student 2022-332-001 - Semester 1 (EEE)
                        "('2022-332-001', 1, 'EEE101', 3.0, 3.50), ('2022-332-001', 1, 'EEE102', 2.0, 3.75), " +
                        "('2022-332-001', 1, 'EEE103', 3.0, 3.25), ('2022-332-001', 1, 'EEE104', 3.0, 3.50), " +
                        "('2022-332-001', 1, 'EEE105', 3.0, 3.75), ('2022-332-001', 1, 'EEE106', 3.0, 3.25)," +
                        // Semester 2
                        "('2022-332-001', 2, 'EEE201', 3.0, 3.50), ('2022-332-001', 2, 'EEE202', 2.0, 3.75), " +
                        "('2022-332-001', 2, 'EEE203', 3.0, 3.25), ('2022-332-001', 2, 'EEE204', 2.0, 3.50), " +
                        "('2022-332-001', 2, 'EEE205', 3.0, 3.75), ('2022-332-001', 2, 'EEE206', 3.0, 3.25)");
                System.out.println("✓ Sample marks inserted");
            }

            // Insert sample student_result data for grade distribution chart - always try to insert
            System.out.println("Inserting sample student_result data for grade distribution...");
            rs = st.executeQuery("SELECT COUNT(*) FROM student_result WHERE registration_no IN ('2022-331-001', '2022-331-002', '2022-331-003', '2022-332-001', '2022-332-002')");
            if (rs.next() && rs.getInt(1) == 0) {
                st.execute("INSERT IGNORE INTO student_result (registration_no, subject_code, marks_obtained, exam_type, exam_year, semester, grade, grade_point, status, is_approved) VALUES " +
                        // Student 2022-331-001 - Semester 1
                        "('2022-331-001', 'CSE101', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE102', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE103', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE104', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE105', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE106', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-331-001', 'CSE201', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE202', 90.0, 'Regular', 2023, 2, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE203', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE204', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE205', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE206', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        // Student 2022-331-002 - Semester 1
                        "('2022-331-002', 'CSE101', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE102', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE103', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE104', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE105', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE106', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-331-002', 'CSE201', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE202', 90.0, 'Regular', 2023, 2, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE203', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE204', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE205', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE206', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        // Student 2022-331-003 - Semester 1 (add some variety in grades)
                        "('2022-331-003', 'CSE101', 70.0, 'Regular', 2022, 1, 'C+', 2.75, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE102', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE103', 65.0, 'Regular', 2022, 1, 'C', 2.50, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE104', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE105', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE106', 55.0, 'Regular', 2022, 1, 'F', 0.00, 'FAIL', TRUE)," +
                        // Student 2022-332-001 - Semester 1 (EEE)
                        "('2022-332-001', 'EEE101', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE102', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE103', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE104', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE105', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE106', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-332-001', 'EEE201', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE202', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE203', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE204', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE205', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE206', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        // Student 2022-332-002 - Semester 1 (EEE)
                        "('2022-332-002', 'EEE101', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-332-002', 'EEE102', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-002', 'EEE103', 70.0, 'Regular', 2022, 1, 'C+', 2.75, 'PASS', TRUE)," +
                        "('2022-332-002', 'EEE104', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-002', 'EEE105', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-002', 'EEE106', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)");
                System.out.println("✓ Sample student_result data inserted");
            }

            // Insert demo summaries to showcase At-Risk dashboard variety (MEDIUM/HIGH)
            // Uses CGPA thresholds in AdminAtRiskStudents: HIGH < 2.50, MEDIUM < 3.00
            System.out.println("Inserting demo result_summary rows for At-Risk dashboard...");
            st.execute("INSERT IGNORE INTO result_summary (registration_no, semester, exam_year, total_marks, obtained_marks, percentage, gpa, result) VALUES " +
                    // MEDIUM risk examples (CGPA around 2.80-2.95)
                    "('2026-331-006', 7, 2025, 600, 435, 72.50, 2.95, 'PASS')," +
                    "('2026-331-006', 8, 2026, 600, 420, 70.00, 2.75, 'PASS')," +
                    "('2026-331-007', 7, 2025, 600, 426, 71.00, 2.85, 'PASS')," +
                    "('2026-331-007', 8, 2026, 600, 414, 69.00, 2.70, 'PASS')," +
                    // HIGH risk example (CGPA around 2.35)
                    "('2026-331-005', 7, 2025, 600, 378, 63.00, 2.40, 'PASS')," +
                    "('2026-331-005', 8, 2026, 600, 366, 61.00, 2.30, 'PASS')");
            System.out.println("✓ Demo result_summary rows inserted for At-Risk dashboard");

            // Insert sample subjects (for result management)
            rs = st.executeQuery("SELECT COUNT(*) FROM subject");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserting sample subject data...");
                st.execute("INSERT IGNORE INTO subject (subject_code, subject_name, semester, department, full_marks, pass_marks) VALUES " +
                        "('CSE101', 'Introduction to Programming', 1, 'CSE', 100, 40)," +
                        "('CSE102', 'Programming Lab', 1, 'CSE', 100, 40)," +
                        "('CSE201', 'Data Structures', 2, 'CSE', 100, 40)," +
                        "('CSE202', 'Data Structures Lab', 2, 'CSE', 100, 40)," +
                        "('EEE101', 'Basic Electrical Engineering', 1, 'EEE', 100, 40)," +
                        "('EEE201', 'Circuit Analysis', 2, 'EEE', 100, 40)," +
                        "('ME101', 'Engineering Mechanics', 1, 'ME', 100, 40)," +
                        "('ME201', 'Thermodynamics', 2, 'ME', 100, 40)");
                System.out.println("✓ Sample subjects inserted");
            }

        }
    }

    /**
     * Insert sample data even if some data already exists.
     * This can be called manually to add sample students and results.
     */
    public static void insertSampleData() throws SQLException {
        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                throw new SQLException("Cannot insert sample data: connection failed.");
            }
            
            try (Statement st = conn.c.createStatement()) {
                System.out.println("Inserting sample data...");
                
                // Insert sample students (5 students only)
                System.out.println("Inserting sample students...");
                st.execute("INSERT IGNORE INTO student (name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path) VALUES " +
                        "('Ahmed Rahman', 'Mohammad Rahman', '2022-331-001', '2004-01-15', 'Dhaka, Bangladesh', '01712345678', 'ahmed.rahman@university.edu', '4.50', '4.80', 'B.Tech', 'CSE', '')," +
                        "('Fatima Khan', 'Hassan Khan', '2022-331-002', '2004-03-20', 'Chittagong, Bangladesh', '01712345679', 'fatima.khan@university.edu', '4.60', '4.75', 'B.Tech', 'CSE', '')," +
                        "('Karim Ali', 'Rashid Ali', '2022-331-003', '2004-05-10', 'Sylhet, Bangladesh', '01712345680', 'karim.ali@university.edu', '4.40', '4.70', 'B.Tech', 'CSE', '')," +
                        "('Sara Ahmed', 'Ibrahim Ahmed', '2022-332-001', '2004-02-25', 'Rajshahi, Bangladesh', '01712345681', 'sara.ahmed@university.edu', '4.55', '4.85', 'B.Tech', 'EEE', '')," +
                        "('Rashid Hasan', 'Nurul Hasan', '2022-332-002', '2004-04-12', 'Khulna, Bangladesh', '01712345682', 'rashid.hasan@university.edu', '4.45', '4.65', 'B.Tech', 'EEE', '')");
                System.out.println("✓ Sample students inserted");
                
                // Insert semester tracking
                st.execute("INSERT IGNORE INTO student_semester (registration_no, dept, current_semester) VALUES " +
                        "('2022-331-001', 'CSE', 4), ('2022-331-002', 'CSE', 3), ('2022-331-003', 'CSE', 2)," +
                        "('2022-332-001', 'EEE', 4), ('2022-332-002', 'EEE', 2)");
                System.out.println("✓ Sample semester tracking inserted");
                
                // Insert sample marks
                st.execute("INSERT IGNORE INTO student_marks (registration_no, semester, course_code, credit, grade_point) VALUES " +
                        // Student 2022-331-001 - Semester 1 (CSE)
                        "('2022-331-001', 1, 'CSE101', 3.0, 3.75), ('2022-331-001', 1, 'CSE102', 2.0, 4.00), " +
                        "('2022-331-001', 1, 'CSE103', 3.0, 3.50), ('2022-331-001', 1, 'CSE104', 3.0, 3.25), " +
                        "('2022-331-001', 1, 'CSE105', 3.0, 3.75), ('2022-331-001', 1, 'CSE106', 3.0, 3.50)," +
                        // Semester 2
                        "('2022-331-001', 2, 'CSE201', 3.0, 3.75), ('2022-331-001', 2, 'CSE202', 2.0, 4.00), " +
                        "('2022-331-001', 2, 'CSE203', 3.0, 3.50), ('2022-331-001', 2, 'CSE204', 2.0, 3.75), " +
                        "('2022-331-001', 2, 'CSE205', 3.0, 3.25), ('2022-331-001', 2, 'CSE206', 3.0, 3.50)," +
                        // Student 2022-331-002 - Semester 1
                        "('2022-331-002', 1, 'CSE101', 3.0, 4.00), ('2022-331-002', 1, 'CSE102', 2.0, 4.00), " +
                        "('2022-331-002', 1, 'CSE103', 3.0, 3.75), ('2022-331-002', 1, 'CSE104', 3.0, 3.50), " +
                        "('2022-331-002', 1, 'CSE105', 3.0, 4.00), ('2022-331-002', 1, 'CSE106', 3.0, 3.75)," +
                        // Semester 2
                        "('2022-331-002', 2, 'CSE201', 3.0, 3.75), ('2022-331-002', 2, 'CSE202', 2.0, 4.00), " +
                        "('2022-331-002', 2, 'CSE203', 3.0, 3.50), ('2022-331-002', 2, 'CSE204', 2.0, 3.75), " +
                        "('2022-331-002', 2, 'CSE205', 3.0, 3.25), ('2022-331-002', 2, 'CSE206', 3.0, 3.50)," +
                        // Student 2022-332-001 - Semester 1 (EEE)
                        "('2022-332-001', 1, 'EEE101', 3.0, 3.50), ('2022-332-001', 1, 'EEE102', 2.0, 3.75), " +
                        "('2022-332-001', 1, 'EEE103', 3.0, 3.25), ('2022-332-001', 1, 'EEE104', 3.0, 3.50), " +
                        "('2022-332-001', 1, 'EEE105', 3.0, 3.75), ('2022-332-001', 1, 'EEE106', 3.0, 3.25)," +
                        // Semester 2
                        "('2022-332-001', 2, 'EEE201', 3.0, 3.50), ('2022-332-001', 2, 'EEE202', 2.0, 3.75), " +
                        "('2022-332-001', 2, 'EEE203', 3.0, 3.25), ('2022-332-001', 2, 'EEE204', 2.0, 3.50), " +
                        "('2022-332-001', 2, 'EEE205', 3.0, 3.75), ('2022-332-001', 2, 'EEE206', 3.0, 3.25)");
                System.out.println("✓ Sample marks inserted");
                
                // Insert sample student_result data for grade distribution chart
                // Convert grade_point to letter grades and create student_result entries
                System.out.println("Inserting sample student_result data for grade distribution...");
                
                // Helper function to convert grade_point to letter grade
                // We'll insert directly with calculated grades
                st.execute("INSERT IGNORE INTO student_result (registration_no, subject_code, marks_obtained, exam_type, exam_year, semester, grade, grade_point, status, is_approved) VALUES " +
                        // Student 2022-331-001 - Semester 1
                        "('2022-331-001', 'CSE101', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE102', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE103', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE104', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE105', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE106', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-331-001', 'CSE201', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE202', 90.0, 'Regular', 2023, 2, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE203', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE204', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE205', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-001', 'CSE206', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        // Student 2022-331-002 - Semester 1
                        "('2022-331-002', 'CSE101', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE102', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE103', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE104', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE105', 90.0, 'Regular', 2022, 1, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE106', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-331-002', 'CSE201', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE202', 90.0, 'Regular', 2023, 2, 'A+', 4.00, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE203', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE204', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE205', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-002', 'CSE206', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        // Student 2022-331-003 - Semester 1 (add some variety in grades)
                        "('2022-331-003', 'CSE101', 70.0, 'Regular', 2022, 1, 'C+', 2.75, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE102', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE103', 65.0, 'Regular', 2022, 1, 'C', 2.50, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE104', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE105', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-331-003', 'CSE106', 55.0, 'Regular', 2022, 1, 'F', 0.00, 'FAIL', TRUE)," +
                        // Student 2022-332-001 - Semester 1 (EEE)
                        "('2022-332-001', 'EEE101', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE102', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE103', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE104', 80.0, 'Regular', 2022, 1, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE105', 85.0, 'Regular', 2022, 1, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE106', 75.0, 'Regular', 2022, 1, 'B', 3.25, 'PASS', TRUE)," +
                        // Semester 2
                        "('2022-332-001', 'EEE201', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE202', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE203', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE204', 80.0, 'Regular', 2023, 2, 'B+', 3.50, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE205', 85.0, 'Regular', 2023, 2, 'A', 3.75, 'PASS', TRUE)," +
                        "('2022-332-001', 'EEE206', 75.0, 'Regular', 2023, 2, 'B', 3.25, 'PASS', TRUE)");
                System.out.println("✓ Sample student_result data inserted");
                
                System.out.println("✓ Sample data insertion complete!");
            }
        }
    }

    /**
     * Reset the initialization flag (for testing purposes only).
     * Do not call this in production!
     */
    protected static void reset() {
        initialized = false;
    }
}
