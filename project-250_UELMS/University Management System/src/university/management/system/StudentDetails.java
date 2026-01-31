package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.MessageFormat;
import javax.swing.table.DefaultTableCellRenderer;

public class StudentDetails extends JFrame implements ActionListener {

    JTable table;
    JTextField tfRegNo;
    JButton search, filter, print, add, update, cancel;
    private JButton toggleView;
    private JPanel viewContainer;
    private CardLayout viewLayout;
    private JScrollPane tableScroll;
    private JScrollPane cardScroll;
    private JPanel cardsGrid;
    private boolean cardView = false;

    private static final String STUDENT_SELECT = "SELECT name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path FROM student";
    
    StudentDetails() {
        // Ensure database tables are initialized
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Apply brand frame styling
        UITheme.applyFrame(this);
        setLayout(null);
        
        JLabel heading = new JLabel("Student Details");
        heading.setBounds(0, 10, 1200, 40);
        UITheme.styleTitle(heading);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(heading);
        
        JLabel regLabel = new JLabel("Search by Registration No:");
        regLabel.setBounds(20, 90, 220, 24);
        regLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        regLabel.setForeground(Color.BLACK);
        add(regLabel);
        
        // Modern interactive search box
        tfRegNo = new JTextField();
        tfRegNo.setBounds(250, 88, 220, 36);
        UITheme.styleField(tfRegNo);
        tfRegNo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        // Add placeholder text
        tfRegNo.setText("Enter registration no...");
        tfRegNo.setForeground(Color.GRAY);
        
        // Focus listener for placeholder behavior (borders handled by UITheme)
        tfRegNo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tfRegNo.getText().equals("Enter registration no...")) {
                    tfRegNo.setText("");
                    tfRegNo.setForeground(new Color(30, 41, 59));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (tfRegNo.getText().isEmpty()) {
                    tfRegNo.setText("Enter registration no...");
                    tfRegNo.setForeground(Color.GRAY);
                }
            }
        });
        
        // Key listener for instant search on Enter key
        tfRegNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        add(tfRegNo);
        
        // Themed buttons: Search next to input, then Filter like TeacherDetails
        search = new JButton("Search");
        UITheme.stylePrimary(search);
        search.setBounds(480, 88, 120, 36);
        // Force purple fill on all Look & Feels
        search.setBackground(UITheme.ACCENT_PINK);
        search.setForeground(Color.WHITE);
        search.setContentAreaFilled(true);
        search.setOpaque(true);
        search.addActionListener(this);
        add(search);

        filter = new JButton("Filter Dept");
        UITheme.styleGhost(filter);
        filter.setBounds(600, 88, 130, 36);
        filter.addActionListener(this);
        add(filter);

        print = new JButton("Print");
        UITheme.styleGhost(print);
        print.setBounds(700, 88, 110, 36);
        print.addActionListener(this);
        add(print);

        add = new JButton("Add");
        UITheme.stylePrimary(add);
        add.setBounds(820, 88, 110, 36);
        add.addActionListener(this);
        add(add);

        update = new JButton("Update");
        UITheme.styleGhost(update);
        update.setBounds(940, 88, 110, 36);
        update.addActionListener(this);
        add(update);

        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(1060, 88, 110, 36);
        cancel.addActionListener(this);
        add(cancel);

        toggleView = new JButton("Card View");
        UITheme.styleGhost(toggleView);
        toggleView.setBounds(20, 125, 120, 28);
        toggleView.addActionListener(this);
        add(toggleView);
        
        // Create views
        viewLayout = new CardLayout();
        viewContainer = new JPanel(viewLayout);
        viewContainer.setBounds(20, 150, 1120, 500);
        viewContainer.setOpaque(false);

        // Table view
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
        applyZebra(table);

        // Card view
        cardsGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        cardsGrid.setOpaque(false);
        cardScroll = new JScrollPane(cardsGrid);
        UITheme.styleScroll(cardScroll);
        cardScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);

        viewContainer.add(tableScroll, "table");
        viewContainer.add(cardScroll, "cards");
        add(viewContainer);
        
        loadAllStudents();
        
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    private void applyZebra(JTable t) {
        final Color alt = new Color(248, 248, 252);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : alt);
                }
                return c;
            }
        });
    }

    // Method to create interactive buttons with modern styling
    private JButton createInteractiveButton(String text, Color baseColor, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(baseColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor.brighter(), 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            public void mouseExited(MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor.darker(), 1),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            public void mousePressed(MouseEvent evt) {
                button.setBackground(baseColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor.darker(), 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            public void mouseReleased(MouseEvent evt) {
                button.setBackground(baseColor.brighter());
            }
        });
        
        return button;
    }
    
    // Style table method
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(59, 130, 246));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Make table non-editable
        table.setDefaultEditor(Object.class, null);
    }
    
    // Style scroll pane method
    private void styleScrollPane(JScrollPane jsp) {
        jsp.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        jsp.getViewport().setBackground(Color.WHITE);
    }
    
    private void performSearch() {
        String reg = tfRegNo.getText().trim();
        
        // Ignore if it's the placeholder text
        if (reg.equals("Enter registration no...") || reg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Registration Number.");
            return;
        }
        
        String query = STUDENT_SELECT + " WHERE registration_no = '"+reg+"'";
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery(query);
            
            if (!rs.isBeforeFirst()) { 
                JOptionPane.showMessageDialog(this, "No student found with this Registration Number!");
                loadAllStudents(); // Reload all students if not found
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
            JOptionPane.showMessageDialog(this, "Error searching student: " + e.getMessage());
        }
    }
    
    private void loadAllStudents() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery(STUDENT_SELECT);
            if (cardView) {
                loadCardsFromResultSet(rs);
            } else {
                table.setModel(DbUtils.resultSetToTableModel(rs));
                titleCaseHeaders(table);
            }
        } catch (SQLException e) {
            // Check if it's a "table doesn't exist" error
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                try {
                    // Try to initialize database
                    DatabaseInitializer.initializeDatabase();
                    // Retry the query
                    Conn c = new Conn();
                    ResultSet rs = c.s.executeQuery(STUDENT_SELECT);
                    if (cardView) {
                        loadCardsFromResultSet(rs);
                    } else {
                        table.setModel(DbUtils.resultSetToTableModel(rs));
                        titleCaseHeaders(table);
                    }
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Database tables are missing. Please restart the application.\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }
    
    private void filterByDepartment() {
        String[] deptArray = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        
        // Create a custom dialog with better styling
        JDialog dialog = new JDialog(this, "Department Filter", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(248, 250, 252));
        
        JLabel label = new JLabel("Select Department:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(71, 85, 105));
        
        JComboBox<String> deptComboBox = new JComboBox<>(deptArray);
        deptComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deptComboBox.setBackground(Color.WHITE);
        deptComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 250, 252));
        
        JButton okButton = createInteractiveButton("Apply Filter", new Color(34, 139, 34), 0, 0, 120, 30);
        JButton cancelButton = createInteractiveButton("Cancel", new Color(178, 34, 34), 0, 0, 100, 30);
        
        okButton.addActionListener(e -> {
            String selectedDept = (String) deptComboBox.getSelectedItem();
            applyDepartmentFilter(selectedDept);
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        contentPanel.add(label, BorderLayout.NORTH);
        contentPanel.add(deptComboBox, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private void applyDepartmentFilter(String selectedDept) {
        if (selectedDept != null) {
            try {
                Conn c = new Conn();
                ResultSet rs = c.s.executeQuery(STUDENT_SELECT + " WHERE branch = '"+selectedDept+"'");
                
                if (!rs.isBeforeFirst()) { 
                    JOptionPane.showMessageDialog(this, 
                        "No students found in " + selectedDept + " department!",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    if (cardView) {
                        loadCardsFromResultSet(rs);
                    } else {
                        table.setModel(DbUtils.resultSetToTableModel(rs));
                        titleCaseHeaders(table);
                    }
                    JOptionPane.showMessageDialog(this, 
                        "Showing students from " + selectedDept + " department",
                        "Filter Applied",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error applying filter: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void titleCaseHeaders(JTable t) {
        try {
            for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
                String name = t.getColumnModel().getColumn(i).getHeaderValue().toString();
                String key = name.trim().toLowerCase().replace(' ', '_');
                String pretty;
                if ("class_x".equals(key)) {
                    pretty = "SSC";
                } else if ("class_xii".equals(key) || "class_xi".equals(key)) {
                    pretty = "HSC";
                } else {
                    pretty = toTitleCase(name);
                }
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
                String reg = rs.getString("registration_no");
                String branch = rs.getString("branch");
                String phone = rs.getString("phone");
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

                JLabel lblReg = new JLabel("Reg: " + (reg == null ? "" : reg));
                lblReg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblReg.setForeground(new Color(71, 85, 105));

                JLabel lblBranch = new JLabel("Dept: " + (branch == null ? "" : branch));
                lblBranch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblBranch.setForeground(new Color(71, 85, 105));

                JLabel lblPhone = new JLabel("Phone: " + (phone == null ? "" : phone));
                lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblPhone.setForeground(new Color(71, 85, 105));

                info.add(lblName);
                info.add(Box.createVerticalStrut(6));
                info.add(lblReg);
                info.add(lblBranch);
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
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == toggleView) {
            cardView = !cardView;
            toggleView.setText(cardView ? "Table View" : "Card View");
            viewLayout.show(viewContainer, cardView ? "cards" : "table");
            loadAllStudents();
        }
        else if (ae.getSource() == filter) {
            filterByDepartment();
        } 
        else if (ae.getSource() == search) {
            performSearch();
        } 
        else if (ae.getSource() == print) {
            try {
                MessageFormat header = new MessageFormat("Student Details");
                MessageFormat footer = new MessageFormat("Page {0}");
                if (!cardView) table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during printing: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (ae.getSource() == add) {
            setVisible(false);
            new AddStudent();
        }
        else if (ae.getSource() == update) {
            String reg = null;
            int row = table.getSelectedRow();
            if (row >= 0) {
                int colIdx = -1;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    String colName = table.getColumnName(i);
                    if (colName != null && colName.equalsIgnoreCase("registration_no")) { colIdx = i; break; }
                }
                if (colIdx != -1) {
                    Object v = table.getValueAt(row, colIdx);
                    if (v != null) reg = v.toString();
                }
            }
            if (reg == null || reg.trim().isEmpty()) {
                reg = JOptionPane.showInputDialog(this, "Enter Student Registration Number:");
            }
            if (reg != null && !reg.trim().isEmpty()) {
                new UpdateStudent(reg.trim());
            }
        }
        else if (ae.getSource() == cancel) {
            int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to close the Student Details?",
                "Confirm Close",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                setVisible(false);
                dispose();
            }
        }
    }

    public static void main(String[] args) {
        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new StudentDetails();
        });
    }
}