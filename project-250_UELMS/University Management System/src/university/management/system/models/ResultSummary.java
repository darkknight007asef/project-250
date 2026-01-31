package university.management.system.models;

/**
 * Model class for ResultSummary entity
 */
public class ResultSummary {
    private int summaryId;
    private String registrationNo;
    private int semester;
    private int examYear;
    private double totalMarks;
    private double obtainedMarks;
    private double percentage;
    private double gpa;
    private String result; // PASS, FAIL

    public ResultSummary() {}

    public ResultSummary(String registrationNo, int semester, int examYear) {
        this.registrationNo = registrationNo;
        this.semester = semester;
        this.examYear = examYear;
    }

    // Getters and Setters
    public int getSummaryId() { return summaryId; }
    public void setSummaryId(int summaryId) { this.summaryId = summaryId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getExamYear() { return examYear; }
    public void setExamYear(int examYear) { this.examYear = examYear; }

    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    public double getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(double obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

