package university.management.system;

import university.management.system.dao.RecheckDAO;
import university.management.system.dao.SummaryDAO;
import university.management.system.models.RecheckRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminRecheckApproval extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbView;

    private JButton btnRefresh;
    private JButton btnForward;
    private JButton btnReject;
    private JButton btnFinalApprove;
    private JButton btnClose;

    public AdminRecheckApproval() {
        setTitle("Recheck Requests (Admin)");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Recheck / Re-evaluation Requests - Admin", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1200, 30);
        add(heading);

        JLabel lbl = new JLabel("View:");
        UITheme.styleLabel(lbl);
        lbl.setBounds(20, 55, 50, 25);
        add(lbl);

        cbView = new JComboBox<>(new String[]{"SUBMITTED", "TEACHER_REVIEWED"});
        cbView.setBounds(80, 55, 180, 28);
        add(cbView);

        btnRefresh = new JButton("Refresh");
        UITheme.styleGhost(btnRefresh);
        btnRefresh.setBounds(270, 55, 120, 28);
        btnRefresh.addActionListener(this);
        add(btnRefresh);

        String[] cols = {"Request ID", "Student", "Subject", "Sem", "Year", "Type", "Status", "Reason", "Teacher No Change", "Proposed Marks", "Proposed Grade", "Admin Comment", "Teacher Comment"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        try {
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setPreferredWidth(0);
        } catch (Exception ignore) {}

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 100, 1140, 430);
        add(sp);

        btnForward = new JButton("Forward to Teacher");
        UITheme.stylePrimary(btnForward);
        btnForward.setBounds(20, 550, 170, 36);
        btnForward.addActionListener(this);
        add(btnForward);

        btnReject = new JButton("Reject");
        UITheme.styleGhost(btnReject);
        btnReject.setBounds(200, 550, 120, 36);
        btnReject.addActionListener(this);
        add(btnReject);

        btnFinalApprove = new JButton("Final Approve & Apply");
        UITheme.stylePrimary(btnFinalApprove);
        btnFinalApprove.setBounds(330, 550, 190, 36);
        btnFinalApprove.addActionListener(this);
        add(btnFinalApprove);

        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.setBounds(530, 550, 120, 36);
        btnClose.addActionListener(this);
        add(btnClose);

        load();
        setVisible(true);
    }

    private void load() {
        model.setRowCount(0);
        String view = (String) cbView.getSelectedItem();
        if ("TEACHER_REVIEWED".equals(view)) {
            List<RecheckRequest> list = RecheckDAO.getRequestsByStatuses(RecheckDAO.Status.TEACHER_REVIEWED);
            for (RecheckRequest r : list) {
                model.addRow(new Object[]{
                        r.getRequestId(), r.getRegistrationNo(), r.getSubjectCode(), r.getSemester(), r.getExamYear(),
                        r.getRequestType(), r.getStatus(), r.getReason(),
                        r.isTeacherNoChange(), r.getProposedMarks(), r.getProposedGrade(), r.getAdminComment(), r.getTeacherComment()
                });
            }
        } else {
            List<RecheckRequest> list = RecheckDAO.getRequestsByStatuses(RecheckDAO.Status.SUBMITTED);
            for (RecheckRequest r : list) {
                model.addRow(new Object[]{
                        r.getRequestId(), r.getRegistrationNo(), r.getSubjectCode(), r.getSemester(), r.getExamYear(),
                        r.getRequestType(), r.getStatus(), r.getReason(),
                        r.isTeacherNoChange(), r.getProposedMarks(), r.getProposedGrade(), r.getAdminComment(), r.getTeacherComment()
                });
            }
        }
    }

    private Integer selectedRequestId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = table.getValueAt(row, 0);
        if (v == null) return null;
        return Integer.parseInt(v.toString());
    }

    private void forwardToTeacher() {
        Integer id = selectedRequestId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request.");
            return;
        }
        String comment = JOptionPane.showInputDialog(this, "Admin comment (optional):");
        boolean ok = RecheckDAO.adminForwardToTeacher(id, comment);
        JOptionPane.showMessageDialog(this, ok ? "Forwarded." : "Failed.");
        if (ok) load();
    }

    private void reject() {
        Integer id = selectedRequestId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request.");
            return;
        }
        String comment = JOptionPane.showInputDialog(this, "Reason for rejection:");
        boolean ok = RecheckDAO.adminReject(id, comment);
        JOptionPane.showMessageDialog(this, ok ? "Rejected." : "Failed.");
        if (ok) load();
    }

    private void finalApprove() {
        Integer id = selectedRequestId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a request.");
            return;
        }
        RecheckRequest req = RecheckDAO.getById(id);
        if (req == null) {
            JOptionPane.showMessageDialog(this, "Request not found.");
            return;
        }
        if (!RecheckDAO.Status.TEACHER_REVIEWED.equals(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "This request is not ready for final approval. Switch view to TEACHER_REVIEWED.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Final approve and apply the teacher decision to student_result?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String adminComment = JOptionPane.showInputDialog(this, "Admin final approval comment (optional):");

        try {
            boolean ok = RecheckDAO.adminFinalApproveApplyToResult(req, adminComment);
            if (ok) {
                SummaryDAO.calculateAndSaveSummary(req.getRegistrationNo(), req.getSemester(), req.getExamYear());
                JOptionPane.showMessageDialog(this, "Applied and approved. Result summary recalculated.");
                load();
            } else {
                JOptionPane.showMessageDialog(this, "Failed.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            load();
        } else if (e.getSource() == btnForward) {
            forwardToTeacher();
        } else if (e.getSource() == btnReject) {
            reject();
        } else if (e.getSource() == btnFinalApprove) {
            finalApprove();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
            dispose();
        }
    }
}
