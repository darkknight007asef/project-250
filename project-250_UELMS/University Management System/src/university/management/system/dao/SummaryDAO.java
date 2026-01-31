package university.management.system.dao;

import university.management.system.Conn;
import university.management.system.models.ResultSummary;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ResultSummary operations
 */
public class SummaryDAO {
    
    /**
     * Calculate and save result summary for a student's semester
     */
    public static boolean calculateAndSaveSummary(String registrationNo, int semester, int examYear) {
        try (Conn c = new Conn()) {
            // Calculate summary from student_result table
            String sql = "SELECT " +
                        "COUNT(*) as total_subjects, " +
                        "SUM(dc.credit * 100) as total_marks, " + // Approximation if full_marks not available, but usually distinct
                        "SUM(sr.marks_obtained) as obtained_marks, " +
                        "SUM(sr.grade_point * dc.credit) / SUM(dc.credit) as gpa, " +
                        "MIN(sr.status) as result_status " +
                        "FROM student_result sr " +
                        "JOIN department_courses dc ON sr.subject_code = dc.course_code " +
                        "WHERE sr.registration_no = ? AND sr.semester = ? AND sr.exam_year = ? " +
                        "AND sr.is_approved = TRUE";
            
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, registrationNo);
                ps.setInt(2, semester);
                ps.setInt(3, examYear);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int totalSubjects = rs.getInt("total_subjects");
                        if (totalSubjects == 0) return false;
                        
                        double totalMarks = rs.getDouble("total_marks");
                        double obtainedMarks = rs.getDouble("obtained_marks");
                        double gpa = rs.getDouble("gpa");
                        String resultStatus = rs.getString("result_status");
                        
                        double percentage = (totalMarks > 0) ? (obtainedMarks / totalMarks) * 100 : 0.0;
                        String finalResult = (resultStatus != null && resultStatus.equals("FAIL")) ? "FAIL" : "PASS";
                        
                        // Insert or update summary
                        String insertSql = "INSERT INTO result_summary " +
                                         "(registration_no, semester, exam_year, total_marks, obtained_marks, percentage, gpa, result) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                                         "ON DUPLICATE KEY UPDATE " +
                                         "total_marks=VALUES(total_marks), obtained_marks=VALUES(obtained_marks), " +
                                         "percentage=VALUES(percentage), gpa=VALUES(gpa), result=VALUES(result)";
                        
                        try (PreparedStatement insertPs = c.c.prepareStatement(insertSql)) {
                            insertPs.setString(1, registrationNo);
                            insertPs.setInt(2, semester);
                            insertPs.setInt(3, examYear);
                            insertPs.setDouble(4, totalMarks);
                            insertPs.setDouble(5, obtainedMarks);
                            insertPs.setDouble(6, percentage);
                            insertPs.setDouble(7, gpa);
                            insertPs.setString(8, finalResult);
                            return insertPs.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get summary for a student's semester
     */
    public static ResultSummary getSummary(String registrationNo, int semester, int examYear) {
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM result_summary WHERE registration_no=? AND semester=? AND exam_year=?";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, registrationNo);
                ps.setInt(2, semester);
                ps.setInt(3, examYear);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ResultSummary summary = new ResultSummary();
                        summary.setSummaryId(rs.getInt("summary_id"));
                        summary.setRegistrationNo(rs.getString("registration_no"));
                        summary.setSemester(rs.getInt("semester"));
                        summary.setExamYear(rs.getInt("exam_year"));
                        summary.setTotalMarks(rs.getDouble("total_marks"));
                        summary.setObtainedMarks(rs.getDouble("obtained_marks"));
                        summary.setPercentage(rs.getDouble("percentage"));
                        summary.setGpa(rs.getDouble("gpa"));
                        summary.setResult(rs.getString("result"));
                        return summary;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all summaries for a student
     */
    public static List<ResultSummary> getStudentSummaries(String registrationNo) {
        List<ResultSummary> summaries = new ArrayList<>();
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM result_summary WHERE registration_no=? ORDER BY exam_year DESC, semester DESC";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, registrationNo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ResultSummary summary = new ResultSummary();
                        summary.setSummaryId(rs.getInt("summary_id"));
                        summary.setRegistrationNo(rs.getString("registration_no"));
                        summary.setSemester(rs.getInt("semester"));
                        summary.setExamYear(rs.getInt("exam_year"));
                        summary.setTotalMarks(rs.getDouble("total_marks"));
                        summary.setObtainedMarks(rs.getDouble("obtained_marks"));
                        summary.setPercentage(rs.getDouble("percentage"));
                        summary.setGpa(rs.getDouble("gpa"));
                        summary.setResult(rs.getString("result"));
                        summaries.add(summary);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
    
    /**
     * Calculate CGPA (Cumulative GPA) for a student
     */
    public static double calculateCGPA(String registrationNo) {
        try (Conn c = new Conn()) {
            String sql = "SELECT AVG(gpa) as cgpa FROM result_summary WHERE registration_no=?";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, registrationNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("cgpa");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}

