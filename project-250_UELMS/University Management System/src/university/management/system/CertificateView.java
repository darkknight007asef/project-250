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
            JOptionPane.showMessageDialog(this, "You have not yet completed all requirements (8 Semesters Passed) to generate this certificate.", "Not Eligible", JOptionPane.WARNING_MESSAGE);
            setVisible(false);
            dispose();
            return;
        }

        setVisible(true);
    }
    
    private boolean checkEligibility() {
        // Simplified check: 8 Passed Semesters in Result Summary
        // In reality, should check specific credits
        try (Conn c = new Conn()) {
            java.sql.ResultSet rs = c.s.executeQuery("SELECT COUNT(*) FROM result_summary WHERE result='PASS' AND registration_no='" + registrationNo + "'");
            if (rs.next()) {
                return rs.getInt(1) >= 8;
            }
        } catch (Exception e) {}
        return false; // Strict check
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
