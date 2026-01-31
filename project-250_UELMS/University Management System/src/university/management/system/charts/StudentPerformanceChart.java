package university.management.system.charts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import university.management.system.Conn;
import university.management.system.dao.SummaryDAO;

/**
 * Student Performance Chart - Line chart showing GPA across semesters
 */
public class StudentPerformanceChart extends JPanel {
    private String registrationNo;
    private List<SemesterData> semesterData;
    
    private static class SemesterData {
        int semester;
        int year;
        double gpa;
        
        SemesterData(int semester, int year, double gpa) {
            this.semester = semester;
            this.year = year;
            this.gpa = gpa;
        }
    }
    
    public StudentPerformanceChart(String registrationNo) {
        this.registrationNo = registrationNo;
        this.semesterData = new ArrayList<>();
        setPreferredSize(new Dimension(700, 400));
        setBackground(Color.WHITE);
        loadData();
    }
    
    private void loadData() {
        semesterData.clear();
        try (Conn c = new Conn()) {
            // First try to get data from result_summary
            String sql = "SELECT semester, exam_year, gpa FROM result_summary " +
                        "WHERE registration_no = ? ORDER BY exam_year, semester";
            try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                ps.setString(1, registrationNo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int semester = rs.getInt("semester");
                        int year = rs.getInt("exam_year");
                        double gpa = rs.getDouble("gpa");
                        semesterData.add(new SemesterData(semester, year, gpa));
                    }
                }
            }
            
            // If no data in result_summary, calculate from student_marks
            if (semesterData.isEmpty()) {
                String marksSql = "SELECT semester, " +
                                 "AVG(grade_point) as gpa " +
                                 "FROM student_marks " +
                                 "WHERE registration_no = ? AND grade_point > 0 " +
                                 "GROUP BY semester " +
                                 "ORDER BY semester";
                try (PreparedStatement ps = c.c.prepareStatement(marksSql)) {
                    ps.setString(1, registrationNo);
                    try (ResultSet rs = ps.executeQuery()) {
                        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                        int semesterNum = 1;
                        while (rs.next()) {
                            int semester = rs.getInt("semester");
                            double gpa = rs.getDouble("gpa");
                            // Estimate year based on semester (assuming 2 semesters per year)
                            int year = currentYear - (int)Math.ceil((8 - semester) / 2.0);
                            semesterData.add(new SemesterData(semester, year, gpa));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (semesterData.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(Color.GRAY);
            String message = "No performance data available";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
            g2.dispose();
            return;
        }
        
        drawLineChart(g2);
        g2.dispose();
    }
    
    private void drawLineChart(Graphics2D g2) {
        int margin = 70;
        int chartWidth = getWidth() - 2 * margin;
        int chartHeight = getHeight() - 2 * margin;
        
        // Find min and max GPA for scaling
        double minGPA = semesterData.stream().mapToDouble(d -> d.gpa).min().orElse(0.0);
        double maxGPA = semesterData.stream().mapToDouble(d -> d.gpa).max().orElse(4.0);
        if (maxGPA == minGPA) {
            maxGPA = 4.0;
            minGPA = 0.0;
        }
        double gpaRange = maxGPA - minGPA;
        if (gpaRange == 0) gpaRange = 4.0;
        
        // Draw grid lines
        g2.setColor(new Color(230, 230, 230));
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 4; i++) {
            double gpaValue = minGPA + (gpaRange * i / 4);
            int y = getHeight() - margin - (int) ((gpaValue - minGPA) * chartHeight / gpaRange);
            g2.drawLine(margin, y, getWidth() - margin, y);
        }
        
        // Draw axes
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(margin, getHeight() - margin, getWidth() - margin, getHeight() - margin); // X-axis
        g2.drawLine(margin, margin, margin, getHeight() - margin); // Y-axis
        
        // Draw Y-axis labels (GPA)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= 4; i++) {
            double gpaValue = minGPA + (gpaRange * i / 4);
            String label = String.format("%.2f", gpaValue);
            FontMetrics fm = g2.getFontMetrics();
            int y = getHeight() - margin - (int) ((gpaValue - minGPA) * chartHeight / gpaRange) + fm.getAscent() / 2;
            g2.drawString(label, margin - fm.stringWidth(label) - 10, y);
        }
        
        // Draw data points and line
        if (semesterData.size() > 0) {
            int pointSize = 8;
            int pointRadius = pointSize / 2;
            Color lineColor = new Color(124, 58, 237); // Purple accent
            Color pointColor = new Color(124, 58, 237);
            
            // Calculate X positions
            int dataCount = semesterData.size();
            int xSpacing = chartWidth / Math.max(1, dataCount - 1);
            
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < dataCount; i++) {
                SemesterData data = semesterData.get(i);
                int x = margin + (i * xSpacing);
                int y = getHeight() - margin - (int) ((data.gpa - minGPA) * chartHeight / gpaRange);
                points.add(new Point(x, y));
            }
            
            // Draw line connecting points
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }
            
            // Draw points and labels
            g2.setColor(pointColor);
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                SemesterData data = semesterData.get(i);
                
                // Draw point
                g2.fillOval(p.x - pointRadius, p.y - pointRadius, pointSize, pointSize);
                g2.setColor(Color.WHITE);
                g2.fillOval(p.x - pointRadius + 2, p.y - pointRadius + 2, pointSize - 4, pointSize - 4);
                g2.setColor(pointColor);
                g2.drawOval(p.x - pointRadius, p.y - pointRadius, pointSize, pointSize);
                
                // Draw GPA value above point
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String gpaLabel = String.format("%.2f", data.gpa);
                FontMetrics fm = g2.getFontMetrics();
                int labelX = p.x - fm.stringWidth(gpaLabel) / 2;
                g2.setColor(Color.BLACK);
                g2.drawString(gpaLabel, labelX, p.y - 10);
                
                // Draw semester label below X-axis
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String semLabel = "Sem " + data.semester;
                fm = g2.getFontMetrics();
                labelX = p.x - fm.stringWidth(semLabel) / 2;
                g2.drawString(semLabel, labelX, getHeight() - margin + 20);
            }
        }
        
        // Draw title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String title = "GPA Performance Across Semesters";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(title, titleX, 30);
        
        // Draw Y-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String yLabel = "GPA";
        fm = g2.getFontMetrics();
        int yLabelX = 15;
        int yLabelY = getHeight() / 2;
        g2.rotate(-Math.PI / 2, yLabelX, yLabelY);
        g2.drawString(yLabel, yLabelX - fm.stringWidth(yLabel) / 2, yLabelY);
        g2.rotate(Math.PI / 2, yLabelX, yLabelY);
        
        // Draw X-axis label
        String xLabel = "Semester";
        fm = g2.getFontMetrics();
        int xLabelX = (getWidth() - fm.stringWidth(xLabel)) / 2;
        g2.drawString(xLabel, xLabelX, getHeight() - 15);
    }
}

