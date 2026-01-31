package university.management.system.models;

/**
 * Model class for Subject entity
 */
public class Subject {
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int semester;
    private String department;
    private int fullMarks;
    private int passMarks;

    public Subject() {}

    public Subject(String subjectCode, String subjectName, int semester, String department, int fullMarks, int passMarks) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.semester = semester;
        this.department = department;
        this.fullMarks = fullMarks;
        this.passMarks = passMarks;
    }

    // Getters and Setters
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getFullMarks() { return fullMarks; }
    public void setFullMarks(int fullMarks) { this.fullMarks = fullMarks; }

    public int getPassMarks() { return passMarks; }
    public void setPassMarks(int passMarks) { this.passMarks = passMarks; }
}

