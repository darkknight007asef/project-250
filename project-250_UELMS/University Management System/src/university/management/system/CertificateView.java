package university.management.system;

import university.management.system.dao.SummaryDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import javax.imageio.ImageIO;

public class CertificateView extends JFrame implements ActionListener {
    private String registrationNo;
    private JButton btnPrint, btnDownload, btnClose;
    private JPanel certificatePanel;
    private boolean eligible;

    public CertificateView(String registrationNo) {
        this.registrationNo = registrationNo;
        setTitle("Course Completion Certificate");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPrint = new JButton("Print");
        UITheme.stylePrimary(btnPrint);
        btnPrint.addActionListener(this);
        
        btnDownload = new JButton("Download PNG");
        UITheme.styleSecondary(btnDownload);
        btnDownload.addActionListener(this);
        
        btnClose = new JButton("Close");
        UITheme.styleGhost(btnClose);
        btnClose.addActionListener(this);
        
        toolbar.add(btnPrint);
        toolbar.add(btnDownload);
        toolbar.add(btnClose);
        add(toolbar, BorderLayout.SOUTH);
        
        // Certificate Panel
        certificatePanel = new CertificatePanel();
        add(new JScrollPane(certificatePanel), BorderLayout.CENTER);
        
        // Verification Check
        eligible = checkEligibility();
        if (!eligible) {
            String details = buildEligibilityDiagnostics();
            JOptionPane.showMessageDialog(
                    this,
                    "You have not yet completed all requirements (8 Semesters Passed) to generate this certificate.\n\n" + details,
                    "Not Eligible",
                    JOptionPane.WARNING_MESSAGE);
            setVisible(false);
            dispose();
            return;
        }

        setVisible(true);
    }

    private String buildEligibilityDiagnostics() {
        try (Conn c = new Conn()) {
            int summaryPass = 0;
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM result_summary WHERE result='PASS' AND registration_no=?")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) summaryPass = rs.getInt(1);
                }
            }

            String dept = "";
            int currentSem = 0;
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT dept, current_semester FROM student_semester WHERE registration_no=?")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dept = rs.getString("dept");
                        currentSem = rs.getInt("current_semester");
                    }
                }
            }

            int srTotal = 0;
            int srApproved = 0;
            int srApprovedSem = 0;
            int srApprovedFails = 0;
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT " +
                            "COUNT(*) AS total_cnt, " +
                            "SUM(CASE WHEN is_approved=TRUE THEN 1 ELSE 0 END) AS approved_cnt, " +
                            "COUNT(DISTINCT CASE WHEN is_approved=TRUE THEN semester END) AS approved_sem_cnt, " +
                            "SUM(CASE WHEN is_approved=TRUE AND status='FAIL' THEN 1 ELSE 0 END) AS approved_fail_cnt " +
                            "FROM student_result WHERE registration_no=?")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        srTotal = rs.getInt("total_cnt");
                        srApproved = rs.getInt("approved_cnt");
                        srApprovedSem = rs.getInt("approved_sem_cnt");
                        srApprovedFails = rs.getInt("approved_fail_cnt");
                    }
                }
            }

            int marksSem = 0;
            int marksRows = 0;
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT COUNT(*) AS rows_cnt, COUNT(DISTINCT semester) AS sem_cnt " +
                            "FROM student_marks WHERE registration_no=? AND grade_point > 0")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        marksRows = rs.getInt("rows_cnt");
                        marksSem = rs.getInt("sem_cnt");
                    }
                }
            }

            return "Debug (" + registrationNo + ")\n" +
                    "result_summary PASS semesters: " + summaryPass + "/8\n" +
                    "student_semester dept/current_semester: " + dept + "/" + currentSem + "\n" +
                    "student_result rows total/approved: " + srTotal + "/" + srApproved + "\n" +
                    "student_result approved semesters: " + srApprovedSem + "/8\n" +
                    "student_result approved FAIL rows: " + srApprovedFails + "\n" +
                    "student_marks rows/semesters (gp>0): " + marksRows + "/" + marksSem;
        } catch (Exception ex) {
            return "Debug failed: " + ex.getMessage();
        }
    }
    
    private boolean checkEligibility() {
        // Primary check: 8 Passed Semesters in result_summary
        // Fallback: verify each semester has all expected courses approved with no FAIL (latest exam_year per semester)
        try (Conn c = new Conn()) {
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM result_summary WHERE result='PASS' AND registration_no=?")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= 8) {
                        return true;
                    }
                }
            }

            String dept = null;
            int currentSem = 0;
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT dept, current_semester FROM student_semester WHERE registration_no=?")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    dept = rs.getString("dept");
                    currentSem = rs.getInt("current_semester");
                }
            }
            if (dept == null || dept.trim().isEmpty()) return false;

            // Do not strictly depend on current_semester promotion.
            // Some databases have completed results but current_semester was not updated.
            boolean strictCurriculumOk = true;
            for (int sem = 1; sem <= 8; sem++) {
                int expectedCourses = 0;
                try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                        "SELECT COUNT(*) FROM department_courses WHERE dept=? AND sem=?")) {
                    ps.setString(1, dept);
                    ps.setInt(2, sem);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) expectedCourses = rs.getInt(1);
                    }
                }
                if (expectedCourses <= 0) {
                    strictCurriculumOk = false;
                    break;
                }

                Integer latestYear = null;
                try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                        "SELECT MAX(exam_year) FROM student_result WHERE registration_no=? AND semester=? AND is_approved=TRUE")) {
                    ps.setString(1, registrationNo);
                    ps.setInt(2, sem);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int y = rs.getInt(1);
                            latestYear = rs.wasNull() ? null : y;
                        }
                    }
                }
                if (latestYear == null) {
                    strictCurriculumOk = false;
                    break;
                }

                int approvedCourses = 0;
                int failCount = 0;
                try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                        "SELECT " +
                                "COUNT(DISTINCT subject_code) AS approved_cnt, " +
                                "SUM(CASE WHEN status='FAIL' THEN 1 ELSE 0 END) AS fail_cnt " +
                                "FROM student_result " +
                                "WHERE registration_no=? AND semester=? AND exam_year=? AND is_approved=TRUE")) {
                    ps.setString(1, registrationNo);
                    ps.setInt(2, sem);
                    ps.setInt(3, latestYear);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            approvedCourses = rs.getInt("approved_cnt");
                            failCount = rs.getInt("fail_cnt");
                        }
                    }
                }

                if (approvedCourses < expectedCourses) {
                    strictCurriculumOk = false;
                    break;
                }
                if (failCount > 0) {
                    strictCurriculumOk = false;
                    break;
                }
            }

            if (strictCurriculumOk) return true;

            // Loose fallback 1: student_result contains approved results for 8 distinct semesters and no FAILs.
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT " +
                            "COUNT(DISTINCT semester) AS sem_cnt, " +
                            "SUM(CASE WHEN status='FAIL' THEN 1 ELSE 0 END) AS fail_cnt " +
                            "FROM student_result WHERE registration_no=? AND is_approved=TRUE")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int semCnt = rs.getInt("sem_cnt");
                        int failCnt = rs.getInt("fail_cnt");
                        if (semCnt >= 8 && failCnt == 0) {
                            return true;
                        }
                    }
                }
            }

            // Loose fallback 2: legacy student_marks contains entries for 8 distinct semesters with grade_point > 0.
            try (java.sql.PreparedStatement ps = c.c.prepareStatement(
                    "SELECT COUNT(DISTINCT semester) AS sem_cnt " +
                            "FROM student_marks WHERE registration_no=? AND grade_point > 0")) {
                ps.setString(1, registrationNo);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int semCnt = rs.getInt("sem_cnt");
                        if (semCnt >= 8) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!eligible) {
            return;
        }
        if (e.getSource() == btnPrint) {
            printCertificate();
        } else if (e.getSource() == btnDownload) {
            downloadCertificate();
        } else if (e.getSource() == btnClose) {
            setVisible(false);
        }
    }
    
    private void printCertificate() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Certificate - " + registrationNo);
        
        job.setPrintable(new Printable() {
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                double scaleX = pf.getImageableWidth() / certificatePanel.getWidth();
                double scaleY = pf.getImageableHeight() / certificatePanel.getHeight();
                double scale = Math.min(scaleX, scaleY);
                g2.scale(scale, scale);
                
                certificatePanel.paint(g2);
                return Printable.PAGE_EXISTS;
            }
        });
        
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException ex) { ex.printStackTrace(); }
        }
    }
    
    private void downloadCertificate() {
        BufferedImage image = new BufferedImage(certificatePanel.getWidth(), certificatePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        certificatePanel.paint(g2);
        g2.dispose();
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Certificate_" + registrationNo + ".png"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(image, "png", fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Certificate Saved!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    // Custom Painting Panel
    class CertificatePanel extends JPanel {
        private String studentName = "Student Name";
        private double cgpa = 0.0;
        
        public CertificatePanel() {
            setPreferredSize(new Dimension(800, 600)); // Landscape A4ish ratio
            setBackground(Color.WHITE);
            loadData();
        }
        
        private void loadData() {
            try (Conn c = new Conn()) {
                java.sql.ResultSet rs = c.s.executeQuery("SELECT name FROM student WHERE registration_no='" + registrationNo + "'");
                if (rs.next()) studentName = rs.getString("name");
                cgpa = SummaryDAO.calculateCGPA(registrationNo);
            } catch (Exception e) {}
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Border
            g2.setColor(new Color(23, 23, 50)); // Dark Blue
            g2.setStroke(new BasicStroke(20));
            g2.drawRect(20, 20, w-40, h-40);
            
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(218, 165, 32)); // Gold
            g2.drawRect(35, 35, w-70, h-70);
            
            // Header
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.BOLD, 36));
            drawCenteredString(g2, "Shahjalal University of Science and Technology", w/2, 100);
            
            g2.setFont(new Font("Serif", Font.PLAIN, 18));
            drawCenteredString(g2, "Sylhet, Bangladesh", w/2, 130);
            
            // Title
            g2.setColor(new Color(23, 23, 50));
            g2.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 48));
            drawCenteredString(g2, "Certificate of Completion", w/2, 220);
            
            // Body
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.PLAIN, 20));
            drawCenteredString(g2, "This is to certify that", w/2, 280);
            
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 32));
            drawCenteredString(g2, studentName, w/2, 330);
            g2.drawLine(w/2 - 150, 335, w/2 + 150, 335); // Underline name
            
            g2.setFont(new Font("Serif", Font.PLAIN, 20));
            drawCenteredString(g2, "Registration No: " + registrationNo, w/2, 380);
            
            String text = "has successfully completed the 4-Year Bachelor Degree Program";
            drawCenteredString(g2, text, w/2, 430);
            
            drawCenteredString(g2, "with a Cumulative Grade Point Average (CGPA) of " + String.format("%.2f", cgpa), w/2, 470);
            
            // Date and Signatures
            g2.setFont(new Font("Serif", Font.PLAIN, 18));
            g2.drawString("Date: " + java.time.LocalDate.now().toString(), 100, 550);
            
            g2.drawLine(w - 250, 540, w - 50, 540);
            g2.drawString("Vice Chancellor", w - 200, 560);
            
            // Seal (Placeholder)
            g2.setColor(new Color(218, 165, 32));
            g2.fillOval(100, 400, 100, 100);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            drawCenteredString(g2, "OFFICIAL", 150, 450);
            drawCenteredString(g2, "SEAL", 150, 465);
        }
        
        private void drawCenteredString(Graphics g, String text, int x, int y) {
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int xCoord = x - (metrics.stringWidth(text) / 2);
            g.drawString(text, xCoord, y);
        }
    }
}
