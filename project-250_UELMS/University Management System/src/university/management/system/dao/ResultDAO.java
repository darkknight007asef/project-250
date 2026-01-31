package university.management.system.dao;

import university.management.system.Conn;
import university.management.system.dao.SummaryDAO;
import university.management.system.models.StudentResult;
import university.management.system.utils.GradeCalculator;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for StudentResult operations
 */
public class ResultDAO {
    
    /**
     * Insert or update student result
     */
    public static boolean saveResult(StudentResult result, int fullMarks, int passMarks) {
        try (Conn c = new Conn()) {
            // Calculate grade and status
            GradeCalculator.GradeResult gradeResult = GradeCalculator.calculateGrade(
                result.getMarksObtained(), fullMarks);
            
            result.setGrade(gradeResult.getGrade());
            result.setGradePoint(gradeResult.getGradePoint());
            result.setStatus(gradeResult.getStatus());
            
            String sql = "INSERT INTO student_result " +
                        "(registration_no, subject_code, marks_obtained, exam_type, exam_year, semester, grade, grade_point, status, is_approved, " +
                        "marks_attendance, marks_eval, marks_term, marks_final) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "marks_obtained=VALUES(marks_obtained), grade=VALUES(grade), grade_point=VALUES(grade_point), " +
                        "status=VALUES(status), is_approved=FALSE, " +
                        "marks_attendance=VALUES(marks_attendance), marks_eval=VALUES(marks_eval), " +
                        "marks_term=VALUES(marks_term), marks_final=VALUES(marks_final)";
            
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, result.getRegistrationNo());
                ps.setString(2, result.getSubjectCode());
                ps.setDouble(3, result.getMarksObtained());
                ps.setString(4, result.getExamType());
                ps.setInt(5, result.getExamYear());
                ps.setInt(6, result.getSemester());
                ps.setString(7, result.getGrade());
                ps.setDouble(8, result.getGradePoint());
                ps.setString(9, result.getStatus());
                ps.setBoolean(10, false); // Not approved by default
                ps.setDouble(11, result.getMarksAttendance());
                ps.setDouble(12, result.getMarksEval());
                ps.setDouble(13, result.getMarksTerm());
                ps.setDouble(14, result.getMarksFinal());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean approveResultsAndPostProcess(List<Integer> resultIds) {
        if (resultIds == null || resultIds.isEmpty()) return false;

        Set<String> uniqueTriples = new HashSet<>();

        try (Conn c = new Conn()) {
            c.c.setAutoCommit(false);

            String placeholders = "?,".repeat(resultIds.size());
            placeholders = placeholders.substring(0, placeholders.length() - 1);

            String approveSql = "UPDATE student_result SET is_approved=TRUE WHERE result_id IN (" + placeholders + ")";
            try (PreparedStatement ps = c.c.prepareStatement(approveSql)) {
                for (int i = 0; i < resultIds.size(); i++) {
                    ps.setInt(i + 1, resultIds.get(i));
                }
                int updated = ps.executeUpdate();
                if (updated <= 0) {
                    c.c.rollback();
                    return false;
                }
            }

            String triplesSql = "SELECT registration_no, semester, exam_year FROM student_result WHERE result_id IN (" + placeholders + ")";
            try (PreparedStatement ps = c.c.prepareStatement(triplesSql)) {
                for (int i = 0; i < resultIds.size(); i++) {
                    ps.setInt(i + 1, resultIds.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String reg = rs.getString("registration_no");
                        int sem = rs.getInt("semester");
                        int year = rs.getInt("exam_year");
                        uniqueTriples.add(reg + "|" + sem + "|" + year);
                    }
                }
            }

            c.c.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        for (String t : uniqueTriples) {
            String[] parts = t.split("\\|");
            if (parts.length != 3) continue;
            String reg = parts[0];
            int sem;
            int year;
            try {
                sem = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            } catch (Exception ignore) {
                continue;
            }

            try {
                SummaryDAO.calculateAndSaveSummary(reg, sem, year);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                tryPromoteStudent(reg, sem, year);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    private static void tryPromoteStudent(String registrationNo, int approvedSemester, int approvedYear) throws SQLException {
        try (Conn c = new Conn()) {
            String dept;
            int currentSem;

            String semSql = "SELECT dept, current_semester FROM student_semester WHERE registration_no=?";
            try (PreparedStatement ps = c.c.prepareStatement(semSql)) {
                ps.setString(1, registrationNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return;
                    dept = rs.getString("dept");
                    currentSem = rs.getInt("current_semester");
                }
            }

            // Promote only when approvals are complete for the student's current semester
            if (approvedSemester != currentSem) return;
            if (currentSem >= 8) return;

            int expectedCourses = 0;
            String expSql = "SELECT COUNT(*) AS cnt FROM department_courses WHERE dept=? AND sem=?";
            try (PreparedStatement ps = c.c.prepareStatement(expSql)) {
                ps.setString(1, dept);
                ps.setInt(2, currentSem);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) expectedCourses = rs.getInt("cnt");
                }
            }
            if (expectedCourses <= 0) return;

            int approvedCourses = 0;
            String approvedSql = "SELECT COUNT(DISTINCT subject_code) AS cnt " +
                    "FROM student_result WHERE registration_no=? AND semester=? AND exam_year=? AND is_approved=TRUE";
            try (PreparedStatement ps = c.c.prepareStatement(approvedSql)) {
                ps.setString(1, registrationNo);
                ps.setInt(2, currentSem);
                ps.setInt(3, approvedYear);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) approvedCourses = rs.getInt("cnt");
                }
            }
            if (approvedCourses < expectedCourses) return;

            String promoteSql = "UPDATE student_semester SET current_semester=? WHERE registration_no=? AND current_semester=?";
            try (PreparedStatement ps = c.c.prepareStatement(promoteSql)) {
                ps.setInt(1, currentSem + 1);
                ps.setString(2, registrationNo);
                ps.setInt(3, currentSem);
                ps.executeUpdate();
            }
        }
    }

    public static boolean saveResultsBatch(List<StudentResult> results, int fullMarks, int passMarks) {
        if (results == null || results.isEmpty()) return false;

        try (Conn c = new Conn()) {
            c.c.setAutoCommit(false);

            String sql = "INSERT INTO student_result " +
                    "(registration_no, subject_code, marks_obtained, exam_type, exam_year, semester, grade, grade_point, status, is_approved, " +
                    "marks_attendance, marks_eval, marks_term, marks_final) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "marks_obtained=VALUES(marks_obtained), grade=VALUES(grade), grade_point=VALUES(grade_point), " +
                    "status=VALUES(status), is_approved=FALSE, " +
                    "marks_attendance=VALUES(marks_attendance), marks_eval=VALUES(marks_eval), " +
                    "marks_term=VALUES(marks_term), marks_final=VALUES(marks_final)";

            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                for (StudentResult result : results) {
                    GradeCalculator.GradeResult gradeResult = GradeCalculator.calculateGrade(
                            result.getMarksObtained(), fullMarks);
                    result.setGrade(gradeResult.getGrade());
                    result.setGradePoint(gradeResult.getGradePoint());
                    result.setStatus(gradeResult.getStatus());

                    ps.setString(1, result.getRegistrationNo());
                    ps.setString(2, result.getSubjectCode());
                    ps.setDouble(3, result.getMarksObtained());
                    ps.setString(4, result.getExamType());
                    ps.setInt(5, result.getExamYear());
                    ps.setInt(6, result.getSemester());
                    ps.setString(7, result.getGrade());
                    ps.setDouble(8, result.getGradePoint());
                    ps.setString(9, result.getStatus());
                    ps.setBoolean(10, false);
                    ps.setDouble(11, result.getMarksAttendance());
                    ps.setDouble(12, result.getMarksEval());
                    ps.setDouble(13, result.getMarksTerm());
                    ps.setDouble(14, result.getMarksFinal());
                    ps.addBatch();
                }

                ps.executeBatch();
                c.c.commit();
                return true;
            } catch (SQLException e) {
                try { c.c.rollback(); } catch (SQLException ignore) {}
                throw e;
            } finally {
                try { c.c.setAutoCommit(true); } catch (SQLException ignore) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all results for a student
     */
    public static List<StudentResult> getStudentResults(String registrationNo, Integer semester, Integer examYear) {
        List<StudentResult> results = new ArrayList<>();
        try (Conn c = new Conn()) {
            StringBuilder sql = new StringBuilder(
                "SELECT * FROM student_result WHERE registration_no=?");
            List<Object> params = new ArrayList<>();
            params.add(registrationNo);
            
            if (semester != null) {
                sql.append(" AND semester=?");
                params.add(semester);
            }
            if (examYear != null) {
                sql.append(" AND exam_year=?");
                params.add(examYear);
            }
            sql.append(" ORDER BY exam_year DESC, semester DESC, subject_code");
            
            try (PreparedStatement ps = c.c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        StudentResult result = mapResultSetToResult(rs);
                        results.add(result);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    /**
     * Get unapproved results (for admin approval)
     */
    public static List<StudentResult> getUnapprovedResults() {
        List<StudentResult> results = new ArrayList<>();
        try (Conn c = new Conn()) {
            String sql = "SELECT sr.*, s.name as student_name, sub.subject_name " +
                        "FROM student_result sr " +
                        "JOIN student s ON sr.registration_no = s.registration_no " +
                        "LEFT JOIN subject sub ON sr.subject_code = sub.subject_code " +
                        "WHERE sr.is_approved = FALSE " +
                        "ORDER BY sr.exam_year DESC, sr.semester DESC";
            try (ResultSet rs = c.s.executeQuery(sql)) {
                while (rs.next()) {
                    StudentResult result = mapResultSetToResult(rs);
                    results.add(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    /**
     * Approve results
     */
    public static boolean approveResults(List<Integer> resultIds) {
        if (resultIds == null || resultIds.isEmpty()) return false;
        
        try (Conn c = new Conn()) {
            String placeholders = "?,".repeat(resultIds.size());
            placeholders = placeholders.substring(0, placeholders.length() - 1);
            String sql = "UPDATE student_result SET is_approved=TRUE WHERE result_id IN (" + placeholders + ")";
            
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                for (int i = 0; i < resultIds.size(); i++) {
                    ps.setInt(i + 1, resultIds.get(i));
                }
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map ResultSet to StudentResult object
     */
    private static StudentResult mapResultSetToResult(ResultSet rs) throws SQLException {
        StudentResult result = new StudentResult();
        result.setResultId(rs.getInt("result_id"));
        result.setRegistrationNo(rs.getString("registration_no"));
        result.setSubjectCode(rs.getString("subject_code"));
        result.setMarksObtained(rs.getDouble("marks_obtained"));
        result.setExamType(rs.getString("exam_type"));
        result.setExamYear(rs.getInt("exam_year"));
        result.setSemester(rs.getInt("semester"));
        result.setGrade(rs.getString("grade"));
        result.setGradePoint(rs.getDouble("grade_point"));
        result.setStatus(rs.getString("status"));
        result.setApproved(rs.getBoolean("is_approved"));
        
        // Detailed marks with fail-safe in case old data doesn't have it (though defaults are 0)
        try {
            result.setMarksAttendance(rs.getDouble("marks_attendance"));
            result.setMarksEval(rs.getDouble("marks_eval"));
            result.setMarksTerm(rs.getDouble("marks_term"));
            result.setMarksFinal(rs.getDouble("marks_final"));
        } catch (SQLException ignore) {
            // Column might not exist if run on old schema without update
        }
        
        return result;
    }
}

