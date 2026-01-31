package university.management.system.models;

import java.sql.Timestamp;

public class RecheckRequest {
    private int requestId;
    private String registrationNo;
    private String subjectCode;
    private int semester;
    private int examYear;
    private String requestType;
    private String reason;
    private String status;
    private String adminComment;
    private String teacherComment;

    private Double proposedMarks;
    private String proposedGrade;
    private Double proposedGradePoint;
    private String proposedStatus;
    private boolean teacherNoChange;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getExamYear() {
        return examYear;
    }

    public void setExamYear(int examYear) {
        this.examYear = examYear;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public Double getProposedMarks() {
        return proposedMarks;
    }

    public void setProposedMarks(Double proposedMarks) {
        this.proposedMarks = proposedMarks;
    }

    public String getProposedGrade() {
        return proposedGrade;
    }

    public void setProposedGrade(String proposedGrade) {
        this.proposedGrade = proposedGrade;
    }

    public Double getProposedGradePoint() {
        return proposedGradePoint;
    }

    public void setProposedGradePoint(Double proposedGradePoint) {
        this.proposedGradePoint = proposedGradePoint;
    }

    public String getProposedStatus() {
        return proposedStatus;
    }

    public void setProposedStatus(String proposedStatus) {
        this.proposedStatus = proposedStatus;
    }

    public boolean isTeacherNoChange() {
        return teacherNoChange;
    }

    public void setTeacherNoChange(boolean teacherNoChange) {
        this.teacherNoChange = teacherNoChange;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
