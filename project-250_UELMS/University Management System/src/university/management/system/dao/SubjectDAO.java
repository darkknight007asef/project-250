package university.management.system.dao;

import university.management.system.Conn;
import university.management.system.models.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subject operations
 */
public class SubjectDAO {
    
    /**
     * Add a new subject
     */
    public static boolean addSubject(Subject subject) {
        try (Conn c = new Conn()) {
            String sql = "INSERT INTO subject (subject_code, subject_name, semester, department, full_marks, pass_marks) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, subject.getSubjectCode());
                ps.setString(2, subject.getSubjectName());
                ps.setInt(3, subject.getSemester());
                ps.setString(4, subject.getDepartment());
                ps.setInt(5, subject.getFullMarks());
                ps.setInt(6, subject.getPassMarks());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing subject
     */
    public static boolean updateSubject(Subject subject) {
        try (Conn c = new Conn()) {
            String sql = "UPDATE subject SET subject_name=?, semester=?, department=?, full_marks=?, pass_marks=? " +
                        "WHERE subject_code=?";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, subject.getSubjectName());
                ps.setInt(2, subject.getSemester());
                ps.setString(3, subject.getDepartment());
                ps.setInt(4, subject.getFullMarks());
                ps.setInt(5, subject.getPassMarks());
                ps.setString(6, subject.getSubjectCode());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a subject
     */
    public static boolean deleteSubject(String subjectCode) {
        try (Conn c = new Conn()) {
            String sql = "DELETE FROM subject WHERE subject_code=?";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, subjectCode);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all subjects
     */
    public static List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM subject ORDER BY department, semester, subject_code";
            try (ResultSet rs = c.s.executeQuery(sql)) {
                while (rs.next()) {
                    Subject subject = new Subject();
                    subject.setSubjectId(rs.getInt("subject_id"));
                    subject.setSubjectCode(rs.getString("subject_code"));
                    subject.setSubjectName(rs.getString("subject_name"));
                    subject.setSemester(rs.getInt("semester"));
                    subject.setDepartment(rs.getString("department"));
                    subject.setFullMarks(rs.getInt("full_marks"));
                    subject.setPassMarks(rs.getInt("pass_marks"));
                    subjects.add(subject);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }
    
    /**
     * Get subjects by department and semester
     */
    public static List<Subject> getSubjectsByDeptAndSem(String department, int semester) {
        List<Subject> subjects = new ArrayList<>();
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM subject WHERE department=? AND semester=? ORDER BY subject_code";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, department);
                ps.setInt(2, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Subject subject = new Subject();
                        subject.setSubjectId(rs.getInt("subject_id"));
                        subject.setSubjectCode(rs.getString("subject_code"));
                        subject.setSubjectName(rs.getString("subject_name"));
                        subject.setSemester(rs.getInt("semester"));
                        subject.setDepartment(rs.getString("department"));
                        subject.setFullMarks(rs.getInt("full_marks"));
                        subject.setPassMarks(rs.getInt("pass_marks"));
                        subjects.add(subject);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }
    
    /**
     * Get subject by code
     */
    public static Subject getSubjectByCode(String subjectCode) {
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM subject WHERE subject_code=?";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, subjectCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Subject subject = new Subject();
                        subject.setSubjectId(rs.getInt("subject_id"));
                        subject.setSubjectCode(rs.getString("subject_code"));
                        subject.setSubjectName(rs.getString("subject_name"));
                        subject.setSemester(rs.getInt("semester"));
                        subject.setDepartment(rs.getString("department"));
                        subject.setFullMarks(rs.getInt("full_marks"));
                        subject.setPassMarks(rs.getInt("pass_marks"));
                        return subject;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

