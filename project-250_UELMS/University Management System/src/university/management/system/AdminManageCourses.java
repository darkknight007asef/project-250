package university.management.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Admin interface for managing department courses
 */
public class AdminManageCourses extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfCourseCode, tfCourseName, tfCredit;
    private JComboBox<String> cbDepartment, cbSemester, cbType;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh, btnCancel;
    
    public AdminManageCourses() {
        setTitle("Manage Department Courses");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Heading
        JLabel heading = new JLabel("Manage Department Courses");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1100, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);
        
        // Form panel
        JPanel formPanel = UITheme.cardPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBounds(20, 50, 500, 280);
        formPanel.setBorder(BorderFactory.createTitledBorder("Course Information"));
        
        // Department
        JLabel lblDept = new JLabel("Department:");
        UITheme.styleLabel(lblDept);
        formPanel.add(lblDept);
        cbDepartment = new JComboBox<>(new String[]{
            "CSE", "EEE", "SWE", "MATH", "PHY", "CHE", "GEO", "GE", "BMB", 
            "CEP", "ME", "CE", "FET", "BAN", "ENG", "ANP", "PAD", "SOC"
        });
        cbDepartment.setBackground(Color.WHITE);
        cbDepartment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cbDepartment);
        
        // Semester
        JLabel lblSem = new JLabel("Semester:");
        UITheme.styleLabel(lblSem);
        formPanel.add(lblSem);
        cbSemester = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        cbSemester.setBackground(Color.WHITE);
        cbSemester.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cbSemester);
        
        // Course Code
        JLabel lblCode = new JLabel("Course Code:");
        UITheme.styleLabel(lblCode);
        formPanel.add(lblCode);
        tfCourseCode = new JTextField();
        UITheme.styleField(tfCourseCode);
        formPanel.add(tfCourseCode);
        
        // Course Name
        JLabel lblName = new JLabel("Course Name:");
        UITheme.styleLabel(lblName);
        formPanel.add(lblName);
        tfCourseName = new JTextField();
        UITheme.styleField(tfCourseName);
        formPanel.add(tfCourseName);
        
        // Credit
        JLabel lblCredit = new JLabel("Credit:");
        UITheme.styleLabel(lblCredit);
        formPanel.add(lblCredit);
        tfCredit = new JTextField();
        UITheme.styleField(tfCredit);
        formPanel.add(tfCredit);
        
        // Type
        JLabel lblType = new JLabel("Type:");
        UITheme.styleLabel(lblType);
        formPanel.add(lblType);
        cbType = new JComboBox<>(new String[]{"Theory", "Lab"});
        cbType.setBackground(Color.WHITE);
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cbType);
        
        add(formPanel);
        
        // Buttons
        btnAdd = new JButton("Add Course");
        UITheme.stylePrimary(btnAdd);
        btnAdd.setBounds(20, 350, 120, 36);
        btnAdd.addActionListener(this);
        add(btnAdd);
        
        btnUpdate = new JButton("Update");
        UITheme.stylePrimary(btnUpdate);
        btnUpdate.setBounds(150, 350, 100, 36);
        btnUpdate.addActionListener(this);
        add(btnUpdate);
        
        btnDelete = new JButton("Delete");
        UITheme.styleDanger(btnDelete);
        btnDelete.setBounds(260, 350, 100, 36);
        btnDelete.addActionListener(this);
        add(btnDelete);
        
        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(370, 350, 100, 36);
        btnRefresh.addActionListener(this);
        add(btnRefresh);
        
        btnCancel = new JButton("Close");
        UITheme.styleGhost(btnCancel);
        btnCancel.setBounds(480, 350, 100, 36);
        btnCancel.addActionListener(this);
        add(btnCancel);
        
        // Table
        String[] columns = {"Department", "Semester", "Course Code", "Course Name", "Credit", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    cbDepartment.setSelectedItem(tableModel.getValueAt(row, 0));
                    cbSemester.setSelectedItem(tableModel.getValueAt(row, 1).toString());
                    tfCourseCode.setText(tableModel.getValueAt(row, 2).toString());
                    tfCourseName.setText(tableModel.getValueAt(row, 3).toString());
                    tfCredit.setText(tableModel.getValueAt(row, 4).toString());
                    cbType.setSelectedItem(tableModel.getValueAt(row, 5));
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleScroll(scrollPane);
        scrollPane.setBounds(20, 400, 1060, 250);
        add(scrollPane);
        
        loadCourses();
        setVisible(true);
    }
    
    private void loadCourses() {
        try {
            tableModel.setRowCount(0);
            Conn c = new Conn();
            String query = "SELECT dept, sem, course_code, course_name, credit, type " +
                          "FROM department_courses ORDER BY dept, sem, course_code";
            ResultSet rs = c.s.executeQuery(query);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("dept"),
                    rs.getInt("sem"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getDouble("credit"),
                    rs.getString("type")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateFields() {
        if (tfCourseCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter course code");
            return false;
        }
        if (tfCourseName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter course name");
            return false;
        }
        if (tfCredit.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter credit");
            return false;
        }
        try {
            double credit = Double.parseDouble(tfCredit.getText().trim());
            if (credit <= 0) {
                JOptionPane.showMessageDialog(this, "Credit must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credit must be a valid number");
            return false;
        }
        return true;
    }
    
    private void addCourse() {
        if (!validateFields()) return;
        
        try {
            Conn c = new Conn();
            String dept = (String) cbDepartment.getSelectedItem();
            int sem = Integer.parseInt((String) cbSemester.getSelectedItem());
            String courseCode = tfCourseCode.getText().trim().toUpperCase();
            String courseName = tfCourseName.getText().trim();
            double credit = Double.parseDouble(tfCredit.getText().trim());
            String type = (String) cbType.getSelectedItem();
            
            // Check if course code already exists (since it's primary key, it must be unique globally)
            // But we'll also check for dept+sem combination to provide better error message
            String checkQuery = "SELECT COUNT(*) FROM department_courses WHERE course_code = '" + courseCode + "'";
            ResultSet rs = c.s.executeQuery(checkQuery);
            if (rs.next() && rs.getInt(1) > 0) {
                // Check if it's the same dept and sem
                String deptSemQuery = "SELECT dept, sem FROM department_courses WHERE course_code = '" + courseCode + "'";
                ResultSet deptSemRs = c.s.executeQuery(deptSemQuery);
                if (deptSemRs.next()) {
                    String existingDept = deptSemRs.getString("dept");
                    int existingSem = deptSemRs.getInt("sem");
                    if (existingDept.equals(dept) && existingSem == sem) {
                        JOptionPane.showMessageDialog(this, 
                            "Course code already exists for this department and semester!");
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Course code already exists for " + existingDept + " Semester " + existingSem + 
                            "!\nPlease use a unique course code.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Course code already exists!");
                }
                return;
            }
            
            String insertQuery = "INSERT INTO department_courses " +
                               "(dept, sem, course_code, course_name, credit, type) " +
                               "VALUES ('" + dept + "', " + sem + ", '" + courseCode + 
                               "', '" + courseName + "', " + credit + ", '" + type + "')";
            
            c.s.executeUpdate(insertQuery);
            JOptionPane.showMessageDialog(this, "Course added successfully!");
            loadCourses();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateCourse() {
        if (!validateFields() || tfCourseCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course to update");
            return;
        }
        
        try {
            Conn c = new Conn();
            String dept = (String) cbDepartment.getSelectedItem();
            int sem = Integer.parseInt((String) cbSemester.getSelectedItem());
            String courseCode = tfCourseCode.getText().trim().toUpperCase();
            String courseName = tfCourseName.getText().trim();
            double credit = Double.parseDouble(tfCredit.getText().trim());
            String type = (String) cbType.getSelectedItem();
            
            // Note: course_code is the primary key, so we need to handle updates carefully
            // For now, we'll update by course_code (assuming it's unique per dept+sem)
            String updateQuery = "UPDATE department_courses SET " +
                               "dept = '" + dept + "', sem = " + sem + 
                               ", course_name = '" + courseName + 
                               "', credit = " + credit + ", type = '" + type + "' " +
                               "WHERE course_code = '" + courseCode + "'";
            
            int rowsUpdated = c.s.executeUpdate(updateQuery);
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Course updated successfully!");
                loadCourses();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Course not found or update failed");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteCourse() {
        if (tfCourseCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this course?\n" +
            "This will also delete all related student marks!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            Conn c = new Conn();
            String courseCode = tfCourseCode.getText().trim();
            String deleteQuery = "DELETE FROM department_courses WHERE course_code = '" + courseCode + "'";
            
            int rowsDeleted = c.s.executeUpdate(deleteQuery);
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Course deleted successfully!");
                loadCourses();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Course not found");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearFields() {
        tfCourseCode.setText("");
        tfCourseName.setText("");
        tfCredit.setText("");
        cbDepartment.setSelectedIndex(0);
        cbSemester.setSelectedIndex(0);
        cbType.setSelectedIndex(0);
        table.clearSelection();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            addCourse();
        } else if (e.getSource() == btnUpdate) {
            updateCourse();
        } else if (e.getSource() == btnDelete) {
            deleteCourse();
        } else if (e.getSource() == btnRefresh) {
            loadCourses();
            clearFields();
        } else if (e.getSource() == btnCancel) {
            setVisible(false);
        }
    }
    
    public static void main(String[] args) {
        new AdminManageCourses();
    }
}

