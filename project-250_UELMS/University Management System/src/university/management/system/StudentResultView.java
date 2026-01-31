package university.management.system;

import university.management.system.dao.ResultDAO;
import university.management.system.dao.SummaryDAO;
import university.management.system.models.StudentResult;
import university.management.system.models.ResultSummary;
import university.management.system.charts.StudentPerformanceChart;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Student interface for viewing results with GPA/CGPA
 */
public class StudentResultView extends JFrame implements ActionListener {
    private JTable resultTable, summaryTable;
    private DefaultTableModel resultModel, summaryModel;
    private JComboBox<Integer> cbSemester;
    private JComboBox<Integer> cbYear;
    private JButton btnView, btnCancel;
    private JLabel lblCGPA, lblGPA;
    private String registrationNo;
    
    public StudentResultView(String registrationNo) {
        this.registrationNo = registrationNo;
        setTitle("View My Results");
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Heading
        JLabel heading = new JLabel("My Results");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1100, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);
        
        // Filters
        JLabel lblSem = new JLabel("Semester:");
        UITheme.styleLabel(lblSem);
        lblSem.setBounds(20, 50, 80, 25);
        add(lblSem);
        
        cbSemester = new JComboBox<>(new Integer[]{null, 1, 2, 3, 4, 5, 6, 7, 8});
        cbSemester.setBounds(110, 48, 100, 28);
        cbSemester.insertItemAt(null, 0);
        cbSemester.setSelectedIndex(0);
        add(cbSemester);
        
        JLabel lblYear = new JLabel("Year:");
        UITheme.styleLabel(lblYear);
        lblYear.setBounds(230, 50, 50, 25);
        add(lblYear);
        
        cbYear = new JComboBox<>();
        cbYear.setBounds(290, 48, 100, 28);
        loadYears();
        add(cbYear);
        
        btnView = new JButton("View Results");
        UITheme.stylePrimary(btnView);
        btnView.setBounds(410, 48, 120, 28);
        btnView.addActionListener(this);
        add(btnView);
        
        btnCancel = new JButton("Close");
        UITheme.styleGhost(btnCancel);
        btnCancel.setBounds(550, 48, 100, 28);
        btnCancel.addActionListener(this);
        add(btnCancel);
        
        // GPA/CGPA Display
        JPanel gpaPanel = UITheme.cardPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        gpaPanel.setBounds(20, 90, 1060, 50);
        
        JLabel lblGpaText = new JLabel("Semester GPA:");
        UITheme.styleLabel(lblGpaText);
        gpaPanel.add(lblGpaText);
        lblGPA = new JLabel("0.00");
        lblGPA.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGPA.setForeground(UITheme.ACCENT_PINK);
        gpaPanel.add(lblGPA);
        
        JLabel lblCgpaText = new JLabel("Cumulative GPA (CGPA):");
        UITheme.styleLabel(lblCgpaText);
        gpaPanel.add(lblCgpaText);
        lblCGPA = new JLabel("0.00");
        lblCGPA.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCGPA.setForeground(UITheme.ACCENT_PINK);
        gpaPanel.add(lblCGPA);
        
        add(gpaPanel);
        
        // Result details table
        String[] resultColumns = {"Subject Code", "Marks Obtained", "Grade", "Grade Point", "Status", "Semester", "Year", "Exam Type"};
        resultModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultModel);
        UITheme.styleTable(resultTable);
        
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBounds(20, 150, 560, 250);
        add(resultScroll);
        
        // Performance Chart (on the right side)
        JLabel chartLabel = new JLabel("Performance Chart");
        UITheme.styleTitle(chartLabel);
        chartLabel.setBounds(600, 150, 480, 25);
        add(chartLabel);
        
        StudentPerformanceChart performanceChart = new StudentPerformanceChart(registrationNo);
        performanceChart.setBounds(600, 180, 480, 220);
        add(performanceChart);
        
        // Summary table
        String[] summaryColumns = {"Semester", "Year", "Total Marks", "Obtained Marks", "Percentage", "GPA", "Result"};
        summaryModel = new DefaultTableModel(summaryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        summaryTable = new JTable(summaryModel);
        UITheme.styleTable(summaryTable);
        
        JScrollPane summaryScroll = new JScrollPane(summaryTable);
        summaryScroll.setBounds(20, 420, 1060, 200);
        add(summaryScroll);
        
        // Load all results initially
        loadAllResults();
        loadCGPA();
        
        setVisible(true);
    }
    
    private void loadYears() {
        try (Conn c = new Conn()) {
            String query = "SELECT DISTINCT exam_year FROM student_result WHERE registration_no = ? ORDER BY exam_year DESC";
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(query)) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    cbYear.addItem(null);
                    while (rs.next()) {
                        cbYear.addItem(rs.getInt("exam_year"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadAllResults() {
        resultModel.setRowCount(0);
        Integer semester = (Integer) cbSemester.getSelectedItem();
        Integer year = (Integer) cbYear.getSelectedItem();
        
        List<StudentResult> results = ResultDAO.getStudentResults(registrationNo, semester, year);
        
        double totalGradePoints = 0;
        int count = 0;
        
        for (StudentResult result : results) {
            if (result.isApproved()) { // Only show approved results
                resultModel.addRow(new Object[]{
                    result.getSubjectCode(),
                    String.format("%.2f", result.getMarksObtained()),
                    result.getGrade(),
                    String.format("%.2f", result.getGradePoint()),
                    result.getStatus(),
                    result.getSemester(),
                    result.getExamYear(),
                    result.getExamType()
                });
                
                totalGradePoints += result.getGradePoint();
                count++;
            }
        }
        
        // Calculate and display GPA for filtered results
        if (count > 0) {
            double gpa = totalGradePoints / count;
            lblGPA.setText(String.format("%.2f", gpa));
        } else {
            lblGPA.setText("0.00");
        }
    }
    
    private void loadCGPA() {
        double cgpa = SummaryDAO.calculateCGPA(registrationNo);
        lblCGPA.setText(String.format("%.2f", cgpa));
        
        // Load summary table
        summaryModel.setRowCount(0);
        List<ResultSummary> summaries = SummaryDAO.getStudentSummaries(registrationNo);
        for (ResultSummary summary : summaries) {
            summaryModel.addRow(new Object[]{
                summary.getSemester(),
                summary.getExamYear(),
                String.format("%.2f", summary.getTotalMarks()),
                String.format("%.2f", summary.getObtainedMarks()),
                String.format("%.2f%%", summary.getPercentage()),
                String.format("%.2f", summary.getGpa()),
                summary.getResult()
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnView) {
            loadAllResults();
        } else if (e.getSource() == btnCancel) {
            setVisible(false);
        }
    }
}

