package university.management.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminAtRiskStudents extends JFrame implements ActionListener {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbDept;
    private JComboBox<String> cbRisk;
    private JButton btnRefresh;
    private JButton btnClose;
    private JLabel lblStatus;

    private static final class RiskRow {
        String reg;
        String name;
        String dept;
        int currentSem;
        double cgpa;
        Double lastGpa;
        Double prevGpa;
        int failsLastTwo;
        String risk;
        String reasons;
    }

    public AdminAtRiskStudents() {
        setTitle("At-Risk Students");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("At-Risk Students Dashboard", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBounds(0, 10, 1200, 30);
        add(heading);

        JLabel lblDept = new JLabel("Department:");
        UITheme.styleLabel(lblDept);
        lblDept.setBounds(20, 55, 100, 25);
        add(lblDept);

        cbDept = new JComboBox<>();
        cbDept.setBounds(130, 55, 140, 28);
        add(cbDept);

        JLabel lblRisk = new JLabel("Risk:");
        UITheme.styleLabel(lblRisk);
        lblRisk.setBounds(290, 55, 50, 25);
        add(lblRisk);

        cbRisk = new JComboBox<>(new String[]{"ALL", "HIGH", "MEDIUM", "LOW"});
        cbRisk.setBounds(340, 55, 120, 28);
        add(cbRisk);

        btnRefresh = new JButton("Refresh");
        UITheme.stylePrimary(btnRefresh);
        btnRefresh.setBounds(480, 55, 120, 28);
        btnRefresh.addActionListener(this);
        add(btnRefresh);

        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.setBounds(610, 55, 120, 28);
        btnClose.addActionListener(this);
        add(btnClose);

        lblStatus = new JLabel(" ");
        lblStatus.setBounds(750, 55, 410, 28);
        lblStatus.setForeground(UITheme.TEXT_SECONDARY);
        add(lblStatus);

        String[] cols = {"Reg", "Name", "Dept", "Current Sem", "CGPA", "Last GPA", "Prev GPA", "Fails (Last 2)", "Risk", "Reasons"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 100, 1140, 470);
        add(sp);

        loadDeptOptions();
        refreshAsync();

        setVisible(true);
    }

    private void loadDeptOptions() {
        cbDept.removeAllItems();
        cbDept.addItem("ALL");
        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(
                "SELECT DISTINCT dept FROM student_semester ORDER BY dept")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cbDept.addItem(rs.getString("dept"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshAsync() {
        model.setRowCount(0);
        btnRefresh.setEnabled(false);
        cbDept.setEnabled(false);
        cbRisk.setEnabled(false);
        lblStatus.setText("Loading... please wait");

        String deptFilter = (String) cbDept.getSelectedItem();
        if (deptFilter == null) deptFilter = "ALL";
        String riskFilter = (String) cbRisk.getSelectedItem();
        if (riskFilter == null) riskFilter = "ALL";

        final String deptArg = deptFilter;
        final String riskArg = riskFilter;

        SwingWorker<List<RiskRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RiskRow> doInBackground() {
                return fetchRiskRows(deptArg, riskArg);
            }

            @Override
            protected void done() {
                try {
                    List<RiskRow> rows = get();
                    for (RiskRow r : rows) {
                        model.addRow(new Object[]{
                                r.reg, r.name, r.dept, r.currentSem,
                                String.format("%.2f", r.cgpa),
                                r.lastGpa == null ? "" : String.format("%.2f", r.lastGpa),
                                r.prevGpa == null ? "" : String.format("%.2f", r.prevGpa),
                                r.failsLastTwo,
                                r.risk,
                                r.reasons
                        });
                    }
                    lblStatus.setText("Loaded " + rows.size() + " students");
                } catch (Exception e) {
                    e.printStackTrace();
                    lblStatus.setText("Failed to load");
                    JOptionPane.showMessageDialog(AdminAtRiskStudents.this, "Error loading At-Risk: " + e.getMessage());
                } finally {
                    btnRefresh.setEnabled(true);
                    cbDept.setEnabled(true);
                    cbRisk.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private List<RiskRow> fetchRiskRows(String deptFilter, String riskFilter) {
        List<RiskRow> out = new ArrayList<>();

        String sql =
                "WITH ordered AS (" +
                "  SELECT registration_no, gpa, exam_year, semester, " +
                "         ROW_NUMBER() OVER (PARTITION BY registration_no ORDER BY exam_year DESC, semester DESC) AS rn " +
                "  FROM result_summary" +
                "), last_two AS (" +
                "  SELECT registration_no, " +
                "         MAX(CASE WHEN rn=1 THEN gpa END) AS last_gpa, " +
                "         MAX(CASE WHEN rn=2 THEN gpa END) AS prev_gpa, " +
                "         MAX(CASE WHEN rn=1 THEN exam_year END) AS y1, " +
                "         MAX(CASE WHEN rn=1 THEN semester END) AS s1, " +
                "         MAX(CASE WHEN rn=2 THEN exam_year END) AS y2, " +
                "         MAX(CASE WHEN rn=2 THEN semester END) AS s2 " +
                "  FROM ordered GROUP BY registration_no" +
                "), cgpa AS (" +
                "  SELECT registration_no, AVG(gpa) AS cgpa FROM result_summary GROUP BY registration_no" +
                "), fails AS (" +
                "  SELECT sr.registration_no, COUNT(*) AS fails_last_two " +
                "  FROM student_result sr " +
                "  JOIN last_two lt ON lt.registration_no = sr.registration_no " +
                "  WHERE sr.is_approved=TRUE AND sr.status='FAIL' AND (" +
                "        (sr.exam_year = lt.y1 AND sr.semester = lt.s1) OR (sr.exam_year = lt.y2 AND sr.semester = lt.s2)" +
                "  ) GROUP BY sr.registration_no" +
                ") " +
                "SELECT s.registration_no, s.name, ss.dept, ss.current_semester, " +
                "       COALESCE(c.cgpa, 0) AS cgpa, lt.last_gpa, lt.prev_gpa, COALESCE(f.fails_last_two, 0) AS fails_last_two " +
                "FROM student s " +
                "JOIN student_semester ss ON ss.registration_no = s.registration_no " +
                "LEFT JOIN cgpa c ON c.registration_no = s.registration_no " +
                "LEFT JOIN last_two lt ON lt.registration_no = s.registration_no " +
                "LEFT JOIN fails f ON f.registration_no = s.registration_no ";
        if (!"ALL".equals(deptFilter)) {
            sql += "WHERE ss.dept = ? ";
        }
        sql += "ORDER BY ss.dept, s.registration_no";

        try (Conn c = new Conn(); PreparedStatement ps = c.c.prepareStatement(sql)) {
            if (!"ALL".equals(deptFilter)) {
                ps.setString(1, deptFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RiskRow rr = new RiskRow();
                    rr.reg = rs.getString("registration_no");
                    rr.name = rs.getString("name");
                    rr.dept = rs.getString("dept");
                    rr.currentSem = rs.getInt("current_semester");
                    rr.cgpa = rs.getDouble("cgpa");
                    double lg = rs.getDouble("last_gpa");
                    rr.lastGpa = rs.wasNull() ? null : lg;
                    double pg = rs.getDouble("prev_gpa");
                    rr.prevGpa = rs.wasNull() ? null : pg;
                    rr.failsLastTwo = rs.getInt("fails_last_two");

                    double drop = 0.0;
                    if (rr.lastGpa != null && rr.prevGpa != null) {
                        drop = rr.prevGpa - rr.lastGpa;
                    }

                    StringBuilder reasons = new StringBuilder();
                    String risk = "LOW";

                    if (rr.cgpa > 0 && rr.cgpa < 2.50) {
                        risk = "HIGH";
                        reasons.append("CGPA below 2.50; ");
                    } else if (rr.cgpa > 0 && rr.cgpa < 3.00) {
                        risk = "MEDIUM";
                        reasons.append("CGPA below 3.00; ");
                    }

                    if (rr.failsLastTwo >= 2) {
                        risk = "HIGH";
                        reasons.append("2+ fails in last 2 semesters; ");
                    } else if (rr.failsLastTwo == 1 && "LOW".equals(risk)) {
                        risk = "MEDIUM";
                        reasons.append("1 fail in last 2 semesters; ");
                    }

                    if (drop >= 0.50) {
                        if (!"HIGH".equals(risk)) risk = "MEDIUM";
                        reasons.append("GPA drop >= 0.50; ");
                    }

                    if (reasons.length() == 0) {
                        reasons.append("No major risk signals.");
                    }

                    rr.risk = risk;
                    rr.reasons = reasons.toString();

                    if (!"ALL".equals(riskFilter) && !riskFilter.equals(risk)) {
                        continue;
                    }

                    out.add(rr);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            refreshAsync();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
            dispose();
        }
    }
}
