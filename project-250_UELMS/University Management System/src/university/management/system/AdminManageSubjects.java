package university.management.system;

import university.management.system.dao.SubjectDAO;
import university.management.system.models.Subject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Admin interface for managing subjects
 */
public class AdminManageSubjects extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfCode, tfName, tfSemester, tfDept, tfFullMarks, tfPassMarks;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh, btnCancel;
    private JComboBox<String> cbDepartment;
    
    public AdminManageSubjects() {
        setTitle("Manage Subjects");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Heading
        JLabel heading = new JLabel("Manage Subjects");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1000, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);
        
        // Form panel
        JPanel formPanel = UITheme.cardPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBounds(20, 50, 450, 280);
        formPanel.setBorder(BorderFactory.createTitledBorder("Subject Information"));
        
        // Subject Code
        JLabel lblCode = new JLabel("Subject Code:");
        UITheme.styleLabel(lblCode);
        formPanel.add(lblCode);
        tfCode = new JTextField();
        UITheme.styleField(tfCode);
        formPanel.add(tfCode);
        
        // Subject Name
        JLabel lblName = new JLabel("Subject Name:");
        UITheme.styleLabel(lblName);
        formPanel.add(lblName);
        tfName = new JTextField();
        UITheme.styleField(tfName);
        formPanel.add(tfName);
        
        // Semester
        JLabel lblSem = new JLabel("Semester:");
        UITheme.styleLabel(lblSem);
        formPanel.add(lblSem);
        tfSemester = new JTextField();
        UITheme.styleField(tfSemester);
        formPanel.add(tfSemester);
        
        // Department
        JLabel lblDept = new JLabel("Department:");
        UITheme.styleLabel(lblDept);
        formPanel.add(lblDept);
        cbDepartment = new JComboBox<>(new String[]{"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"});
        cbDepartment.setBackground(Color.WHITE);
        cbDepartment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cbDepartment);
        
        // Full Marks
        JLabel lblFull = new JLabel("Full Marks:");
        UITheme.styleLabel(lblFull);
        formPanel.add(lblFull);
        tfFullMarks = new JTextField("100");
        UITheme.styleField(tfFullMarks);
        formPanel.add(tfFullMarks);
        
        // Pass Marks
        JLabel lblPass = new JLabel("Pass Marks:");
        UITheme.styleLabel(lblPass);
        formPanel.add(lblPass);
        tfPassMarks = new JTextField("40");
        UITheme.styleField(tfPassMarks);
        formPanel.add(tfPassMarks);
        
        add(formPanel);
        
        // Buttons
        btnAdd = new JButton("Add");
        UITheme.stylePrimary(btnAdd);
        btnAdd.setBounds(20, 340, 100, 36);
        btnAdd.addActionListener(this);
        add(btnAdd);
        
        btnUpdate = new JButton("Update");
        UITheme.stylePrimary(btnUpdate);
        btnUpdate.setBounds(130, 340, 100, 36);
        btnUpdate.addActionListener(this);
        add(btnUpdate);
        
        btnDelete = new JButton("Delete");
        UITheme.styleDanger(btnDelete);
        btnDelete.setBounds(240, 340, 100, 36);
        btnDelete.addActionListener(this);
        add(btnDelete);
        
        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(350, 340, 100, 36);
        btnRefresh.addActionListener(this);
        add(btnRefresh);
        
        btnCancel = new JButton("Cancel");
        UITheme.styleGhost(btnCancel);
        btnCancel.setBounds(20, 390, 100, 36);
        btnCancel.addActionListener(this);
        add(btnCancel);
        
        // Table
        String[] columns = {"Subject Code", "Subject Name", "Semester", "Department", "Full Marks", "Pass Marks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedSubject();
            }
        });
        UITheme.styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(490, 50, 480, 540);
        add(scrollPane);
        
        loadSubjects();
        setVisible(true);
    }
    
    private void loadSubjects() {
        tableModel.setRowCount(0);
        List<Subject> subjects = SubjectDAO.getAllSubjects();
        for (Subject subject : subjects) {
            tableModel.addRow(new Object[]{
                subject.getSubjectCode(),
                subject.getSubjectName(),
                subject.getSemester(),
                subject.getDepartment(),
                subject.getFullMarks(),
                subject.getPassMarks()
            });
        }
    }
    
    private void loadSelectedSubject() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            tfCode.setText(table.getValueAt(selectedRow, 0).toString());
            tfName.setText(table.getValueAt(selectedRow, 1).toString());
            tfSemester.setText(table.getValueAt(selectedRow, 2).toString());
            cbDepartment.setSelectedItem(table.getValueAt(selectedRow, 3).toString());
            tfFullMarks.setText(table.getValueAt(selectedRow, 4).toString());
            tfPassMarks.setText(table.getValueAt(selectedRow, 5).toString());
        }
    }
    
    private void clearFields() {
        tfCode.setText("");
        tfName.setText("");
        tfSemester.setText("");
        cbDepartment.setSelectedIndex(0);
        tfFullMarks.setText("100");
        tfPassMarks.setText("40");
        table.clearSelection();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            addSubject();
        } else if (e.getSource() == btnUpdate) {
            updateSubject();
        } else if (e.getSource() == btnDelete) {
            deleteSubject();
        } else if (e.getSource() == btnRefresh) {
            loadSubjects();
            clearFields();
        } else if (e.getSource() == btnCancel) {
            setVisible(false);
        }
    }
    
    private void addSubject() {
        if (!validateFields()) return;
        
        Subject subject = new Subject();
        subject.setSubjectCode(tfCode.getText().trim());
        subject.setSubjectName(tfName.getText().trim());
        subject.setSemester(Integer.parseInt(tfSemester.getText().trim()));
        subject.setDepartment(cbDepartment.getSelectedItem().toString());
        subject.setFullMarks(Integer.parseInt(tfFullMarks.getText().trim()));
        subject.setPassMarks(Integer.parseInt(tfPassMarks.getText().trim()));
        
        if (SubjectDAO.addSubject(subject)) {
            JOptionPane.showMessageDialog(this, "Subject added successfully!");
            loadSubjects();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add subject. Subject code may already exist.");
        }
    }
    
    private void updateSubject() {
        if (!validateFields() || tfCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a subject to update.");
            return;
        }
        
        Subject subject = new Subject();
        subject.setSubjectCode(tfCode.getText().trim());
        subject.setSubjectName(tfName.getText().trim());
        subject.setSemester(Integer.parseInt(tfSemester.getText().trim()));
        subject.setDepartment(cbDepartment.getSelectedItem().toString());
        subject.setFullMarks(Integer.parseInt(tfFullMarks.getText().trim()));
        subject.setPassMarks(Integer.parseInt(tfPassMarks.getText().trim()));
        
        if (SubjectDAO.updateSubject(subject)) {
            JOptionPane.showMessageDialog(this, "Subject updated successfully!");
            loadSubjects();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update subject.");
        }
    }
    
    private void deleteSubject() {
        String subjectCode = tfCode.getText().trim();
        if (subjectCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete subject: " + subjectCode + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (SubjectDAO.deleteSubject(subjectCode)) {
                JOptionPane.showMessageDialog(this, "Subject deleted successfully!");
                loadSubjects();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete subject.");
            }
        }
    }
    
    private boolean validateFields() {
        if (tfCode.getText().trim().isEmpty() || tfName.getText().trim().isEmpty() ||
            tfSemester.getText().trim().isEmpty() || tfFullMarks.getText().trim().isEmpty() ||
            tfPassMarks.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return false;
        }
        
        try {
            int semester = Integer.parseInt(tfSemester.getText().trim());
            if (semester < 1 || semester > 8) {
                JOptionPane.showMessageDialog(this, "Semester must be between 1 and 8.");
                return false;
            }
            
            int fullMarks = Integer.parseInt(tfFullMarks.getText().trim());
            int passMarks = Integer.parseInt(tfPassMarks.getText().trim());
            
            if (fullMarks <= 0) {
                JOptionPane.showMessageDialog(this, "Full marks must be greater than 0.");
                return false;
            }
            
            if (passMarks < 0 || passMarks > fullMarks) {
                JOptionPane.showMessageDialog(this, "Pass marks must be between 0 and full marks.");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for semester, full marks, and pass marks.");
            return false;
        }
        
        return true;
    }
}

