package university.management.system;

import university.management.system.dao.RecheckDAO;
import university.management.system.models.RecheckRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TeacherRecheckReview extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel model;

    private JButton btnRefresh;
    private JButton btnNoChange;
    private JButton btnPropose;
    private JButton btnClose;

    public TeacherRecheckReview() {
        setTitle("Recheck Requests (Teacher)");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Recheck Requests - Teacher Review", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1100, 30);
        add(heading);

        String[] cols = {"Request ID", "Student", "Subject", "Semester", "Year", "Type", "Reason", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        // Hide request id column
        try {
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setPreferredWidth(0);
        } catch (Exception ignore) {}

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 60, 1040, 420);
        add(sp);

        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(20, 500, 120, 36);
        btnRefresh.addActionListener(this);
        add(btnRefresh);

        btnNoChange = new JButton("No Change");
        UITheme.stylePrimary(btnNoChange);
        btnNoChange.setBounds(150, 500, 140, 36);
        btnNoChange.addActionListener(this);
        add(btnNoChange);

        btnPropose = new JButton("Propose New Marks");
        UITheme.stylePrimary(btnPropose);
        btnPropose.setBounds(300, 500, 180, 36);
        btnPropose.addActionListener(this);
        add(btnPropose);

        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.setBounds(490, 500, 120, 36);
        btnClose.addActionListener(this);
        add(btnClose);

        load();
        setVisible(true);
    }

    private void load() {
        model.setRowCount(0);
        List<RecheckRequest> list = RecheckDAO.getRequestsByStatuses(RecheckDAO.Status.FORWARDED_TO_TEACHER);
        for (RecheckRequest r : list) {
            model.addRow(new Object[]{
                    r.getRequestId(),
                    r.getRegistrationNo(),
                    r.getSubjectCode(),
                    r.getSemester(),
                    r.getExamYear(),
                    r.getRequestType(),
                    r.getReason(),
                    r.getStatus()
            });
        }
    }

    private Integer selectedRequestId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = table.getValueAt(row, 0);
        if (v == null) return null;
        return Integer.parseInt(v.toString());
    }

    private void doNoChange() {
        Integer id = selectedRequestId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }

        String comment = JOptionPane.showInputDialog(this, "Teacher comment (optional):");
        boolean ok = RecheckDAO.teacherNoChange(id, comment);
        JOptionPane.showMessageDialog(this, ok ? "Saved." : "Failed.");
        if (ok) load();
    }

    private void doPropose() {
        Integer id = selectedRequestId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }

        RecheckRequest req = RecheckDAO.getById(id);
        if (req == null) {
            JOptionPane.showMessageDialog(this, "Request not found.");
            return;
        }

        String sMarks = JOptionPane.showInputDialog(this, "Enter proposed marks:");
        if (sMarks == null) return;
        sMarks = sMarks.trim();
        if (sMarks.isEmpty()) return;

        double marks;
        try {
            marks = Double.parseDouble(sMarks);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
            return;
        }

        double fullMarks = RecheckDAO.getFullMarksForSubject(req.getSubjectCode());
        String comment = JOptionPane.showInputDialog(this, "Teacher comment (optional):");

        boolean ok = RecheckDAO.teacherProposeMarks(id, marks, fullMarks, comment);
        JOptionPane.showMessageDialog(this, ok ? "Proposed change saved (needs admin final approval)." : "Failed.");
        if (ok) load();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            load();
        } else if (e.getSource() == btnNoChange) {
            doNoChange();
        } else if (e.getSource() == btnPropose) {
            doPropose();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
            dispose();
        }
    }
}
