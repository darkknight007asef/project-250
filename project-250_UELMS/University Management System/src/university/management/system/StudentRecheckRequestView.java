package university.management.system;

import university.management.system.dao.RecheckDAO;
import university.management.system.models.StudentResult;
import university.management.system.dao.ResultDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StudentRecheckRequestView extends JFrame implements ActionListener {
    private final String registrationNo;

    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbType;
    private JTextField tfReason;

    private JButton btnSubmit;
    private JButton btnRefresh;
    private JButton btnClose;

    public StudentRecheckRequestView(String registrationNo) {
        this.registrationNo = registrationNo;

        setTitle("Recheck / Re-evaluation Request");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Request Recheck / Re-evaluation", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1000, 30);
        add(heading);

        JLabel lblType = new JLabel("Request Type:");
        UITheme.styleLabel(lblType);
        lblType.setBounds(20, 55, 120, 25);
        add(lblType);

        cbType = new JComboBox<>(new String[]{"RECOUNT", "REEVALUATION"});
        cbType.setBounds(150, 55, 160, 28);
        add(cbType);

        JLabel lblReason = new JLabel("Reason:");
        UITheme.styleLabel(lblReason);
        lblReason.setBounds(330, 55, 70, 25);
        add(lblReason);

        tfReason = new JTextField();
        UITheme.styleField(tfReason);
        tfReason.setBounds(400, 55, 380, 28);
        add(tfReason);

        btnSubmit = new JButton("Submit Request");
        UITheme.stylePrimary(btnSubmit);
        btnSubmit.setBounds(800, 55, 160, 28);
        btnSubmit.addActionListener(this);
        add(btnSubmit);

        String[] cols = {"Subject Code", "Marks", "Grade", "Grade Point", "Status", "Semester", "Year", "Exam Type"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 110, 940, 380);
        add(sp);

        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(20, 510, 120, 36);
        btnRefresh.addActionListener(this);
        add(btnRefresh);

        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.setBounds(150, 510, 120, 36);
        btnClose.addActionListener(this);
        add(btnClose);

        loadApprovedResults();
        setVisible(true);
    }

    private void loadApprovedResults() {
        model.setRowCount(0);
        List<StudentResult> results = ResultDAO.getStudentResults(registrationNo, null, null);
        for (StudentResult r : results) {
            if (!r.isApproved()) continue;
            model.addRow(new Object[]{
                    r.getSubjectCode(),
                    String.format("%.2f", r.getMarksObtained()),
                    r.getGrade(),
                    String.format("%.2f", r.getGradePoint()),
                    r.getStatus(),
                    r.getSemester(),
                    r.getExamYear(),
                    r.getExamType()
            });
        }
    }

    private void submitSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a result row to request recheck for.");
            return;
        }

        String subject = String.valueOf(model.getValueAt(row, 0));
        int semester = Integer.parseInt(String.valueOf(model.getValueAt(row, 5)));
        int year = Integer.parseInt(String.valueOf(model.getValueAt(row, 6)));

        String type = (String) cbType.getSelectedItem();
        String reason = tfReason.getText() == null ? "" : tfReason.getText().trim();

        if (reason.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Submit without a reason?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        boolean ok = RecheckDAO.createRequest(registrationNo, subject, semester, year, type, reason);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Request submitted. Admin will review it.");
            tfReason.setText("");
        } else {
            String err = RecheckDAO.getLastError();
            if (err == null || err.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Failed to submit request.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit request: " + err);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSubmit) {
            submitSelected();
        } else if (e.getSource() == btnRefresh) {
            loadApprovedResults();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
            dispose();
        }
    }
}
