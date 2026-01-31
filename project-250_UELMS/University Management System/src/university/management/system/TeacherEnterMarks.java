package university.management.system;
import university.management.system.dao.ResultDAO;
import university.management.system.models.StudentResult;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherEnterMarks extends JFrame implements ActionListener {
    
    JTextField searchField;
    JButton search, submit, cancel;
    JComboBox<String>[] gradeBoxes;
    JLabel[] courseLabels;
    JLabel nameLabel, registrationLabel, deptLabel, semLabel, cgpaLabel, cumulativeLabel, failedCoursesLabel, failedCreditsLabel;
    String registrationNo, department;
    int currentSemester;
    JPanel coursesPanel;
    private JLabel lblStatus;

    private static final class CourseRow {
        final String code;
        final String name;
        final double credit;
        CourseRow(String code, String name, double credit) {
            this.code = code;
            this.name = name;
            this.credit = credit;
        }
    }

    private List<CourseRow> loadedCourses = new ArrayList<>();
    
    public TeacherEnterMarks() {
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

        lblStatus = new JLabel(" ");
        UITheme.styleLabel(lblStatus);
        lblStatus.setBounds(50, 610, 800, 20);
        add(lblStatus);
        
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
            searchStudentAsync();
        } else if (ae.getSource() == submit) {
            submitMarksAsync();
        } else if (ae.getSource() == cancel) {
            setVisible(false);
        }
    }
    
    private void searchStudentAsync() {
        registrationNo = searchField.getText().trim();
        if (registrationNo.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter registration number");
            return;
        }

        search.setEnabled(false);
        submit.setEnabled(false);
        lblStatus.setText("Loading student... please wait");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String name;
            private String reg;
            private String dept;
            private int sem;

            @Override
            protected Boolean doInBackground() {
                try (Conn c = new Conn()) {
                    String query = "SELECT name, registration_no, branch FROM student WHERE registration_no = ?";
                    try (PreparedStatement ps = c.c.prepareStatement(query)) {
                        ps.setString(1, registrationNo);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) return false;
                            name = rs.getString("name");
                            reg = rs.getString("registration_no");
                            dept = rs.getString("branch");
                        }
                    }

                    String semQuery = "SELECT current_semester FROM student_semester WHERE registration_no = ?";
                    try (PreparedStatement ps = c.c.prepareStatement(semQuery)) {
                        ps.setString(1, registrationNo);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) return false;
                            sem = rs.getInt("current_semester");
                        }
                    }
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        JOptionPane.showMessageDialog(null, "Student not found or semester info missing!");
                        clearFields();
                        return;
                    }

                    nameLabel.setText(name);
                    registrationLabel.setText(reg);
                    deptLabel.setText(dept);
                    department = dept;
                    currentSemester = sem;
                    semLabel.setText(String.valueOf(currentSemester));

                    loadCoursesAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error searching student: " + e.getMessage());
                    clearFields();
                } finally {
                    search.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void loadCoursesAsync() {
        coursesPanel.setVisible(false);
        coursesPanel.removeAll();
        coursesPanel.revalidate();
        coursesPanel.repaint();

        lblStatus.setText("Loading courses... please wait");
        submit.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Map<String, Double> existingGrades = new HashMap<>();

            @Override
            protected Boolean doInBackground() {
                loadedCourses.clear();
                try (Conn c = new Conn()) {
                    String query = "SELECT course_code, course_name, credit FROM department_courses " +
                            "WHERE dept = ? AND sem = ? ORDER BY course_code";
                    try (PreparedStatement ps = c.c.prepareStatement(query)) {
                        ps.setString(1, department);
                        ps.setInt(2, currentSemester);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                loadedCourses.add(new CourseRow(
                                        rs.getString("course_code"),
                                        rs.getString("course_name"),
                                        rs.getDouble("credit")
                                ));
                            }
                        }
                    }

                    if (loadedCourses.isEmpty()) return false;

                    // Load existing grades for this semester in ONE query
                    String gradeSql = "SELECT subject_code, grade_point, exam_year " +
                            "FROM student_result WHERE registration_no=? AND semester=? ORDER BY exam_year DESC";
                    try (PreparedStatement ps = c.c.prepareStatement(gradeSql)) {
                        ps.setString(1, registrationNo);
                        ps.setInt(2, currentSemester);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String code = rs.getString("subject_code");
                                if (existingGrades.containsKey(code)) continue; // keep latest
                                double gp = rs.getDouble("grade_point");
                                existingGrades.put(code, gp);
                            }
                        }
                    }
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        JLabel noCoursesLabel = new JLabel("<html><center>No courses found for " + department + " Semester " + currentSemester +
                                "<br>Courses will be automatically loaded when database is initialized.<br>" +
                                "If this persists, contact admin to add courses.</center></html>");
                        noCoursesLabel.setForeground(Color.RED);
                        noCoursesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        noCoursesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        coursesPanel.add(noCoursesLabel);
                        coursesPanel.setVisible(true);
                        lblStatus.setText("No courses found");
                        return;
                    }

                    coursesPanel.removeAll();
                    gradeBoxes = new JComboBox[loadedCourses.size()];
                    courseLabels = new JLabel[loadedCourses.size()];

                    String[] grades = {"Select", "4.00", "3.75", "3.50", "3.25", "3.00", "2.75", "2.50", "2.25", "2.00", "0.00"};

                    for (int i = 0; i < loadedCourses.size(); i++) {
                        CourseRow cr = loadedCourses.get(i);
                        String displayText = cr.name;
                        if (displayText.length() > 30) displayText = displayText.substring(0, 27) + "...";
                        courseLabels[i] = new JLabel(displayText + " (" + cr.credit + " cr)");
                        courseLabels[i].setToolTipText(cr.name + " (" + cr.credit + " credits)");
                        coursesPanel.add(courseLabels[i]);

                        gradeBoxes[i] = new JComboBox<>(grades);
                        coursesPanel.add(gradeBoxes[i]);

                        Double gp = existingGrades.get(cr.code);
                        if (gp != null) {
                            gradeBoxes[i].setSelectedItem(String.format("%.2f", gp));
                        }

                        coursesPanel.add(new JLabel(""));
                    }

                    coursesPanel.revalidate();
                    coursesPanel.repaint();
                    coursesPanel.setVisible(true);

                    // Load previous results after course load (still async, no freeze)
                    loadPreviousResultsAsync();

                    submit.setEnabled(true);
                    lblStatus.setText("Ready");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error loading courses: " + e.getMessage());
                    lblStatus.setText("Failed to load courses");
                }
            }
        };
        worker.execute();
    }
    
    // Old synchronous course loading removed (now async + batched)
    
    private void loadPreviousResultsAsync() {
        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            @Override
            protected double[] doInBackground() {
                try (Conn c = new Conn()) {
                    double cumulativeCGPA = 0.0;
                    int failedCourses = 0;
                    double failedCredits = 0.0;

                    String query = "SELECT SUM(dc.credit * sr.grade_point) as total_grade_points, SUM(dc.credit) as total_credits " +
                            "FROM student_result sr " +
                            "JOIN department_courses dc ON sr.subject_code = dc.course_code " +
                            "WHERE sr.registration_no = ? AND sr.is_approved = TRUE";
                    try (PreparedStatement ps = c.c.prepareStatement(query)) {
                        ps.setString(1, registrationNo);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                double totalGradePoints = rs.getDouble("total_grade_points");
                                double totalCredits = rs.getDouble("total_credits");
                                if (totalCredits > 0) cumulativeCGPA = totalGradePoints / totalCredits;
                            }
                        }
                    }

                    String failedQuery = "SELECT COUNT(*) as failed_courses, COALESCE(SUM(dc.credit),0) as failed_credits " +
                            "FROM student_result sr " +
                            "JOIN department_courses dc ON sr.subject_code = dc.course_code " +
                            "WHERE sr.registration_no = ? AND sr.is_approved = TRUE AND sr.status = 'FAIL'";
                    try (PreparedStatement ps = c.c.prepareStatement(failedQuery)) {
                        ps.setString(1, registrationNo);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                failedCourses = rs.getInt("failed_courses");
                                failedCredits = rs.getDouble("failed_credits");
                            }
                        }
                    }

                    return new double[]{cumulativeCGPA, failedCourses, failedCredits};
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    double[] res = get();
                    cumulativeLabel.setText(String.format("%.2f", res[0]));
                    failedCoursesLabel.setText(String.valueOf((int) res[1]));
                    failedCreditsLabel.setText(String.format("%.1f", res[2]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void submitMarksAsync() {
        if (gradeBoxes == null || gradeBoxes.length == 0 || loadedCourses == null || loadedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No courses loaded.");
            return;
        }

        // Validate grades selected (fast, on EDT)
        for (JComboBox<String> gb : gradeBoxes) {
            String selectedGrade = (String) gb.getSelectedItem();
            if ("Select".equals(selectedGrade)) {
                JOptionPane.showMessageDialog(null, "Please select grade for all courses");
                return;
            }
        }

        submit.setEnabled(false);
        search.setEnabled(false);
        lblStatus.setText("Submitting... please wait");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private int failedCourses;
            private double failedCredits;
            private double semesterCGPA;

            @Override
            protected Boolean doInBackground() {
                int examYear = Calendar.getInstance().get(Calendar.YEAR);

                double totalGradePoints = 0;
                double totalCredits = 0;
                failedCourses = 0;
                failedCredits = 0;

                List<StudentResult> toSave = new ArrayList<>();

                for (int i = 0; i < loadedCourses.size(); i++) {
                    CourseRow cr = loadedCourses.get(i);
                    String selectedGrade = (String) gradeBoxes[i].getSelectedItem();
                    double gradePoint = Double.parseDouble(selectedGrade);

                    StudentResult r = new StudentResult();
                    r.setRegistrationNo(registrationNo);
                    r.setSubjectCode(cr.code);
                    r.setSemester(currentSemester);
                    r.setExamYear(examYear);
                    r.setExamType("Regular");
                    r.setGrade(mapGradePointToGrade(gradePoint));
                    r.setGradePoint(gradePoint);
                    r.setStatus(gradePoint >= 2.0 ? "PASS" : "FAIL");
                    r.setMarksObtained(estimateMarksFromGradePoint(gradePoint));
                    r.setMarksAttendance(0);
                    r.setMarksEval(0);
                    r.setMarksTerm(0);
                    r.setMarksFinal(0);
                    toSave.add(r);

                    if (gradePoint > 0) {
                        totalGradePoints += cr.credit * gradePoint;
                        totalCredits += cr.credit;
                    } else {
                        failedCourses++;
                        failedCredits += cr.credit;
                    }
                }

                semesterCGPA = (totalCredits > 0) ? totalGradePoints / totalCredits : 0.0;

                return ResultDAO.saveResultsBatch(toSave, 100, 40);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        JOptionPane.showMessageDialog(null, "Error submitting marks: Failed to save results");
                        return;
                    }

                    cgpaLabel.setText(String.format("%.2f", semesterCGPA));
                    failedCoursesLabel.setText(String.valueOf(failedCourses));
                    failedCreditsLabel.setText(String.format("%.1f", failedCredits));

                    loadPreviousResultsAsync();

                    JOptionPane.showMessageDialog(null,
                            "Marks submitted for admin approval. Results will be published after approval." +
                                    (failedCourses > 0 ? " (Currently contains " + failedCourses + " failing course(s).)" : ""));
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error submitting marks: " + e.getMessage());
                } finally {
                    submit.setEnabled(true);
                    search.setEnabled(true);
                    lblStatus.setText(" ");
                }
            }
        };
        worker.execute();
    }

    private String mapGradePointToGrade(double gp) {
        // Matches the UI's grade-point list
        if (gp >= 4.00) return "A+";
        if (gp >= 3.75) return "A";
        if (gp >= 3.50) return "B+";
        if (gp >= 3.25) return "B";
        if (gp >= 3.00) return "C+";
        if (gp >= 2.75) return "C";
        if (gp >= 2.50) return "C";
        if (gp >= 2.25) return "C";
        if (gp >= 2.00) return "C";
        return "F";
    }

    private double estimateMarksFromGradePoint(double gp) {
        // Provide a stable, non-null mark number for charts/reports.
        // This is an estimation since this screen collects grade-points, not raw marks.
        if (gp >= 4.00) return 95.0;
        if (gp >= 3.75) return 85.0;
        if (gp >= 3.50) return 75.0;
        if (gp >= 3.25) return 65.0;
        if (gp >= 3.00) return 55.0;
        if (gp >= 2.75) return 45.0;
        if (gp >= 2.50) return 42.0;
        if (gp >= 2.25) return 41.0;
        if (gp >= 2.00) return 40.0;
        return 0.0;
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
        new TeacherEnterMarks();
    }
}
