package university.management.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewMarks extends JFrame implements ActionListener {

    private JTextField tfReg;
    private JButton btnSearch, btnCancel, btnPrint;
    private JTable table;
    private JComboBox<Integer> cbSemester;
    private JLabel lblFixedReg;
    private String fixedRegNo = null; // when not null, student-only mode

    public ViewMarks() {
        UITheme.applyFrame(this);
        setLayout(null);
        setTitle("View Student Marks");

        JLabel heading = new JLabel("View Student Marks");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 20, 760, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);

        JLabel lblReg = new JLabel("Registration No:");
        UITheme.styleLabel(lblReg);
        lblReg.setBounds(40, 70, 150, 30);
        add(lblReg);

        tfReg = new JTextField();
        UITheme.styleField(tfReg);
        tfReg.setBounds(180, 70, 180, 36);
        add(tfReg);

        btnSearch = new JButton("Search");
        UITheme.stylePrimary(btnSearch);
        btnSearch.setBounds(370, 70, 110, 36);
        btnSearch.addActionListener(this);
        add(btnSearch);

        btnPrint = new JButton("Print");
        UITheme.styleGhost(btnPrint);
        btnPrint.setBounds(490, 70, 100, 36);
        btnPrint.addActionListener(this);
        add(btnPrint);

        btnCancel = new JButton("Cancel");
        UITheme.styleGhost(btnCancel);
        btnCancel.setBounds(600, 70, 100, 36);
        btnCancel.addActionListener(this);
        add(btnCancel);

        // Semester filter (hidden by default; shown in student mode or after first load)
        cbSemester = new JComboBox<>();
        cbSemester.setBounds(40, 120, 120, 32);
        cbSemester.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbSemester.setBackground(Color.WHITE);
        cbSemester.addActionListener(e -> {
            Integer sem = (Integer) cbSemester.getSelectedItem();
            String reg = (fixedRegNo != null) ? fixedRegNo : (tfReg.getText() == null ? "" : tfReg.getText().trim());
            if (reg != null && !reg.isEmpty() && sem != null) {
                loadMarks(reg, sem);
            }
        });
        cbSemester.setVisible(false);
        add(cbSemester);

        lblFixedReg = new JLabel();
        UITheme.styleLabel(lblFixedReg);
        lblFixedReg.setBounds(180, 120, 420, 28);
        lblFixedReg.setVisible(false);
        add(lblFixedReg);

        // Table
        table = new JTable();
        UITheme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        UITheme.styleScroll(sp);
        sp.setBorder(new EmptyBorder(10, 10, 10, 10));
        sp.setBounds(40, 170, 660, 420);
        // Deselect when clicking outside cells
        sp.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                if (row == -1 || col == -1) table.clearSelection();
            }
        });
        applyZebra(table);
        add(sp);

        setSize(760, 650);
        setLocation(420, 120);
        setVisible(true);
    }

    // Constructor for student-only mode
    public ViewMarks(String registrationNo) {
        this();
        this.fixedRegNo = registrationNo;
        // Hide search UI
        tfReg.setVisible(false);
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JLabel) {
                JLabel jl = (JLabel) c;
                if ("Registration No:".equals(jl.getText())) {
                    jl.setVisible(false);
                }
            }
        }
        btnSearch.setVisible(false);

        // Show fixed reg label and semester filter
        lblFixedReg.setText("Registration: " + registrationNo);
        lblFixedReg.setVisible(true);
        cbSemester.setVisible(true);

        loadSemesters(registrationNo);
        Integer first = cbSemester.getItemCount() > 0 ? cbSemester.getItemAt(0) : null;
        loadMarks(registrationNo, first);
    }

    private void applyZebra(JTable t) {
        final Color alt = new Color(248, 248, 252);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : alt);
                return c;
            }
        });
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == btnSearch) {
            String reg = tfReg.getText() == null ? "" : tfReg.getText().trim();
            if (reg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a registration number.");
                return;
            }
            // On admin search, populate semesters and default to first semester
            loadSemesters(reg);
            if (cbSemester.getItemCount() > 0) {
                cbSemester.setVisible(true);
                Integer first = cbSemester.getItemAt(0);
                cbSemester.setSelectedIndex(0);
                loadMarks(reg, first);
            } else {
                // No semesters found, clear table
                loadMarks(reg, null);
            }
        } else if (ae.getSource() == btnPrint) {
            try { table.print(); } catch (Exception ex) { ex.printStackTrace(); }
        } else if (ae.getSource() == btnCancel) {
            setVisible(false);
        }
    }

    private void loadMarks(String reg, Integer semester) {
        String sql = "SELECT DISTINCT sr.semester AS Semester, sr.subject_code AS CourseCode, dc.course_name AS CourseName, " +
                     "dc.credit AS Credit, sr.grade_point AS GradePoint " +
                     "FROM student_result sr " +
                     "LEFT JOIN department_courses dc ON sr.subject_code = dc.course_code AND dc.sem = sr.semester " +
                     "WHERE sr.registration_no = ? AND sr.is_approved = TRUE " +
                     (semester != null ? "AND sr.semester = ? " : "") +
                     "ORDER BY sr.semester, sr.subject_code";
        try {
            Conn c = new Conn();
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, reg);
                if (semester != null) ps.setInt(2, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    // Build a table model manually to avoid external deps
                    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                            new Object[]{"Semester", "Course Code", "Course Name", "Credit", "Grade"}, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) { return false; }
                    };
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        Object[] row = new Object[]{
                                rs.getInt("Semester"),
                                rs.getString("CourseCode"),
                                rs.getString("CourseName"),
                                rs.getObject("Credit"),
                                rs.getObject("GradePoint")
                        };
                        model.addRow(row);
                    }
                    table.setModel(model);
                    if (!any) {
                        JOptionPane.showMessageDialog(this, "No marks found for this selection.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading marks: " + e.getMessage());
        }
    }

    private void loadSemesters(String reg) {
        cbSemester.removeAllItems();
        try {
            Conn c = new Conn();
            String q = "SELECT DISTINCT semester FROM student_result WHERE registration_no = ? AND is_approved = TRUE ORDER BY semester";
            try (PreparedStatement ps = c.c.prepareStatement(q)) {
                ps.setString(1, reg);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cbSemester.addItem(rs.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ViewMarks();
    }
}
