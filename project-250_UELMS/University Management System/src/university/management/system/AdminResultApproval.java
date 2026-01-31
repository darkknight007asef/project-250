package university.management.system;

import university.management.system.dao.ResultDAO;
import university.management.system.dao.SummaryDAO;
import university.management.system.models.StudentResult;
import university.management.system.charts.GradeDistributionChart;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin interface for approving student results
 */
public class AdminResultApproval extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnApprove, btnRefresh, btnCancel, btnViewChart;
    private JCheckBox selectAll;
    private JComboBox<String> cbChartType, cbDept, cbSemester, cbYear;
    private GradeDistributionChart chartPanel;
    private List<StudentResult> currentUnapprovedResults = new ArrayList<>();
    private JLabel lblStatus;
    
    public AdminResultApproval() {
        setTitle("Approve Student Results");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Heading
        JLabel heading = new JLabel("Approve Student Results");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1200, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);
        
        // Select all checkbox
        selectAll = new JCheckBox("Select All");
        selectAll.setBounds(20, 50, 120, 25);
        selectAll.addActionListener(e -> {
            boolean selected = selectAll.isSelected();
            for (int i = 0; i < table.getRowCount(); i++) {
                table.setValueAt(selected, i, 0);
            }
        });
        add(selectAll);
        
        // Table
        String[] columns = {"Select", "Result ID", "Student ID", "Subject Code", "Marks", "Grade", "Status", "Semester", "Year", "Exam Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only select column is editable
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        UITheme.styleTable(table);

        // Hide Result ID column (keep it in the model for robust mapping)
        try {
            table.getColumnModel().getColumn(1).setMinWidth(0);
            table.getColumnModel().getColumn(1).setMaxWidth(0);
            table.getColumnModel().getColumn(1).setPreferredWidth(0);
        } catch (Exception ignore) {
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 80, 1160, 400);
        add(scrollPane);
        
        // Buttons
        btnApprove = new JButton("Approve Selected");
        UITheme.stylePrimary(btnApprove);
        btnApprove.setBounds(20, 500, 150, 36);
        btnApprove.addActionListener(this);
        add(btnApprove);
        
        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(180, 500, 100, 36);
        btnRefresh.addActionListener(this);
        add(btnRefresh);
        
        btnCancel = new JButton("Cancel");
        UITheme.styleGhost(btnCancel);
        btnCancel.setBounds(290, 500, 100, 36);
        btnCancel.addActionListener(this);
        add(btnCancel);

        lblStatus = new JLabel(" ");
        UITheme.styleLabel(lblStatus);
        lblStatus.setBounds(410, 505, 770, 28);
        add(lblStatus);
        
        loadUnapprovedResultsAsync();
        setVisible(true);
    }
    
    private void loadUnapprovedResultsAsync() {
        btnApprove.setEnabled(false);
        btnRefresh.setEnabled(false);
        selectAll.setEnabled(false);
        lblStatus.setText("Loading...");
        tableModel.setRowCount(0);

        SwingWorker<List<StudentResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudentResult> doInBackground() {
                return ResultDAO.getUnapprovedResults();
            }

            @Override
            protected void done() {
                try {
                    currentUnapprovedResults = get();
                    for (StudentResult result : currentUnapprovedResults) {
                        tableModel.addRow(new Object[]{
                                false,
                                String.valueOf(result.getResultId()),
                                result.getRegistrationNo(),
                                result.getSubjectCode(),
                                String.format("%.2f", result.getMarksObtained()),
                                result.getGrade(),
                                result.getStatus(),
                                result.getSemester(),
                                result.getExamYear(),
                                result.getExamType()
                        });
                    }
                    lblStatus.setText("Loaded " + currentUnapprovedResults.size() + " pending result(s)");
                } catch (Exception e) {
                    e.printStackTrace();
                    lblStatus.setText("Failed to load");
                    JOptionPane.showMessageDialog(AdminResultApproval.this, "Failed to load results: " + e.getMessage());
                } finally {
                    btnApprove.setEnabled(true);
                    btnRefresh.setEnabled(true);
                    selectAll.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnApprove) {
            approveSelected();
        } else if (e.getSource() == btnRefresh) {
            loadUnapprovedResultsAsync();
            selectAll.setSelected(false);
        } else if (e.getSource() == btnCancel) {
            setVisible(false);
        }
    }
    
    private void approveSelected() {
        List<Integer> resultIds = new ArrayList<>();

        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            Boolean selected = (Boolean) table.getValueAt(viewRow, 0);
            if (selected != null && selected) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < currentUnapprovedResults.size()) {
                    resultIds.add(currentUnapprovedResults.get(modelRow).getResultId());
                }
            }
        }
        
        if (resultIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one result to approve.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to approve " + resultIds.size() + " result(s)?",
            "Confirm Approval", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        btnApprove.setEnabled(false);
        btnRefresh.setEnabled(false);
        selectAll.setEnabled(false);
        lblStatus.setText("Approving... please wait");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return ResultDAO.approveResultsAndPostProcess(resultIds);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminResultApproval.this, "Results approved successfully!\nStudents will be promoted automatically when a semester is fully approved.");
                        loadUnapprovedResultsAsync();
                        selectAll.setSelected(false);
                    } else {
                        JOptionPane.showMessageDialog(AdminResultApproval.this, "Failed to approve results.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminResultApproval.this, "Failed to approve: " + e.getMessage());
                } finally {
                    btnApprove.setEnabled(true);
                    btnRefresh.setEnabled(true);
                    selectAll.setEnabled(true);
                    lblStatus.setText(" ");
                }
            }
        };
        worker.execute();
    }
}

