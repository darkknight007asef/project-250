package university.management.system;
import university.management.system.charts.GradeDistributionChart;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AdminGradeDistribution extends JFrame implements ActionListener {
    private JTextField tfReg;
    private JComboBox<String> cbDept;
    private JComboBox<Integer> cbSemester, cbYear;
    private JButton btnView, btnClose;
    private JPanel chartsPanel;
    private GradeDistributionChart pieChart;
    private GradeDistributionChart barChart;
    private JTable resultHistoryTable;
    private JTable topStudentsTable;

    public AdminGradeDistribution() {
        setTitle("Result Analytics & Grade Distribution");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        UITheme.applyFrame(this);
        setLayout(new BorderLayout());

        // Header and Filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
        topPanel.setOpaque(false);

        topPanel.add(new JLabel("Reg No:"));
        tfReg = new JTextField();
        tfReg.setPreferredSize(new Dimension(160, 28));
        topPanel.add(tfReg);

        topPanel.add(new JLabel("Department:"));
        cbDept = new JComboBox<>();
        cbDept.setPreferredSize(new Dimension(120, 28));
        cbDept.addItem("All");
        loadDepartments();
        topPanel.add(cbDept);
        
        topPanel.add(new JLabel("Semester:"));
        cbSemester = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        topPanel.add(cbSemester);
        
        topPanel.add(new JLabel("Year:"));
        cbYear = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = currentYear; i >= currentYear-5; i--) cbYear.addItem(i);
        topPanel.add(cbYear);
        
        btnView = new JButton("Analyze");
        UITheme.stylePrimary(btnView);
        btnView.addActionListener(this);
        topPanel.add(btnView);

        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.addActionListener(this);
        topPanel.add(btnClose);
        
        add(topPanel, BorderLayout.NORTH);

        // Main Content (Charts + Result History)
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        pieChart = new GradeDistributionChart();
        pieChart.setBorder(BorderFactory.createTitledBorder("Grade Distribution (Pie)"));
        pieChart.setChartType("PIE");

        barChart = new GradeDistributionChart();
        barChart.setBorder(BorderFactory.createTitledBorder("Grade Distribution (Bar)"));
        barChart.setChartType("BAR");

        chartsPanel.add(pieChart);
        chartsPanel.add(barChart);
        center.add(chartsPanel, BorderLayout.NORTH);

        // Result history table (student mode)
        resultHistoryTable = new JTable(new DefaultTableModel(
                new String[]{"Semester", "Year", "Course Code", "Grade", "Grade Point", "Status"}, 0));
        UITheme.styleTable(resultHistoryTable);
        JScrollPane histScroll = new JScrollPane(resultHistoryTable);
        histScroll.setBorder(BorderFactory.createTitledBorder("Result History"));
        histScroll.setPreferredSize(new Dimension(1200, 260));
        center.add(histScroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        
        // Top Students (Bottom)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Top Performing Students"));
        bottomPanel.setPreferredSize(new Dimension(1200, 200));
        
        topStudentsTable = new JTable(new DefaultTableModel(new String[]{"Rank", "Reg No", "Name", "Total Marks", "GPA", "Result"}, 0));
        UITheme.styleTable(topStudentsTable);
        bottomPanel.add(new JScrollPane(topStudentsTable));
        
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnView) {
            analyze();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
        }
    }
    
    private void analyze() {
        String reg = tfReg.getText() == null ? "" : tfReg.getText().trim();
        String dept = cbDept.getSelectedItem() == null ? "" : cbDept.getSelectedItem().toString();
        Integer sem = (Integer) cbSemester.getSelectedItem();
        Integer year = (Integer) cbYear.getSelectedItem();

        boolean studentMode = !reg.isEmpty();
        if (studentMode) {
            dept = null;
        } else {
            if ("All".equalsIgnoreCase(dept)) dept = null;
        }

        // Update charts
        pieChart.setRegistrationNo(studentMode ? reg : null);
        pieChart.setDepartment(dept);
        pieChart.setSemester(sem);
        pieChart.setYear(year);
        pieChart.setChartType("PIE");
        pieChart.loadData();

        barChart.setRegistrationNo(studentMode ? reg : null);
        barChart.setDepartment(dept);
        barChart.setSemester(sem);
        barChart.setYear(year);
        barChart.setChartType("BAR");
        barChart.loadData();

        if (studentMode) {
            loadStudentHistory(reg, sem, year);
            clearToppers();
        } else {
            clearHistory();
            loadTopStudents(dept, sem, year);
        }
    }

    private void loadDepartments() {
        try (Conn c = new Conn()) {
            try (java.sql.ResultSet rs = c.s.executeQuery("SELECT DISTINCT branch FROM student ORDER BY branch")) {
                while (rs.next()) {
                    String b = rs.getString(1);
                    if (b != null && !b.trim().isEmpty()) cbDept.addItem(b.trim());
                }
            }
        } catch (Exception ignore) {
        }
    }

    private void loadStudentHistory(String reg, Integer sem, Integer year) {
        DefaultTableModel m = (DefaultTableModel) resultHistoryTable.getModel();
        m.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT semester, exam_year, subject_code, grade, grade_point, status " +
                "FROM student_result WHERE is_approved = TRUE AND registration_no = ?");
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(reg);
        if (sem != null) {
            sql.append(" AND semester = ?");
            params.add(sem);
        }
        if (year != null) {
            sql.append(" AND exam_year = ?");
            params.add(year);
        }
        sql.append(" ORDER BY exam_year DESC, semester DESC, subject_code");

        try (Conn c = new Conn()) {
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        m.addRow(new Object[]{
                                rs.getInt("semester"),
                                rs.getInt("exam_year"),
                                rs.getString("subject_code"),
                                rs.getString("grade"),
                                String.format("%.2f", rs.getDouble("grade_point")),
                                rs.getString("status")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student history: " + ex.getMessage());
        }
    }

    private void loadTopStudents(String dept, Integer sem, Integer year) {
        DefaultTableModel model = (DefaultTableModel) topStudentsTable.getModel();
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT rs.registration_no, s.name, rs.total_marks, rs.gpa, rs.result " +
                "FROM result_summary rs JOIN student s ON rs.registration_no = s.registration_no " +
                "WHERE rs.semester = ? AND rs.exam_year = ?");
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(sem);
        params.add(year);
        if (dept != null && !dept.trim().isEmpty()) {
            sql.append(" AND s.branch = ?");
            params.add(dept);
        }
        sql.append(" ORDER BY rs.gpa DESC, rs.total_marks DESC");

        try (Conn c = new Conn()) {
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    int rank = 1;
                    while (rs.next() && rank <= 10) {
                        model.addRow(new Object[]{
                                rank++,
                                rs.getString("registration_no"),
                                rs.getString("name"),
                                String.format("%.1f", rs.getDouble("total_marks")),
                                String.format("%.2f", rs.getDouble("gpa")),
                                rs.getString("result")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading top students: " + ex.getMessage());
        }
    }

    private void clearHistory() {
        DefaultTableModel m = (DefaultTableModel) resultHistoryTable.getModel();
        m.setRowCount(0);
    }

    private void clearToppers() {
        DefaultTableModel model = (DefaultTableModel) topStudentsTable.getModel();
        model.setRowCount(0);
    }
}
