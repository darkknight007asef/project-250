package university.management.system;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SchemaUpdater {
    public static void main(String[] args) {
        try {
            updateSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateSchema() throws SQLException {
        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                System.out.println("Connection failed.");
                return;
            }
            try (Statement st = conn.c.createStatement()) {
                // 1. Add detailed marks columns to student_result if they don't exist
                try {
                    st.execute("ALTER TABLE student_result ADD COLUMN marks_attendance DECIMAL(5,2) DEFAULT 0");
                    System.out.println("Added marks_attendance column.");
                } catch (SQLException e) { System.out.println("Column marks_attendance likely exists."); }

                try {
                    st.execute("ALTER TABLE student_result ADD COLUMN marks_eval DECIMAL(5,2) DEFAULT 0");
                    System.out.println("Added marks_eval column.");
                } catch (SQLException e) { System.out.println("Column marks_eval likely exists."); }

                try {
                    st.execute("ALTER TABLE student_result ADD COLUMN marks_term DECIMAL(5,2) DEFAULT 0");
                    System.out.println("Added marks_term column.");
                } catch (SQLException e) { System.out.println("Column marks_term likely exists."); }

                try {
                    st.execute("ALTER TABLE student_result ADD COLUMN marks_final DECIMAL(5,2) DEFAULT 0");
                    System.out.println("Added marks_final column.");
                } catch (SQLException e) { System.out.println("Column marks_final likely exists."); }

                // 2. Create recheck_request table
                st.execute("CREATE TABLE IF NOT EXISTS recheck_request (" +
                        "request_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "registration_no VARCHAR(30) NOT NULL," +
                        "subject_code VARCHAR(20) NOT NULL," +
                        "semester INT NOT NULL," +
                        "exam_year YEAR NOT NULL," +
                        "reason VARCHAR(255)," +
                        "status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'," +
                        "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "admin_comment VARCHAR(255)," +
                        "UNIQUE KEY unique_recheck (registration_no, subject_code, semester, exam_year)," +
                        "FOREIGN KEY (registration_no) REFERENCES student(registration_no) ON DELETE CASCADE" +
                        ")");
                System.out.println("Created/Verified recheck_request table.");
            }
        }
    }
}
