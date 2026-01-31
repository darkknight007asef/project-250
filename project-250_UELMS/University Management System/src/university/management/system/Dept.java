package university.management.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dept extends JFrame implements ActionListener {

    JTable creditTable, courseTable;
    JComboBox<String> deptFilter;
    JButton filterBtn;
    DefaultTableModel creditModel, courseModel;

    public Dept() {
        // Ensure database tables are initialized
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setTitle("Department Information");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        UITheme.applyFrame(this);

        // -------------------- HEADING --------------------
        JLabel heading = new JLabel("Department Information", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        add(heading, BorderLayout.NORTH);

        // -------------------- TOP CONTROLS --------------------
        JPanel topPanel = UITheme.cardPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setOpaque(false);

        JLabel lbl = new JLabel("Select Department:");
        UITheme.styleLabel(lbl);
        deptFilter = new JComboBox<>();
        deptFilter.setBackground(Color.WHITE);
        deptFilter.setForeground(Color.BLACK);
        deptFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deptFilter.setBorder(BorderFactory.createLineBorder(new Color(230,232,240), 1, true));
        deptFilter.setPreferredSize(new Dimension(180, 36));
        loadDepartments();

        filterBtn = new JButton("Filter Dept");
        UITheme.stylePrimary(filterBtn);
        filterBtn.addActionListener(this);

        topPanel.add(lbl);
        topPanel.add(deptFilter);
        topPanel.add(filterBtn);

        add(topPanel, BorderLayout.PAGE_START);

        // -------------------- CREDIT TABLE --------------------
        creditModel = new DefaultTableModel();
        creditModel.setColumnIdentifiers(new String[]{
                "Dept", "Total Credit",
                "Sem1", "Sem2", "Sem3", "Sem4",
                "Sem5", "Sem6", "Sem7", "Sem8"
        });

        creditTable = new JTable(creditModel);
        UITheme.styleTable(creditTable);
        creditTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        creditTable.setRowHeight(28);
        creditTable.setIntercellSpacing(new Dimension(6, 6));
        JScrollPane creditScroll = new JScrollPane(creditTable);
        UITheme.styleScroll(creditScroll);
        creditScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        creditScroll.getViewport().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int r = creditTable.rowAtPoint(p), c = creditTable.columnAtPoint(p);
                if (r == -1 || c == -1) creditTable.clearSelection();
            }
        });
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        JLabel creditTitle = new JLabel("Department Credit Info", SwingConstants.LEFT);
        UITheme.styleLabel(creditTitle);
        creditTitle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        centerWrap.add(creditTitle, BorderLayout.NORTH);
        centerWrap.add(creditScroll, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        // -------------------- COURSE TABLE --------------------
        courseModel = new DefaultTableModel();
        courseModel.setColumnIdentifiers(new String[]{
                "Sem", "Course Code", "Course Name", "Credit", "Type"
        });

        courseTable = new JTable(courseModel);
        UITheme.styleTable(courseTable);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        courseTable.setRowHeight(28);
        courseTable.setIntercellSpacing(new Dimension(6, 6));
        JScrollPane courseScroll = new JScrollPane(courseTable);
        UITheme.styleScroll(courseScroll);
        courseScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        courseScroll.getViewport().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int r = courseTable.rowAtPoint(p), c = courseTable.columnAtPoint(p);
                if (r == -1 || c == -1) courseTable.clearSelection();
            }
        });
        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setOpaque(false);
        JLabel courseTitle = new JLabel("Courses", SwingConstants.LEFT);
        UITheme.styleLabel(courseTitle);
        courseTitle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        southWrap.add(courseTitle, BorderLayout.NORTH);
        southWrap.add(courseScroll, BorderLayout.CENTER);
        southWrap.setPreferredSize(new Dimension(900, 220));
        add(southWrap, BorderLayout.SOUTH);

        // Load credit info for all departments by default
        loadDepartmentCredits();

        setVisible(true);
    }

    private void loadDepartments() {
        try {
            Conn c = new Conn();
            if (c.s == null) {
                JOptionPane.showMessageDialog(null, "Database connection failed!");
                return;
            }
            
            ResultSet rs = c.s.executeQuery("SELECT dept FROM department_credit ORDER BY dept");
            while (rs.next()) {
                deptFilter.addItem(rs.getString("dept"));
            }
        } catch (SQLException e) {
            // Check if it's a "table doesn't exist" error
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                try {
                    // Try to initialize database
                    DatabaseInitializer.initializeDatabase();
                    // Retry the query
                    Conn c = new Conn();
                    if (c.s != null) {
                        ResultSet rs = c.s.executeQuery("SELECT dept FROM department_credit ORDER BY dept");
                        while (rs.next()) {
                            deptFilter.addItem(rs.getString("dept"));
                        }
                    }
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, 
                        "Database tables are missing. Please restart the application.\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error loading departments: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading departments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    private void loadDepartmentCredits() {
        try {
            creditModel.setRowCount(0);
            Conn c = new Conn();
            if (c.s == null) {
                JOptionPane.showMessageDialog(null, "Database connection failed!");
                return;
            }
            
            ResultSet rs = c.s.executeQuery("SELECT * FROM department_credit ORDER BY dept");
            while (rs.next()) {
                creditModel.addRow(new Object[]{
                        rs.getString("dept"),
                        rs.getString("total_credit"),
                        rs.getString("sem1_credit"),
                        rs.getString("sem2_credit"),
                        rs.getString("sem3_credit"),
                        rs.getString("sem4_credit"),
                        rs.getString("sem5_credit"),
                        rs.getString("sem6_credit"),
                        rs.getString("sem7_credit"),
                        rs.getString("sem8_credit")
                });
            }
        } catch (SQLException e) {
            // Check if it's a "table doesn't exist" error
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                try {
                    // Try to initialize database
                    DatabaseInitializer.initializeDatabase();
                    // Retry the query
                    Conn c = new Conn();
                    if (c.s != null) {
                        ResultSet rs = c.s.executeQuery("SELECT * FROM department_credit ORDER BY dept");
                        while (rs.next()) {
                            creditModel.addRow(new Object[]{
                                    rs.getString("dept"),
                                    rs.getString("total_credit"),
                                    rs.getString("sem1_credit"),
                                    rs.getString("sem2_credit"),
                                    rs.getString("sem3_credit"),
                                    rs.getString("sem4_credit"),
                                    rs.getString("sem5_credit"),
                                    rs.getString("sem6_credit"),
                                    rs.getString("sem7_credit"),
                                    rs.getString("sem8_credit")
                            });
                        }
                    }
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, 
                        "Database tables are missing. Please restart the application.\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error loading credit info: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading credit info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCoursesByDept(String dept) {
        try {
            courseModel.setRowCount(0);
            Conn c = new Conn();
            if (c.s == null) {
                JOptionPane.showMessageDialog(null, "Database connection failed!");
                return;
            }
            
            String query = "SELECT sem, course_code, course_name, credit, type FROM department_courses WHERE dept = '" + dept + "' ORDER BY sem, course_code";
            ResultSet rs = c.s.executeQuery(query);

            while (rs.next()) {
                courseModel.addRow(new Object[]{
                        rs.getInt("sem"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("credit"),
                        rs.getString("type")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == filterBtn) {
            String selectedDept = (String) deptFilter.getSelectedItem();
            if (selectedDept != null) {
                loadCoursesByDept(selectedDept);
            }
        }
    }

    public static void main(String[] args) {
        new Dept();
    }
}