package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class TeacherDetails extends JFrame implements ActionListener {

    JTable table;
    JTextField tfPhone;
    JButton search, filter, print, update, add, cancel;
    JTextField tfDept;

    private JButton toggleView;
    private JPanel viewContainer;
    private CardLayout viewLayout;
    private JScrollPane tableScroll;
    private JScrollPane cardScroll;
    private JPanel cardsGrid;
    private boolean cardView = false;

    private static final String TEACHER_SELECT = "SELECT name, fname, empId, dob, address, phone, email, bsc_in_sub, msc_in_sub, cgpa_in_bsc, cgpa_in_msc, phd, department, position, photo_path FROM teacher";
    
    public TeacherDetails() {
        UITheme.applyFrame(this);
        setLayout(null);
        JLabel heading = new JLabel("Teacher Details");
        heading.setBounds(0, 10, 1200, 40);
        UITheme.styleTitle(heading);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(heading);
        
        JLabel phoneLabel = new JLabel("Search by Phone:");
        UITheme.styleLabel(phoneLabel);
        phoneLabel.setBounds(20, 90, 180, 24);
        add(phoneLabel);
        
        tfPhone = new JTextField();
        UITheme.styleField(tfPhone);
        tfPhone.setBounds(200, 88, 220, 36);
        tfPhone.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfPhone);
        
        JLabel deptLabel = new JLabel("Department:");
        UITheme.styleLabel(deptLabel);
        deptLabel.setBounds(20, 20, 150, 20);
        add(deptLabel);
        
        tfDept = new JTextField();
        UITheme.styleField(tfDept);
        tfDept.setBounds(180, 20, 170, 30);
        add(tfDept);
        // Hide department input as requested (kept for potential future use without breaking code)
        deptLabel.setVisible(false);
        tfDept.setVisible(false);
        
        search = new JButton("Search");
        UITheme.stylePrimary(search);
        search.setBounds(440, 88, 110, 36);
        search.addActionListener(this);
        add(search);
        
        filter = new JButton("Filter Dept");
        UITheme.styleGhost(filter);
        filter.setBounds(560, 88, 110, 36);
        filter.addActionListener(this);
        add(filter);
        
        print = new JButton("Print");
        UITheme.styleGhost(print);
        print.setBounds(680, 88, 110, 36);
        print.addActionListener(this);
        add(print);
        
        add = new JButton("Add");
        UITheme.stylePrimary(add);
        add.setBounds(800, 88, 110, 36);
        add.addActionListener(this);
        add(add);
        
        update = new JButton("Update");
        UITheme.styleGhost(update);
        update.setBounds(920, 88, 110, 36);
        update.addActionListener(this);
        add(update);
        
        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(1040, 88, 110, 36);
        cancel.addActionListener(this);
        add(cancel);

        toggleView = new JButton("Card View");
        UITheme.styleGhost(toggleView);
        toggleView.setBounds(20, 125, 120, 28);
        toggleView.addActionListener(this);
        add(toggleView);
        
        viewLayout = new CardLayout();
        viewContainer = new JPanel(viewLayout);
        viewContainer.setBounds(20, 150, 1120, 500);
        viewContainer.setOpaque(false);

        table = new JTable();
        UITheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(36);
        table.setIntercellSpacing(new Dimension(8, 8));
        tableScroll = new JScrollPane(table);
        UITheme.styleScroll(tableScroll);
        tableScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Deselect when clicking outside cells
        tableScroll.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                if (row == -1 || col == -1) {
                    table.clearSelection();
                }
            }
        });
        // zebra rows (inline)
        final Color alt = new Color(248, 248, 252);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : alt);
                }
                return c;
            }
        });

        cardsGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        cardsGrid.setOpaque(false);
        cardScroll = new JScrollPane(cardsGrid);
        UITheme.styleScroll(cardScroll);
        cardScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);

        viewContainer.add(tableScroll, "table");
        viewContainer.add(cardScroll, "cards");
        add(viewContainer);
        
        loadAllTeachers();
        
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void loadAllTeachers() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery(TEACHER_SELECT);
            if (cardView) {
                loadCardsFromResultSet(rs);
            } else {
                table.setModel(DbUtils.resultSetToTableModel(rs));
                titleCaseHeaders(table);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void filterByDepartment() {
        String[] deptArray = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        
        String selectedDept = (String) JOptionPane.showInputDialog(
                this,
                "Select Department",
                "Department Filter",
                JOptionPane.QUESTION_MESSAGE,
                null,
                deptArray,
                deptArray[0]
        );
        
        if (selectedDept != null) {
            try {
                Conn c = new Conn();
                ResultSet rs = c.s.executeQuery(TEACHER_SELECT + " WHERE department = '"+selectedDept+"'");
                
                if (!rs.isBeforeFirst()) {
                    JOptionPane.showMessageDialog(this, "No teachers found in " + selectedDept + " department!");
                } else {
                    if (cardView) {
                        loadCardsFromResultSet(rs);
                    } else {
                        table.setModel(DbUtils.resultSetToTableModel(rs));
                        titleCaseHeaders(table);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == toggleView) {
            cardView = !cardView;
            toggleView.setText(cardView ? "Table View" : "Card View");
            viewLayout.show(viewContainer, cardView ? "cards" : "table");
            loadAllTeachers();
        }
        else if (ae.getSource() == filter) {
            filterByDepartment();
        } 
        else if (ae.getSource() == search) {
            String phone = tfPhone.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a phone number.");
                return;
            }
            
            String query = TEACHER_SELECT + " WHERE phone = '"+phone+"'";
            try {
                Conn c = new Conn();
                ResultSet rs = c.s.executeQuery(query);
                
                if (!rs.isBeforeFirst()) {
                    JOptionPane.showMessageDialog(this, "No teacher found with this phone number!");
                } else {
                    if (cardView) {
                        loadCardsFromResultSet(rs);
                    } else {
                        table.setModel(DbUtils.resultSetToTableModel(rs));
                        titleCaseHeaders(table);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        else if (ae.getSource() == print) {
            try {
                if (!cardView) table.print();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        else if (ae.getSource() == add) {
            setVisible(false);
            new AddTeacher();
        } 
        else if (ae.getSource() == update) {
            // Try to get empId from selected row; fallback to prompt
            String empId = null;
            int row = table.getSelectedRow();
            if (row >= 0) {
                int colIdx = -1;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    String colName = table.getColumnName(i);
                    if (colName != null && colName.equalsIgnoreCase("empId")) { colIdx = i; break; }
                }
                if (colIdx != -1) {
                    Object v = table.getValueAt(row, colIdx);
                    if (v != null) empId = v.toString();
                }
            }
            if (empId == null || empId.trim().isEmpty()) {
                empId = JOptionPane.showInputDialog(this, "Enter Faculty Employee ID:");
            }
            if (empId != null && !empId.trim().isEmpty()) {
                new UpdateTeacher(empId.trim());
            }
        } 
        else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new TeacherDetails();
    }

    private void titleCaseHeaders(JTable t) {
        try {
            for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
                String name = t.getColumnModel().getColumn(i).getHeaderValue().toString();
                String pretty = toTitleCase(name);
                t.getColumnModel().getColumn(i).setHeaderValue(pretty);
            }
            t.getTableHeader().revalidate();
            t.getTableHeader().repaint();
        } catch (Exception ignore) {}
    }

    private String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            String cap = p.substring(0, 1).toUpperCase() + (p.length() > 1 ? p.substring(1).toLowerCase() : "");
            if (sb.length() > 0) sb.append(' ');
            sb.append(cap);
        }
        return sb.toString();
    }

    private void loadCardsFromResultSet(ResultSet rs) {
        cardsGrid.removeAll();
        try {
            while (rs.next()) {
                String name = rs.getString("name");
                String empId = rs.getString("empId");
                String dept = rs.getString("department");
                String phone = rs.getString("phone");
                String position = rs.getString("position");
                String photo = rs.getString("photo_path");

                JPanel card = new JPanel(new BorderLayout());
                UITheme.styleCard(card);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));

                JLabel img = new JLabel();
                img.setPreferredSize(new Dimension(90, 90));
                img.setHorizontalAlignment(SwingConstants.CENTER);
                img.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
                if (photo != null && !photo.trim().isEmpty()) {
                    try {
                        ImageIcon icon = new ImageIcon(photo);
                        Image scaled = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                        img.setIcon(new ImageIcon(scaled));
                    } catch (Exception ignore) {
                        img.setIcon(null);
                    }
                }

                JPanel info = new JPanel();
                info.setOpaque(false);
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

                JLabel lblName = new JLabel(name == null ? "" : name);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblName.setForeground(new Color(30, 41, 59));

                JLabel lblId = new JLabel("ID: " + (empId == null ? "" : empId));
                lblId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblId.setForeground(new Color(71, 85, 105));

                JLabel lblDept = new JLabel("Dept: " + (dept == null ? "" : dept));
                lblDept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblDept.setForeground(new Color(71, 85, 105));

                JLabel lblPos = new JLabel("Pos: " + (position == null ? "" : position));
                lblPos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblPos.setForeground(new Color(71, 85, 105));

                JLabel lblPhone = new JLabel("Phone: " + (phone == null ? "" : phone));
                lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblPhone.setForeground(new Color(71, 85, 105));

                info.add(lblName);
                info.add(Box.createVerticalStrut(6));
                info.add(lblId);
                info.add(lblDept);
                info.add(lblPos);
                info.add(lblPhone);

                JPanel left = new JPanel(new BorderLayout());
                left.setOpaque(false);
                left.add(img, BorderLayout.NORTH);

                card.add(left, BorderLayout.WEST);
                card.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
                card.add(info, BorderLayout.EAST);

                cardsGrid.add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cardsGrid.revalidate();
        cardsGrid.repaint();
    }
}
