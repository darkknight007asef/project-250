package university.management.system.utils;

/**
 * Utility class for grade calculation logic
 */
public class GradeCalculator {
    
    /**
     * Calculate grade and grade point based on marks obtained and full marks
     * Grading Scale:
     * 90-100 → A+ (4.0)
     * 80-89  → A  (3.7)
     * 70-79  → B+ (3.3)
     * 60-69  → B  (3.0)
     * 50-59  → C+ (2.5)
     * 40-49  → C  (2.0)
     * 0-39   → F  (0.0)
     */
    public static GradeResult calculateGrade(double marksObtained, double fullMarks) {
        if (fullMarks <= 0) {
            return new GradeResult("F", 0.0, "FAIL");
        }
        
        double percentage = (marksObtained / fullMarks) * 100;
        
        String grade;
        double gradePoint;
        
        if (percentage >= 90) {
            grade = "A+";
            gradePoint = 4.0;
        } else if (percentage >= 80) {
            grade = "A";
            gradePoint = 3.7;
        } else if (percentage >= 70) {
            grade = "B+";
            gradePoint = 3.3;
        } else if (percentage >= 60) {
            grade = "B";
            gradePoint = 3.0;
        } else if (percentage >= 50) {
            grade = "C+";
            gradePoint = 2.5;
        } else if (percentage >= 40) {
            grade = "C";
            gradePoint = 2.0;
        } else {
            grade = "F";
            gradePoint = 0.0;
        }
        
        return new GradeResult(grade, gradePoint, gradePoint >= 2.0 ? "PASS" : "FAIL");
    }
    
    /**
     * Check if student passed based on marks and pass marks threshold
     */
    public static boolean isPass(double marksObtained, double passMarks) {
        return marksObtained >= passMarks;
    }
    
    /**
     * Calculate GPA from grade points
     */
    public static double calculateGPA(double totalGradePoints, int totalSubjects) {
        if (totalSubjects == 0) return 0.0;
        return totalGradePoints / totalSubjects;
    }
    
    /**
     * Calculate weighted GPA (considering credit hours)
     */
    public static double calculateWeightedGPA(double totalGradePoints, double totalCredits) {
        if (totalCredits == 0) return 0.0;
        return totalGradePoints / totalCredits;
    }
    
    /**
     * Result class to hold grade calculation results
     */
    public static class GradeResult {
        private final String grade;
        private final double gradePoint;
        private final String status;
        
        public GradeResult(String grade, double gradePoint, String status) {
            this.grade = grade;
            this.gradePoint = gradePoint;
            this.status = status;
        }
        
        public String getGrade() { return grade; }
        public double getGradePoint() { return gradePoint; }
        public String getStatus() { return status; }
    }
}

