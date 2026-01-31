package university.management.system.dao;

import university.management.system.Conn;
import university.management.system.models.RecheckRequest;
import university.management.system.utils.GradeCalculator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class RecheckDAO {
    private RecheckDAO() {}

    private static volatile String lastError;

    public static String getLastError() {
        return lastError;
    }

    public static final class Status {
        public static final String SUBMITTED = "SUBMITTED";
        public static final String REJECTED = "REJECTED";
        public static final String FORWARDED_TO_TEACHER = "FORWARDED_TO_TEACHER";
        public static final String TEACHER_REVIEWED = "TEACHER_REVIEWED";
        public static final String ADMIN_FINAL_APPROVED = "ADMIN_FINAL_APPROVED";
    }

    public static boolean createRequest(String registrationNo, String subjectCode, int semester, int examYear,
                                        String requestType, String reason) {
        lastError = null;
        String sql = "INSERT INTO recheck_request (registration_no, subject_code, semester, exam_year, request_type, reason, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE request_type=VALUES(request_type), reason=VALUES(reason), status=VALUES(status)";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, registrationNo);
            ps.setString(2, subjectCode);
            ps.setInt(3, semester);
            ps.setInt(4, examYear);
            ps.setString(5, requestType);
            ps.setString(6, reason);
            ps.setString(7, Status.SUBMITTED);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public static List<RecheckRequest> getRequestsForStudent(String registrationNo) {
        List<RecheckRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM recheck_request WHERE registration_no=? ORDER BY updated_at DESC";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, registrationNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<RecheckRequest> getRequestsByStatuses(String... statuses) {
        List<RecheckRequest> list = new ArrayList<>();
        if (statuses == null || statuses.length == 0) return list;

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            if (i > 0) in.append(",");
            in.append("?");
        }

        String sql = "SELECT * FROM recheck_request WHERE status IN (" + in + ") ORDER BY updated_at DESC";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            for (int i = 0; i < statuses.length; i++) {
                ps.setString(i + 1, statuses[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean adminForwardToTeacher(int requestId, String adminComment) {
        String sql = "UPDATE recheck_request SET status=?, admin_comment=? WHERE request_id=?";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, Status.FORWARDED_TO_TEACHER);
            ps.setString(2, adminComment);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean adminReject(int requestId, String adminComment) {
        String sql = "UPDATE recheck_request SET status=?, admin_comment=? WHERE request_id=?";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, Status.REJECTED);
            ps.setString(2, adminComment);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean teacherNoChange(int requestId, String teacherComment) {
        String sql = "UPDATE recheck_request SET status=?, teacher_comment=?, teacher_no_change=TRUE, " +
                "proposed_marks=NULL, proposed_grade=NULL, proposed_grade_point=NULL, proposed_status=NULL " +
                "WHERE request_id=?";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, Status.TEACHER_REVIEWED);
            ps.setString(2, teacherComment);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean teacherProposeMarks(int requestId, double proposedMarks, double fullMarks, String teacherComment) {
        GradeCalculator.GradeResult gr = GradeCalculator.calculateGrade(proposedMarks, fullMarks);

        String sql = "UPDATE recheck_request SET status=?, teacher_comment=?, teacher_no_change=FALSE, " +
                "proposed_marks=?, proposed_grade=?, proposed_grade_point=?, proposed_status=? " +
                "WHERE request_id=?";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setString(1, Status.TEACHER_REVIEWED);
            ps.setString(2, teacherComment);
            ps.setDouble(3, proposedMarks);
            ps.setString(4, gr.getGrade());
            ps.setDouble(5, gr.getGradePoint());
            ps.setString(6, gr.getStatus());
            ps.setInt(7, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean adminFinalApproveApplyToResult(RecheckRequest req, String adminComment) throws SQLException {
        if (req == null) return false;

        try (Conn c = new Conn()) {
            c.c.setAutoCommit(false);
            try {
                Integer resultId = null;
                Double oldMarks = null;
                String oldGrade = null;
                Double oldGradePoint = null;
                String oldStatus = null;

                try (PreparedStatement ps = c.c.prepareStatement(
                        "SELECT result_id, marks_obtained, grade, grade_point, status FROM student_result " +
                                "WHERE registration_no=? AND subject_code=? AND semester=? AND exam_year=? AND exam_type='Regular' LIMIT 1")) {
                    ps.setString(1, req.getRegistrationNo());
                    ps.setString(2, req.getSubjectCode());
                    ps.setInt(3, req.getSemester());
                    ps.setInt(4, req.getExamYear());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            resultId = rs.getInt("result_id");
                            oldMarks = rs.getDouble("marks_obtained");
                            oldGrade = rs.getString("grade");
                            oldGradePoint = rs.getDouble("grade_point");
                            oldStatus = rs.getString("status");
                        }
                    }
                }

                if (resultId == null) {
                    throw new SQLException("No matching student_result row found to apply recheck.");
                }

                Double newMarks = req.isTeacherNoChange() ? oldMarks : req.getProposedMarks();
                String newGrade = req.isTeacherNoChange() ? oldGrade : req.getProposedGrade();
                Double newGradePoint = req.isTeacherNoChange() ? oldGradePoint : req.getProposedGradePoint();
                String newStatus = req.isTeacherNoChange() ? oldStatus : req.getProposedStatus();

                if (newMarks == null || newGradePoint == null) {
                    throw new SQLException("Teacher must either mark 'no change' or propose marks.");
                }

                try (PreparedStatement ps = c.c.prepareStatement(
                        "UPDATE student_result SET marks_obtained=?, grade=?, grade_point=?, status=?, is_approved=TRUE WHERE result_id=?")) {
                    ps.setDouble(1, newMarks);
                    ps.setString(2, newGrade);
                    ps.setDouble(3, newGradePoint);
                    ps.setString(4, newStatus);
                    ps.setInt(5, resultId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.c.prepareStatement(
                        "INSERT INTO result_change_audit (result_id, request_id, changed_by_role, old_marks, new_marks, old_grade, new_grade, old_grade_point, new_grade_point, old_status, new_status, comment) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, resultId);
                    ps.setInt(2, req.getRequestId());
                    ps.setString(3, "ADMIN");
                    ps.setObject(4, oldMarks);
                    ps.setObject(5, newMarks);
                    ps.setString(6, oldGrade);
                    ps.setString(7, newGrade);
                    ps.setObject(8, oldGradePoint);
                    ps.setObject(9, newGradePoint);
                    ps.setString(10, oldStatus);
                    ps.setString(11, newStatus);
                    ps.setString(12, adminComment);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.c.prepareStatement(
                        "UPDATE recheck_request SET status=?, admin_comment=? WHERE request_id=?")) {
                    ps.setString(1, Status.ADMIN_FINAL_APPROVED);
                    ps.setString(2, adminComment);
                    ps.setInt(3, req.getRequestId());
                    ps.executeUpdate();
                }

                c.c.commit();
                return true;
            } catch (SQLException e) {
                c.c.rollback();
                throw e;
            } finally {
                try {
                    c.c.setAutoCommit(true);
                } catch (SQLException ignore) {
                }
            }
        }
    }

    public static double getFullMarksForSubject(String subjectCode) {
        if (subjectCode == null || subjectCode.trim().isEmpty()) return 100.0;
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(
                "SELECT full_marks FROM subject WHERE subject_code=? LIMIT 1")) {
            ps.setString(1, subjectCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("full_marks");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 100.0;
    }

    public static RecheckRequest getById(int requestId) {
        String sql = "SELECT * FROM recheck_request WHERE request_id=?";
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static RecheckRequest map(ResultSet rs) throws SQLException {
        RecheckRequest r = new RecheckRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setRegistrationNo(rs.getString("registration_no"));
        r.setSubjectCode(rs.getString("subject_code"));
        r.setSemester(rs.getInt("semester"));
        r.setExamYear(rs.getInt("exam_year"));
        r.setRequestType(rs.getString("request_type"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setAdminComment(rs.getString("admin_comment"));
        r.setTeacherComment(rs.getString("teacher_comment"));
        r.setTeacherNoChange(rs.getBoolean("teacher_no_change"));

        double pm = rs.getDouble("proposed_marks");
        r.setProposedMarks(rs.wasNull() ? null : pm);

        r.setProposedGrade(rs.getString("proposed_grade"));

        double pgp = rs.getDouble("proposed_grade_point");
        r.setProposedGradePoint(rs.wasNull() ? null : pgp);

        r.setProposedStatus(rs.getString("proposed_status"));

        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}
