package university.management.system.models;

/**
 * Model class for StudentResult entity
 */
public class StudentResult {
    private int resultId;
    private String registrationNo;
    private String subjectCode;
    private double marksObtained;
    private String examType; // Regular, Re-Take, Improvement
    private int examYear;
    private int semester;
    private String grade;
    private double gradePoint;
    private String status; // PASS, FAIL
    private boolean isApproved;
    
    // Detailed Marks Breakdown
    private double marksAttendance;
    private double marksEval;
    private double marksTerm;
    private double marksFinal;

    public StudentResult() {}

    public StudentResult(String registrationNo, String subjectCode, double marksObtained, 
                        String examType, int examYear, int semester) {
        this.registrationNo = registrationNo;
        this.subjectCode = subjectCode;
        this.marksObtained = marksObtained;
        this.examType = examType;
        this.examYear = examYear;
        this.semester = semester;
    }

    // Getters and Setters
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public int getExamYear() { return examYear; }
    public void setExamYear(int examYear) { this.examYear = examYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public double getGradePoint() { return gradePoint; }
    public void setGradePoint(double gradePoint) { this.gradePoint = gradePoint; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }
    
    public double getMarksAttendance() { return marksAttendance; }
    public void setMarksAttendance(double marksAttendance) { this.marksAttendance = marksAttendance; }
    
    public double getMarksEval() { return marksEval; }
    public void setMarksEval(double marksEval) { this.marksEval = marksEval; }
    
    public double getMarksTerm() { return marksTerm; }
    public void setMarksTerm(double marksTerm) { this.marksTerm = marksTerm; }
    
    public double getMarksFinal() { return marksFinal; }
    public void setMarksFinal(double marksFinal) { this.marksFinal = marksFinal; }
}

