package university.management.system.charts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import university.management.system.Conn;

/**
 * Grade Distribution Chart - Pie and Bar chart showing distribution of grades
 */
public class GradeDistributionChart extends JPanel {
    private Map<String, Integer> gradeCounts;
    private Map<String, Color> gradeColors;
    private String chartType = "PIE"; // PIE or BAR
    private String filterRegNo = null;
    private String filterDept = null;
    private Integer filterSemester = null;
    private Integer filterYear = null;
    
    public GradeDistributionChart() {
        gradeCounts = new HashMap<>();
        initializeColors();
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.WHITE);
    }
    
    public GradeDistributionChart(String dept, Integer semester, Integer year) {
        this();
        this.filterDept = dept;
        this.filterSemester = semester;
        this.filterYear = year;
    }

    public GradeDistributionChart(String registrationNo) {
        this();
        this.filterRegNo = registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.filterRegNo = registrationNo;
    }

    public void setDepartment(String dept) {
        this.filterDept = dept;
    }

    public void setSemester(Integer semester) {
        this.filterSemester = semester;
    }

    public void setYear(Integer year) {
        this.filterYear = year;
    }
    
    private void initializeColors() {
        gradeColors = new HashMap<>();
        gradeColors.put("A+", new Color(34, 139, 34));   // Green
        gradeColors.put("A", new Color(50, 205, 50));     // Light Green
        gradeColors.put("B+", new Color(30, 144, 255));   // Blue
        gradeColors.put("B", new Color(135, 206, 250));   // Light Blue
        gradeColors.put("C+", new Color(255, 165, 0));    // Orange
        gradeColors.put("C", new Color(255, 215, 0));     // Gold
        gradeColors.put("F", new Color(220, 20, 60));     // Red
    }
    
    public void loadData() {
        gradeCounts.clear();
        try (Conn c = new Conn()) {
            StringBuilder sql = new StringBuilder(
                "SELECT grade, COUNT(*) as count FROM student_result WHERE is_approved = TRUE");
            java.util.List<Object> params = new java.util.ArrayList<>();

            if (filterRegNo != null && !filterRegNo.isEmpty()) {
                sql.append(" AND registration_no = ?");
                params.add(filterRegNo);
            }
            
            if (filterDept != null && !filterDept.isEmpty()) {
                sql.append(" AND registration_no IN (SELECT registration_no FROM student WHERE branch = ?)");
                params.add(filterDept);
            }
            if (filterSemester != null) {
                sql.append(" AND semester = ?");
                params.add(filterSemester);
            }
            if (filterYear != null) {
                sql.append(" AND exam_year = ?");
                params.add(filterYear);
            }
            sql.append(" GROUP BY grade ORDER BY FIELD(grade, 'A+', 'A', 'B+', 'B', 'C+', 'C', 'F')");
            
            try (PreparedStatement ps = c.c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String grade = rs.getString("grade");
                        int count = rs.getInt("count");
                        gradeCounts.put(grade, count);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        repaint();
    }
    
    public void setChartType(String type) {
        this.chartType = type;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gradeCounts.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(Color.GRAY);
            String message = "No data available";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
            g2.dispose();
            return;
        }
        
        if ("PIE".equals(chartType)) {
            drawPieChart(g2);
        } else {
            drawBarChart(g2);
        }
        
        g2.dispose();
    }
    
    private void drawPieChart(Graphics2D g2) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(getWidth(), getHeight()) / 3;
        
        int total = gradeCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return;
        
        double startAngle = 90; // Start from top
        int legendX = 50;
        int legendY = 50;
        int legendSpacing = 25;
        
        // Draw pie slices
        for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
            String grade = entry.getKey();
            int count = entry.getValue();
            double angle = (count * 360.0) / total;
            
            Color color = gradeColors.getOrDefault(grade, Color.GRAY);
            g2.setColor(color);
            
            Arc2D.Double arc = new Arc2D.Double(
                centerX - radius, centerY - radius,
                radius * 2, radius * 2,
                startAngle, angle, Arc2D.PIE);
            g2.fill(arc);
            g2.setColor(Color.BLACK);
            g2.draw(arc);
            
            // Draw legend
            g2.setColor(color);
            g2.fillRect(legendX, legendY, 15, 15);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            double percentage = (count * 100.0) / total;
            String label = grade + ": " + count + " (" + String.format("%.1f", percentage) + "%)";
            g2.drawString(label, legendX + 20, legendY + 12);
            legendY += legendSpacing;
            
            startAngle += angle;
        }
        
        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(Color.BLACK);
        String title = "Grade Distribution";
        if (filterRegNo != null) title += " - " + filterRegNo;
        if (filterDept != null) title += " - " + filterDept;
        if (filterSemester != null) title += " (Sem " + filterSemester + ")";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 30);
    }
    
    private void drawBarChart(Graphics2D g2) {
        int margin = 60;
        int chartWidth = getWidth() - 2 * margin;
        int chartHeight = getHeight() - 2 * margin;
        int barWidth = chartWidth / (gradeCounts.size() + 1);
        
        int total = gradeCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return;
        
        int maxCount = gradeCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        int x = margin;
        int barSpacing = barWidth / 2;
        
        // Draw bars
        for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
            String grade = entry.getKey();
            int count = entry.getValue();
            int barHeight = (int) ((count * (double) chartHeight) / maxCount);
            
            Color color = gradeColors.getOrDefault(grade, Color.GRAY);
            g2.setColor(color);
            
            int barX = x + barSpacing;
            int barY = getHeight() - margin - barHeight;
            g2.fillRect(barX, barY, barWidth - barSpacing, barHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(barX, barY, barWidth - barSpacing, barHeight);
            
            // Draw count on top of bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String countStr = String.valueOf(count);
            FontMetrics fm = g2.getFontMetrics();
            int textX = barX + (barWidth - barSpacing - fm.stringWidth(countStr)) / 2;
            g2.drawString(countStr, textX, barY - 5);
            
            // Draw grade label below
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fm = g2.getFontMetrics();
            textX = barX + (barWidth - barSpacing - fm.stringWidth(grade)) / 2;
            g2.drawString(grade, textX, getHeight() - margin + 20);
            
            x += barWidth;
        }
        
        // Draw Y-axis labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            int value = (maxCount * i) / 5;
            String label = String.valueOf(value);
            FontMetrics fm = g2.getFontMetrics();
            int labelY = getHeight() - margin - (chartHeight * i / 5) + fm.getAscent() / 2;
            g2.drawString(label, margin - fm.stringWidth(label) - 5, labelY);
        }
        
        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String title = "Grade Distribution";
        if (filterRegNo != null) title += " - " + filterRegNo;
        if (filterDept != null) title += " - " + filterDept;
        if (filterSemester != null) title += " (Sem " + filterSemester + ")";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 30);
    }
}

