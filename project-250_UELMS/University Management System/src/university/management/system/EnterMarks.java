/*
package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnterMarks extends JFrame implements ActionListener {
    
    JTextField searchField;
    JButton search, submit, cancel;
    JComboBox<String>[] gradeBoxes;
    JLabel[] courseLabels;
    JLabel nameLabel, registrationLabel, deptLabel, semLabel, cgpaLabel, cumulativeLabel;
    String registrationNo, department;
    int currentSemester;
    JPanel coursesPanel;
    List<String[]> coursesList;
    
    public EnterMarks() {
        setSize(900, 750);
        setLocation(300, 50);
        setTitle("Enter Marks");
        setLayout(null);
        
        getContentPane().setBackground(Color.WHITE);
        
        // Search section
        JLabel heading = new JLabel("Enter Student Marks");
        heading.setBounds(50, 10, 400, 30);
        heading.setFont(new Font("Tahoma", Font.BOLD, 20));
        add(heading);
        
        JLabel searchLabel = new JLabel("Search by Registration No:");
        searchLabel.setBounds(50, 60, 200, 20);
        add(searchLabel);
        
        searchField = new JTextField();
        searchField.setBounds(250, 60, 150, 25);
        add(searchField);
        
        search = new JButton("Search");
        search.setBounds(420, 60, 100, 25);
        search.setBackground(Color.BLACK);
        search.setForeground(Color.WHITE);
        search.addActionListener(this);
        add(search);
        
        // Student info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(50, 100, 800, 80);
        infoPanel.setLayout(new GridLayout(2, 4, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        JLabel nameText = new JLabel("Name:");
        nameLabel = new JLabel("");
        JLabel regText = new JLabel("Registration No:");
        registrationLabel = new JLabel("");
        JLabel deptText = new JLabel("Department:");
        deptLabel = new JLabel("");
        JLabel semText = new JLabel("Current Semester:");
        semLabel = new JLabel("");
        
        infoPanel.add(nameText);
        infoPanel.add(nameLabel);
        infoPanel.add(regText);
        infoPanel.add(registrationLabel);
        infoPanel.add(deptText);
        infoPanel.add(deptLabel);
        infoPanel.add(semText);
        infoPanel.add(semLabel);
        
        add(infoPanel);
        
        // Courses panel
        coursesPanel = new JPanel();
        coursesPanel.setBounds(50, 200, 800, 350);
        coursesPanel.setLayout(new GridLayout(0, 3, 10, 10));
        coursesPanel.setBorder(BorderFactory.createTitledBorder("Course Grades"));
        coursesPanel.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setBounds(50, 200, 800, 350);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
        
        // Results panel
        JPanel resultPanel = new JPanel();
        resultPanel.setBounds(50, 560, 800, 60);
        resultPanel.setLayout(new GridLayout(1, 4, 10, 10));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        JLabel cgpaText = new JLabel("Semester CGPA:");
        cgpaLabel = new JLabel("");
        JLabel cumulativeText = new JLabel("Cumulative CGPA:");
        cumulativeLabel = new JLabel("");
        
        resultPanel.add(cgpaText);
        resultPanel.add(cgpaLabel);
        resultPanel.add(cumulativeText);
        resultPanel.add(cumulativeLabel);
        
        add(resultPanel);
        
        // Buttons
        submit = new JButton("Submit");
        submit.setBounds(250, 630, 100, 30);
        submit.setBackground(Color.BLUE);
        submit.setForeground(Color.WHITE);
        submit.addActionListener(this);
        submit.setEnabled(false);
        add(submit);
        
        cancel = new JButton("Cancel");
        cancel.setBounds(400, 630, 100, 30);
        cancel.setBackground(Color.RED);
        cancel.setForeground(Color.WHITE);
        cancel.addActionListener(this);
        add(cancel);
        
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == search) {
            searchStudent();
        } else if (ae.getSource() == submit) {
            submitMarks();
        } else if (ae.getSource() == cancel) {
            setVisible(false);
        }
    }
    
    private void searchStudent() {
        registrationNo = searchField.getText().trim();
        
        if (registrationNo.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter registration number");
            return;
        }
        
        try {
            Conn c = new Conn();
            String query = "SELECT s.name, s.registration_no, s.branch, ss.current_semester " +
                          "FROM student s JOIN student_semester ss ON s.registration_no = ss.registration_no " +
                          "WHERE s.registration_no = '" + registrationNo + "'";
            
            ResultSet rs = c.s.executeQuery(query);
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                registrationLabel.setText(rs.getString("registration_no"));
                deptLabel.setText(rs.getString("branch"));
                semLabel.setText(rs.getString("current_semester"));
                
                department = rs.getString("branch");
                currentSemester = rs.getInt("current_semester");
                
                loadCourses();
                loadPreviousResults();
                
                coursesPanel.setVisible(true);
                submit.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(null, "Student not found!");
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error searching student: " + e.getMessage());
        }
    }
    
    private void loadCourses() {
        coursesPanel.removeAll();
        coursesList = new ArrayList<>();
        
        try {
            Conn c = new Conn();
            String query = "SELECT course_code, course_name, credit FROM department_courses " +
                          "WHERE dept = '" + department + "' AND sem = " + currentSemester + " ORDER BY course_code";
            
            ResultSet rs = c.s.executeQuery(query);
            
            while (rs.next()) {
                String[] course = new String[3];
                course[0] = rs.getString("course_code");
                course[1] = rs.getString("course_name");
                course[2] = rs.getString("credit");
                coursesList.add(course);
            }
            
            // Create the UI components
            courseLabels = new JLabel[coursesList.size()];
            gradeBoxes = new JComboBox[coursesList.size()];
            
            for (int i = 0; i < coursesList.size(); i++) {
                String[] course = coursesList.get(i);
                
                // Course name label
                String displayText = course[1];
                if (displayText.length() > 30) {
                    displayText = displayText.substring(0, 27) + "...";
                }
                courseLabels[i] = new JLabel(displayText + " (" + course[2] + " cr)");
                courseLabels[i].setToolTipText(course[1] + " (" + course[2] + " credits)");
                coursesPanel.add(courseLabels[i]);
                
                // Grade combo box
                String[] grades = {"Select", "4.00", "3.75", "3.50", "3.25", "3.00", "2.75", "2.50", "2.25", "2.00", "0.00"};
                gradeBoxes[i] = new JComboBox<>(grades);
                gradeBoxes[i].setBackground(Color.WHITE);
                gradeBoxes[i].setForeground(Color.BLACK);
                gradeBoxes[i].setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
                coursesPanel.add(gradeBoxes[i]);
                
                // Check if grade already exists in database
                checkExistingGrade(course[0], gradeBoxes[i]);
                
                // Add empty label for spacing
                coursesPanel.add(new JLabel(""));
            }
            
            coursesPanel.revalidate();
            coursesPanel.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading courses: " + e.getMessage());
        }
    }
    
    private void checkExistingGrade(String courseCode, JComboBox<String> gradeBox) {
        try (Conn c = new Conn()) {
            String query = "SELECT grade_point FROM student_marks " +
                          "WHERE registration_no = ? AND semester = ? AND course_code = ?";
            java.sql.PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, registrationNo);
            ps.setInt(2, currentSemester);
            ps.setString(3, courseCode);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                double grade = rs.getDouble("grade_point");
                gradeBox.setSelectedItem(String.format("%.2f", grade));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Ignore errors - just means no grade exists yet
        }
    }
    
    private void loadPreviousResults() {
        try {
            Conn c = new Conn();
            
            // Calculate cumulative CGPA
            String query = "SELECT SUM(credit * grade_point) as total_grade_points, SUM(credit) as total_credits " +
                          "FROM student_marks WHERE registration_no = '" + registrationNo + "' " +
                          "AND grade_point > 0";
            
            ResultSet rs = c.s.executeQuery(query);
            
            double cumulativeCGPA = 0.0;
            if (rs.next()) {
                double totalGradePoints = rs.getDouble("total_grade_points");
                double totalCredits = rs.getDouble("total_credits");
                
                if (totalCredits > 0) {
                    cumulativeCGPA = totalGradePoints / totalCredits;
                }
            }
            
            cumulativeLabel.setText(String.format("%.2f", cumulativeCGPA));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading previous results: " + e.getMessage());
        }
    }
    
    private void submitMarks() {
        try {
            Conn c = new Conn();
            
            double totalGradePoints = 0;
            double totalCredits = 0;
            int failedCourses = 0;
            boolean allGradesSelected = true;
            
            // First validate that all grades are selected
            for (int i = 0; i < gradeBoxes.length; i++) {
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                if ("Select".equals(selectedGrade)) {
                    allGradesSelected = false;
                    JOptionPane.showMessageDialog(null, "Please select grade for all courses");
                    return;
                }
            }
            
            if (!allGradesSelected) {
                return;
            }
            
            // Process each course
            for (int i = 0; i < coursesList.size(); i++) {
                String courseCode = coursesList.get(i)[0];
                double credit = Double.parseDouble(coursesList.get(i)[2]);
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                double gradePoint = Double.parseDouble(selectedGrade);
                
                // Upsert without relying on a unique key: UPDATE first, then INSERT if no row updated
                int updated;
                try (PreparedStatement up = c.c.prepareStatement(
                        "UPDATE student_marks SET credit = ?, grade_point = ? WHERE registration_no = ? AND semester = ? AND course_code = ?")) {
                    up.setDouble(1, credit);
                    up.setDouble(2, gradePoint);
                    up.setString(3, registrationNo);
                    up.setInt(4, currentSemester);
                    up.setString(5, courseCode);
                    updated = up.executeUpdate();
                }
                if (updated == 0) {
                    try (PreparedStatement ins = c.c.prepareStatement(
                            "INSERT INTO student_marks (registration_no, semester, course_code, credit, grade_point) VALUES (?,?,?,?,?)")) {
                        ins.setString(1, registrationNo);
                        ins.setInt(2, currentSemester);
                        ins.setString(3, courseCode);
                        ins.setDouble(4, credit);
                        ins.setDouble(5, gradePoint);
                        ins.executeUpdate();
                    }
                }
                
                if (gradePoint > 0) {
                    totalGradePoints += credit * gradePoint;
                    totalCredits += credit;
                } else {
                    failedCourses++;
                }
            }
            
            // Calculate semester CGPA
            double semesterCGPA = (totalCredits > 0) ? totalGradePoints / totalCredits : 0.0;
            cgpaLabel.setText(String.format("%.2f", semesterCGPA));
            
            // Update cumulative CGPA
            loadPreviousResults();
            
            // If student passed all courses, promote to next semester
            if (failedCourses == 0 && currentSemester < 8) {
                String updateQuery = "UPDATE student_semester SET current_semester = " + (currentSemester + 1) + 
                                   " WHERE registration_no = '" + registrationNo + "'";
                c.s.executeUpdate(updateQuery);
                
                semLabel.setText(String.valueOf(currentSemester + 1));
                currentSemester++; // Update the current semester variable
                JOptionPane.showMessageDialog(null, "Marks submitted successfully! Student promoted to semester " + currentSemester);
            } else if (failedCourses > 0) {
                JOptionPane.showMessageDialog(null, "Marks submitted successfully! Student has " + failedCourses + " failed course(s) and cannot be promoted.");
            } else {
                JOptionPane.showMessageDialog(null, "Marks submitted successfully! Student has completed all semesters.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error submitting marks: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        nameLabel.setText("");
        registrationLabel.setText("");
        deptLabel.setText("");
        semLabel.setText("");
        cgpaLabel.setText("");
        cumulativeLabel.setText("");
        coursesPanel.setVisible(false);
        submit.setEnabled(false);
    }
    
    public static void main(String[] args) {
        new EnterMarks();
    }
}

class Conn {
    Connection c;
    Statement s;
    
    public Conn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/universitymanagementsystem", "root", "");
            s = c.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


*/












/*

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EnterMarks extends JFrame implements ActionListener {
    
    JTextField searchField;
    JButton search, submit, cancel;
    JComboBox<String>[] gradeBoxes;
    JLabel[] courseLabels;
    JLabel nameLabel, registrationLabel, deptLabel, semLabel, cgpaLabel, cumulativeLabel, failedCoursesLabel, failedCreditsLabel;
    String registrationNo, department;
    int currentSemester;
    JPanel coursesPanel;
    
    // Use global Conn (Aiven) from package instead of local
    
    EnterMarks() {
        setSize(900, 800);
        setLocation(300, 50);
        setTitle("Enter Student Marks");
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);
        
        // Search section
        JLabel heading = new JLabel("Enter Student Marks");
        heading.setBounds(50, 10, 400, 30);
        heading.setFont(new Font("Tahoma", Font.BOLD, 20));
        add(heading);
        
        JLabel searchLabel = new JLabel("Search by Registration No:");
        searchLabel.setBounds(50, 60, 200, 20);
        add(searchLabel);
        
        searchField = new JTextField();
        searchField.setBounds(250, 60, 150, 25);
        add(searchField);
        
        search = new JButton("Search");
        search.setBounds(420, 60, 100, 25);
        search.setBackground(Color.BLACK);
        search.setForeground(Color.WHITE);
        search.addActionListener(this);
        add(search);
        
        // Student info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(50, 100, 800, 80);
        infoPanel.setLayout(new GridLayout(2, 4, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        JLabel nameText = new JLabel("Name:");
        nameLabel = new JLabel("");
        JLabel regText = new JLabel("Registration No:");
        registrationLabel = new JLabel("");
        JLabel deptText = new JLabel("Department:");
        deptLabel = new JLabel("");
        JLabel semText = new JLabel("Current Semester:");
        semLabel = new JLabel("");
        
        infoPanel.add(nameText);
        infoPanel.add(nameLabel);
        infoPanel.add(regText);
        infoPanel.add(registrationLabel);
        infoPanel.add(deptText);
        infoPanel.add(deptLabel);
        infoPanel.add(semText);
        infoPanel.add(semLabel);
        
        add(infoPanel);
        
        // Courses panel
        coursesPanel = new JPanel();
        coursesPanel.setBounds(50, 200, 800, 300);
        coursesPanel.setLayout(new GridLayout(0, 3, 10, 10));
        coursesPanel.setBorder(BorderFactory.createTitledBorder("Course Grades"));
        coursesPanel.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setBounds(50, 200, 800, 300);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
        
        // Results panel
        JPanel resultPanel = new JPanel();
        resultPanel.setBounds(50, 510, 800, 80);
        resultPanel.setLayout(new GridLayout(2, 4, 10, 10));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        JLabel cgpaText = new JLabel("Semester CGPA:");
        cgpaLabel = new JLabel("");
        JLabel cumulativeText = new JLabel("Cumulative CGPA:");
        cumulativeLabel = new JLabel("");
        JLabel failedCoursesText = new JLabel("Failed Courses:");
        failedCoursesLabel = new JLabel("");
        JLabel failedCreditsText = new JLabel("Failed Credits:");
        failedCreditsLabel = new JLabel("");
        
        resultPanel.add(cgpaText);
        resultPanel.add(cgpaLabel);
        resultPanel.add(cumulativeText);
        resultPanel.add(cumulativeLabel);
        resultPanel.add(failedCoursesText);
        resultPanel.add(failedCoursesLabel);
        resultPanel.add(failedCreditsText);
        resultPanel.add(failedCreditsLabel);
        
        add(resultPanel);
        
        // Buttons
        submit = new JButton("Submit");
        submit.setBounds(250, 610, 100, 30);
        submit.setBackground(Color.BLUE);
        submit.setForeground(Color.WHITE);
        submit.addActionListener(this);
        submit.setEnabled(false);
        add(submit);
        
        cancel = new JButton("Cancel");
        cancel.setBounds(400, 610, 100, 30);
        cancel.setBackground(Color.RED);
        cancel.setForeground(Color.WHITE);
        cancel.addActionListener(this);
        add(cancel);
        
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == search) {
            searchStudent();
        } else if (ae.getSource() == submit) {
            submitMarks();
        } else if (ae.getSource() == cancel) {
            setVisible(false);
        }
    }
    
    private void searchStudent() {
        registrationNo = searchField.getText().trim();
        
        if (registrationNo.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter registration number");
            return;
        }
        
        try {
            Conn c = new Conn();
            String query = "SELECT name, registration_no, branch FROM student WHERE registration_no = '" + registrationNo + "'";
            
            ResultSet rs = c.s.executeQuery(query);
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                registrationLabel.setText(rs.getString("registration_no"));
                deptLabel.setText(rs.getString("branch"));
                
                department = rs.getString("branch");
                
                // Get current semester
                String semQuery = "SELECT current_semester FROM student_semester WHERE registration_no = '" + registrationNo + "'";
                ResultSet semRs = c.s.executeQuery(semQuery);
                
                if (semRs.next()) {
                    currentSemester = semRs.getInt("current_semester");
                    semLabel.setText(String.valueOf(currentSemester));
                    
                    loadCourses();
                    loadPreviousResults();
                    
                    coursesPanel.setVisible(true);
                    submit.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Semester information not found for this student!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Student not found!");
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error searching student: " + e.getMessage());
        }
    }
    
    private void loadCourses() {
        coursesPanel.removeAll();
        
        try {
            Conn c = new Conn();
            String query = "SELECT course_code, course_name, credit FROM department_courses " +
                          "WHERE dept = '" + department + "' AND sem = " + currentSemester + " ORDER BY course_code";
            
            ResultSet rs = c.s.executeQuery(query);
            
            int courseCount = 0;
            while (rs.next()) {
                courseCount++;
            }
            
            if (courseCount == 0) {
                JLabel noCoursesLabel = new JLabel("<html><center>No courses found for " + department + " Semester " + currentSemester + 
                    "<br>Courses will be automatically loaded when database is initialized.<br>" +
                    "If this persists, contact admin to add courses.</center></html>");
                noCoursesLabel.setForeground(Color.RED);
                noCoursesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                noCoursesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                coursesPanel.add(noCoursesLabel);
                return;
            }
            
            // Reset the result set
            rs = c.s.executeQuery(query);
            
            gradeBoxes = new JComboBox[courseCount];
            courseLabels = new JLabel[courseCount];
            
            int i = 0;
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                String credit = rs.getString("credit");
                
                // Course name label
                String displayText = courseName;
                if (displayText.length() > 30) {
                    displayText = displayText.substring(0, 27) + "...";
                }
                courseLabels[i] = new JLabel(displayText + " (" + credit + " cr)");
                courseLabels[i].setToolTipText(courseName + " (" + credit + " credits)");
                coursesPanel.add(courseLabels[i]);
                
                // Grade combo box
                String[] grades = {"Select", "4.00", "3.75", "3.50", "3.25", "3.00", "2.75", "2.50", "2.25", "2.00", "0.00"};
                gradeBoxes[i] = new JComboBox<>(grades);
                coursesPanel.add(gradeBoxes[i]);
                
                // Check if grade already exists in database
                checkExistingGrade(courseCode, gradeBoxes[i]);
                
                // Add empty label for spacing
                coursesPanel.add(new JLabel(""));
                
                i++;
            }
            
            coursesPanel.revalidate();
            coursesPanel.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading courses: " + e.getMessage());
        }
    }
    
    private void checkExistingGrade(String courseCode, JComboBox<String> gradeBox) {
        try (Conn c = new Conn()) {
            String query = "SELECT grade_point FROM student_marks " +
                          "WHERE registration_no = ? AND semester = ? AND course_code = ?";
            java.sql.PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, registrationNo);
            ps.setInt(2, currentSemester);
            ps.setString(3, courseCode);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                double grade = rs.getDouble("grade_point");
                gradeBox.setSelectedItem(String.format("%.2f", grade));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Ignore errors - just means no grade exists yet
        }
    }
    
    private void loadPreviousResults() {
        try {
            Conn c = new Conn();
            
            // Calculate cumulative CGPA
            String query = "SELECT SUM(credit * grade_point) as total_grade_points, SUM(credit) as total_credits " +
                          "FROM student_marks WHERE registration_no = '" + registrationNo + "' " +
                          "AND grade_point > 0";
            
            ResultSet rs = c.s.executeQuery(query);
            
            double cumulativeCGPA = 0.0;
            if (rs.next()) {
                double totalGradePoints = rs.getDouble("total_grade_points");
                double totalCredits = rs.getDouble("total_credits");
                
                if (totalCredits > 0) {
                    cumulativeCGPA = totalGradePoints / totalCredits;
                }
            }
            
            cumulativeLabel.setText(String.format("%.2f", cumulativeCGPA));
            
            // Calculate failed courses and credits
            String failedQuery = "SELECT COUNT(*) as failed_courses, SUM(credit) as failed_credits " +
                               "FROM student_marks WHERE registration_no = '" + registrationNo + "' " +
                               "AND grade_point = 0";
            
            ResultSet failedRs = c.s.executeQuery(failedQuery);
            
            int failedCourses = 0;
            double failedCredits = 0;
            if (failedRs.next()) {
                failedCourses = failedRs.getInt("failed_courses");
                failedCredits = failedRs.getDouble("failed_credits");
            }
            
            failedCoursesLabel.setText(String.valueOf(failedCourses));
            failedCreditsLabel.setText(String.format("%.1f", failedCredits));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading previous results: " + e.getMessage());
        }
    }
    
    private void submitMarks() {
        try {
            Conn c = new Conn();
            
            double totalGradePoints = 0;
            double totalCredits = 0;
            int failedCourses = 0;
            double failedCredits = 0;
            boolean allGradesSelected = true;
            
            // First validate that all grades are selected
            for (int i = 0; i < gradeBoxes.length; i++) {
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                if ("Select".equals(selectedGrade)) {
                    allGradesSelected = false;
                    JOptionPane.showMessageDialog(null, "Please select grade for all courses");
                    return;
                }
            }
            
            if (!allGradesSelected) {
                return;
            }
            
            // Process each course
            for (int i = 0; i < gradeBoxes.length; i++) {
                String courseCode = "";
                double credit = 0;
                
                // Get course code and credit from database
                String courseQuery = "SELECT course_code, credit FROM department_courses " +
                                   "WHERE dept = '" + department + "' AND sem = " + currentSemester + " " +
                                   "ORDER BY course_code LIMIT " + i + ", 1";
                
                ResultSet courseRs = c.s.executeQuery(courseQuery);
                if (courseRs.next()) {
                    courseCode = courseRs.getString("course_code");
                    credit = courseRs.getDouble("credit");
                }
                
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                double gradePoint = Double.parseDouble(selectedGrade);
                
                // Insert or update marks in database
                String insertQuery = "INSERT INTO student_marks (registration_no, semester, course_code, credit, grade_point) " +
                                   "VALUES ('" + registrationNo + "', " + currentSemester + ", '" + courseCode + "', " + credit + ", " + gradePoint + ") " +
                                   "ON DUPLICATE KEY UPDATE grade_point = " + gradePoint;
                
                c.s.executeUpdate(insertQuery);
                
                if (gradePoint > 0) {
                    totalGradePoints += credit * gradePoint;
                    totalCredits += credit;
                } else {
                    failedCourses++;
                    failedCredits += credit;
                }
            }
            
            // Calculate semester CGPA
            double semesterCGPA = (totalCredits > 0) ? totalGradePoints / totalCredits : 0.0;
            cgpaLabel.setText(String.format("%.2f", semesterCGPA));
            
            // Update failed courses and credits
            failedCoursesLabel.setText(String.valueOf(failedCourses));
            failedCreditsLabel.setText(String.format("%.1f", failedCredits));
            
            // Update cumulative CGPA
            loadPreviousResults();
            
            // Always promote to next semester regardless of failed courses
            if (currentSemester < 8) {
                String updateQuery = "UPDATE student_semester SET current_semester = " + (currentSemester + 1) + 
                                   " WHERE registration_no = '" + registrationNo + "'";
                c.s.executeUpdate(updateQuery);
                
                semLabel.setText(String.valueOf(currentSemester + 1));
                currentSemester++; // Update the current semester variable
                
                if (failedCourses > 0) {
                    JOptionPane.showMessageDialog(null, 
                        "Marks submitted successfully! Student promoted to semester " + currentSemester + 
                        " with " + failedCourses + " failed course(s) (" + failedCredits + " credits).");
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Marks submitted successfully! Student promoted to semester " + currentSemester);
                }
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Marks submitted successfully! Student has completed all semesters." +
                    (failedCourses > 0 ? " But has " + failedCourses + " failed course(s)." : ""));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error submitting marks: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        nameLabel.setText("");
        registrationLabel.setText("");
        deptLabel.setText("");
        semLabel.setText("");
        cgpaLabel.setText("");
        cumulativeLabel.setText("");
        failedCoursesLabel.setText("");
        failedCreditsLabel.setText("");
        coursesPanel.setVisible(false);
        submit.setEnabled(false);
    }
    
    public static void main(String[] args) {
        new EnterMarks();
    }
}

*/


package university.management.system;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EnterMarks extends JFrame implements ActionListener {
    
    JTextField searchField;
    JButton search, submit, cancel;
    JComboBox<String>[] gradeBoxes;
    JLabel[] courseLabels;
    JLabel nameLabel, registrationLabel, deptLabel, semLabel, cgpaLabel, cumulativeLabel, failedCoursesLabel, failedCreditsLabel;
    String registrationNo, department;
    int currentSemester;
    JPanel coursesPanel;
    
    // Use package-level Conn (Aiven) defined elsewhere; remove local override
    
    EnterMarks() {
        final boolean allowMarksEntryHere = false;
        if (!allowMarksEntryHere) {
            JOptionPane.showMessageDialog(this,
                    "Marks entry is now available only for Teachers.\n\nTeachers submit marks for approval, then Admin approves them from 'Approve Results'.");
            dispose();
            return;
        }

        setSize(980, 650);
        setLocation(300, 40);
        setTitle("Enter Student Marks");
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Search section
        JLabel heading = new JLabel("Enter Student Marks");
        UITheme.styleTitle(heading);
        heading.setBounds(50, 10, 400, 30);
        add(heading);
        
        JLabel searchLabel = new JLabel("Search by Registration No:");
        UITheme.styleLabel(searchLabel);
        searchLabel.setBounds(50, 60, 200, 20);
        add(searchLabel);
        
        searchField = new JTextField();
        UITheme.styleField(searchField);
        searchField.setBounds(250, 60, 150, 30);
        add(searchField);
        
        search = new JButton("Search");
        UITheme.stylePrimary(search);
        search.setBounds(420, 60, 100, 30);
        search.addActionListener(this);
        add(search);
        
        // Student info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(50, 100, 800, 80);
        infoPanel.setLayout(new GridLayout(2, 4, 10, 10));
        UITheme.styleCard(infoPanel);
        
        JLabel nameText = new JLabel("Name:");
        UITheme.styleLabel(nameText);
        nameLabel = new JLabel("");
        UITheme.styleLabel(nameLabel);
        JLabel regText = new JLabel("Registration No:");
        UITheme.styleLabel(regText);
        registrationLabel = new JLabel("");
        UITheme.styleLabel(registrationLabel);
        JLabel deptText = new JLabel("Department:");
        UITheme.styleLabel(deptText);
        deptLabel = new JLabel("");
        UITheme.styleLabel(deptLabel);
        JLabel semText = new JLabel("Current Semester:");
        UITheme.styleLabel(semText);
        semLabel = new JLabel("");
        UITheme.styleLabel(semLabel);
        
        infoPanel.add(nameText);
        infoPanel.add(nameLabel);
        infoPanel.add(regText);
        infoPanel.add(registrationLabel);
        infoPanel.add(deptText);
        infoPanel.add(deptLabel);
        infoPanel.add(semText);
        infoPanel.add(semLabel);
        
        add(infoPanel);
        
        // Courses panel
        coursesPanel = new JPanel();
        coursesPanel.setLayout(new GridLayout(0, 3, 10, 10));
        UITheme.styleCard(coursesPanel);
        coursesPanel.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setBounds(50, 200, 800, 250);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        UITheme.styleScroll(scrollPane);
        add(scrollPane);
        
        // Results panel
        JPanel resultPanel = new JPanel();
        resultPanel.setBounds(50, 460, 800, 80);
        resultPanel.setLayout(new GridLayout(2, 4, 10, 10));
        UITheme.styleCard(resultPanel);
        
        JLabel cgpaText = new JLabel("Semester CGPA:");
        UITheme.styleLabel(cgpaText);
        cgpaLabel = new JLabel("");
        UITheme.styleLabel(cgpaLabel);
        JLabel cumulativeText = new JLabel("Cumulative CGPA:");
        UITheme.styleLabel(cumulativeText);
        cumulativeLabel = new JLabel("");
        UITheme.styleLabel(cumulativeLabel);
        JLabel failedCoursesText = new JLabel("Failed Courses:");
        UITheme.styleLabel(failedCoursesText);
        failedCoursesLabel = new JLabel("");
        UITheme.styleLabel(failedCoursesLabel);
        JLabel failedCreditsText = new JLabel("Failed Credits:");
        UITheme.styleLabel(failedCreditsText);
        failedCreditsLabel = new JLabel("");
        UITheme.styleLabel(failedCreditsLabel);
        
        resultPanel.add(cgpaText);
        resultPanel.add(cgpaLabel);
        resultPanel.add(cumulativeText);
        resultPanel.add(cumulativeLabel);
        resultPanel.add(failedCoursesText);
        resultPanel.add(failedCoursesLabel);
        resultPanel.add(failedCreditsText);
        resultPanel.add(failedCreditsLabel);
        
        add(resultPanel);
        
        // Buttons panel - fixed at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBounds(50, 550, 800, 50);
        buttonPanel.setOpaque(false);
        
        submit = new JButton("Submit");
        UITheme.stylePrimary(submit);
        submit.setPreferredSize(new Dimension(120, 35));
        submit.addActionListener(this);
        submit.setEnabled(false);
        buttonPanel.add(submit);
        
        cancel = new JButton("Cancel");
        UITheme.styleDanger(cancel);
        cancel.setPreferredSize(new Dimension(120, 35));
        cancel.addActionListener(this);
        buttonPanel.add(cancel);
        
        add(buttonPanel);
        
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == search) {
            searchStudent();
        } else if (ae.getSource() == submit) {
            submitMarks();
        } else if (ae.getSource() == cancel) {
            setVisible(false);
        }
    }
    
    private void searchStudent() {
        registrationNo = searchField.getText().trim();
        
        if (registrationNo.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter registration number");
            return;
        }
        
        try {
            Conn c = new Conn();
            String query = "SELECT name, registration_no, branch FROM student WHERE registration_no = '" + registrationNo + "'";
            
            ResultSet rs = c.s.executeQuery(query);
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                registrationLabel.setText(rs.getString("registration_no"));
                deptLabel.setText(rs.getString("branch"));
                
                department = rs.getString("branch");
                
                // Get current semester
                String semQuery = "SELECT current_semester FROM student_semester WHERE registration_no = '" + registrationNo + "'";
                ResultSet semRs = c.s.executeQuery(semQuery);
                
                if (semRs.next()) {
                    currentSemester = semRs.getInt("current_semester");
                    semLabel.setText(String.valueOf(currentSemester));
                    
                    loadCourses();
                    loadPreviousResults();
                    
                    coursesPanel.setVisible(true);
                    submit.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Semester information not found for this student!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Student not found!");
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error searching student: " + e.getMessage());
        }
    }
    

    
    private void loadCourses() {
        coursesPanel.removeAll();
        
        try {
            Conn c = new Conn();
            String query = "SELECT course_code, course_name, credit FROM department_courses " +
                          "WHERE dept = '" + department + "' AND sem = " + currentSemester + " ORDER BY course_code";
            
            ResultSet rs = c.s.executeQuery(query);
            
            int courseCount = 0;
            while (rs.next()) {
                courseCount++;
            }
            
            if (courseCount == 0) {
                JLabel noCoursesLabel = new JLabel("<html><center>No courses found for " + department + " Semester " + currentSemester + 
                    "<br>Courses will be automatically loaded when database is initialized.<br>" +
                    "If this persists, contact admin to add courses.</center></html>");
                noCoursesLabel.setForeground(Color.RED);
                noCoursesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                noCoursesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                coursesPanel.add(noCoursesLabel);
                return;
            }
            
            // Reset the result set
            rs = c.s.executeQuery(query);
            
            gradeBoxes = new JComboBox[courseCount];
            courseLabels = new JLabel[courseCount];
            
            int i = 0;
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                String credit = rs.getString("credit");
                
                // Course name label
                String displayText = courseName;
                if (displayText.length() > 30) {
                    displayText = displayText.substring(0, 27) + "...";
                }
                courseLabels[i] = new JLabel(displayText + " (" + credit + " cr)");
                courseLabels[i].setToolTipText(courseName + " (" + credit + " credits)");
                coursesPanel.add(courseLabels[i]);
                
                // Grade combo box
                String[] grades = {"Select", "4.00", "3.75", "3.50", "3.25", "3.00", "2.75", "2.50", "2.25", "2.00", "0.00"};
                gradeBoxes[i] = new JComboBox<>(grades);
                coursesPanel.add(gradeBoxes[i]);
                
                // Check if grade already exists in database
                checkExistingGrade(courseCode, gradeBoxes[i]);
                
                // Add empty label for spacing
                coursesPanel.add(new JLabel(""));
                
                i++;
            }
            
            coursesPanel.revalidate();
            coursesPanel.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading courses: " + e.getMessage());
        }
    }
    
    private void checkExistingGrade(String courseCode, JComboBox<String> gradeBox) {
        try (Conn c = new Conn()) {
            String query = "SELECT grade_point FROM student_marks " +
                          "WHERE registration_no = ? AND semester = ? AND course_code = ?";
            java.sql.PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, registrationNo);
            ps.setInt(2, currentSemester);
            ps.setString(3, courseCode);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                double grade = rs.getDouble("grade_point");
                gradeBox.setSelectedItem(String.format("%.2f", grade));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Ignore errors - just means no grade exists yet
        }
    }
    
    private void loadPreviousResults() {
        try {
            Conn c = new Conn();
            
            // Calculate cumulative CGPA
            String query = "SELECT SUM(credit * grade_point) as total_grade_points, SUM(credit) as total_credits " +
                          "FROM student_marks WHERE registration_no = '" + registrationNo + "' " +
                          "AND grade_point > 0";
            
            ResultSet rs = c.s.executeQuery(query);
            
            double cumulativeCGPA = 0.0;
            if (rs.next()) {
                double totalGradePoints = rs.getDouble("total_grade_points");
                double totalCredits = rs.getDouble("total_credits");
                
                if (totalCredits > 0) {
                    cumulativeCGPA = totalGradePoints / totalCredits;
                }
            }
            
            cumulativeLabel.setText(String.format("%.2f", cumulativeCGPA));
            
            // Calculate failed courses and credits
            String failedQuery = "SELECT COUNT(*) as failed_courses, SUM(credit) as failed_credits " +
                               "FROM student_marks WHERE registration_no = '" + registrationNo + "' " +
                               "AND grade_point = 0";
            
            ResultSet failedRs = c.s.executeQuery(failedQuery);
            
            int failedCourses = 0;
            double failedCredits = 0;
            if (failedRs.next()) {
                failedCourses = failedRs.getInt("failed_courses");
                failedCredits = failedRs.getDouble("failed_credits");
            }
            
            failedCoursesLabel.setText(String.valueOf(failedCourses));
            failedCreditsLabel.setText(String.format("%.1f", failedCredits));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading previous results: " + e.getMessage());
        }
    }
    
    private void submitMarks() {
        try {
            Conn c = new Conn();
            
            double totalGradePoints = 0;
            double totalCredits = 0;
            int failedCourses = 0;
            double failedCredits = 0;
            boolean allGradesSelected = true;
            
            // First validate that all grades are selected
            for (int i = 0; i < gradeBoxes.length; i++) {
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                if ("Select".equals(selectedGrade)) {
                    allGradesSelected = false;
                    JOptionPane.showMessageDialog(null, "Please select grade for all courses");
                    return;
                }
            }
            
            if (!allGradesSelected) {
                return;
            }
            
            // Process each course
            for (int i = 0; i < gradeBoxes.length; i++) {
                String courseCode = "";
                double credit = 0;
                
                // Get course code and credit from database
                String courseQuery = "SELECT course_code, credit FROM department_courses " +
                                   "WHERE dept = '" + department + "' AND sem = " + currentSemester + " " +
                                   "ORDER BY course_code LIMIT " + i + ", 1";
                
                ResultSet courseRs = c.s.executeQuery(courseQuery);
                if (courseRs.next()) {
                    courseCode = courseRs.getString("course_code");
                    credit = courseRs.getDouble("credit");
                }
                
                String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                double gradePoint = Double.parseDouble(selectedGrade);
                
                // Insert or update marks in database
                String insertQuery = "INSERT INTO student_marks (registration_no, semester, course_code, credit, grade_point) " +
                                   "VALUES ('" + registrationNo + "', " + currentSemester + ", '" + courseCode + "', " + credit + ", " + gradePoint + ") " +
                                   "ON DUPLICATE KEY UPDATE grade_point = " + gradePoint;
                
                c.s.executeUpdate(insertQuery);
                
                if (gradePoint > 0) {
                    totalGradePoints += credit * gradePoint;
                    totalCredits += credit;
                } else {
                    failedCourses++;
                    failedCredits += credit;
                }
            }
            
            // Calculate semester CGPA
            double semesterCGPA = (totalCredits > 0) ? totalGradePoints / totalCredits : 0.0;
            cgpaLabel.setText(String.format("%.2f", semesterCGPA));
            
            // Update failed courses and credits
            failedCoursesLabel.setText(String.valueOf(failedCourses));
            failedCreditsLabel.setText(String.format("%.1f", failedCredits));
            
            // Calculate and save result summary for performance chart
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            calculateAndSaveResultSummary(c, currentSemester, currentYear, totalGradePoints, totalCredits, failedCourses);
            
            // Update cumulative CGPA
            loadPreviousResults();
            
            // Always promote to next semester regardless of failed courses
            if (currentSemester < 8) {
                String updateQuery = "UPDATE student_semester SET current_semester = " + (currentSemester + 1) + 
                                   " WHERE registration_no = '" + registrationNo + "'";
                c.s.executeUpdate(updateQuery);
                
                semLabel.setText(String.valueOf(currentSemester + 1));
                currentSemester++; // Update the current semester variable
                
                if (failedCourses > 0) {
                    JOptionPane.showMessageDialog(null, 
                        "Marks submitted successfully! Student promoted to semester " + currentSemester + 
                        " with " + failedCourses + " failed course(s) (" + failedCredits + " credits).");
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Marks submitted successfully! Student promoted to semester " + currentSemester);
                }
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Marks submitted successfully! Student has completed all semesters." +
                    (failedCourses > 0 ? " But has " + failedCourses + " failed course(s)." : ""));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error submitting marks: " + e.getMessage());
        }
    }
    
    private void calculateAndSaveResultSummary(Conn c, int semester, int examYear, double totalGradePoints, double totalCredits, int failedCourses) {
        try {
            // Calculate GPA
            double gpa = (totalCredits > 0) ? totalGradePoints / totalCredits : 0.0;
            
            // Get total marks (sum of credits * 4.0 as max grade point)
            double totalMarks = 0;
            double obtainedMarks = totalGradePoints;
            
            // Get total possible marks for this semester
            String totalMarksQuery = "SELECT SUM(credit) as total_credits FROM department_courses " +
                                    "WHERE dept = '" + department + "' AND sem = " + semester;
            ResultSet totalRs = c.s.executeQuery(totalMarksQuery);
            if (totalRs.next()) {
                double totalSemCredits = totalRs.getDouble("total_credits");
                totalMarks = totalSemCredits * 4.0; // Assuming max grade point is 4.0
            }
            
            // Calculate percentage
            double percentage = (totalMarks > 0) ? (obtainedMarks / totalMarks) * 100 : 0.0;
            
            // Determine result status
            String result = (failedCourses > 0) ? "FAIL" : "PASS";
            
            // Insert or update result_summary
            String insertSql = "INSERT INTO result_summary " +
                             "(registration_no, semester, exam_year, total_marks, obtained_marks, percentage, gpa, result) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE " +
                             "total_marks=VALUES(total_marks), obtained_marks=VALUES(obtained_marks), " +
                             "percentage=VALUES(percentage), gpa=VALUES(gpa), result=VALUES(result)";
            
            try (PreparedStatement ps = c.c.prepareStatement(insertSql)) {
                ps.setString(1, registrationNo);
                ps.setInt(2, semester);
                ps.setInt(3, examYear);
                ps.setDouble(4, totalMarks);
                ps.setDouble(5, obtainedMarks);
                ps.setDouble(6, percentage);
                ps.setDouble(7, gpa);
                ps.setString(8, result);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Don't show error to user, just log it
        }
    }
    
    private void clearFields() {
        nameLabel.setText("");
        registrationLabel.setText("");
        deptLabel.setText("");
        semLabel.setText("");
        cgpaLabel.setText("");
        cumulativeLabel.setText("");
        failedCoursesLabel.setText("");
        failedCreditsLabel.setText("");
        coursesPanel.setVisible(false);
        submit.setEnabled(false);
    }
    
    public static void main(String[] args) {
        new EnterMarks();
    }
}