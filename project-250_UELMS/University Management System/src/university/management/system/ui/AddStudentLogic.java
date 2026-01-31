package university.management.system.ui;

import university.management.system.Conn;

import javax.swing.*; // for existing JOptionPane usage optional
import java.sql.ResultSet;
import java.util.HashMap;

public final class AddStudentLogic {
    private AddStudentLogic() {}

    public static void insert(String name, String fname, String dob, String address, String phone, String email, String x, String xii, String course, String branch, String photoPath) {
        try {
            if (name == null || name.isBlank() || branch == null || branch.isBlank() || xii == null || xii.isBlank()) {
                Dialogs.error("Name, Branch and Class XII (Year) are required.");
                return;
            }
            Conn con = new Conn();

            HashMap<String, String> deptCode = new HashMap<>();
            deptCode.put("CSE", "331"); deptCode.put("EEE", "332"); deptCode.put("SWE", "333");
            deptCode.put("MATH", "334"); deptCode.put("PHY", "335"); deptCode.put("CHE", "336");
            deptCode.put("ME", "337"); deptCode.put("CE", "338"); deptCode.put("PAD", "321");
            deptCode.put("ENG", "432"); deptCode.put("BAN", "121"); deptCode.put("SOC", "545");
            deptCode.put("GE", "231"); deptCode.put("BMB", "878");

            String dept = deptCode.get(branch);

            String countQuery = "SELECT COUNT(*) FROM student WHERE class_xii='" + xii + "' AND branch='" + branch + "'";
            ResultSet rs = con.s.executeQuery(countQuery);
            int count = 0; if (rs.next()) count = rs.getInt(1);

            String rollFormatted = String.format("%03d", count + 1);
            String registrationNo = xii + "-" + dept + "-" + rollFormatted;

            String studentQuery = "INSERT INTO student (name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path) " +
                    "VALUES('" + name + "', '" + fname + "', '" + registrationNo + "', '" + dob + "', '" + address + "', '" + phone + "', '" + email + "', '" + x + "', '" + xii + "', '" + course + "', '" + branch + "', '" + (photoPath == null ? "" : photoPath) + "')";
            con.s.executeUpdate(studentQuery);

            String semesterQuery = "INSERT INTO student_semester(registration_no, dept) VALUES('" + registrationNo + "', '" + branch + "')";
            con.s.executeUpdate(semesterQuery);

            Dialogs.info("Student Added\nRegistration No: " + registrationNo);
        } catch (Exception e) {
            Dialogs.error("Failed: " + e.getMessage());
        }
    }
}


