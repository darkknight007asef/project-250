package university.management.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Project extends JFrame {
    
    // Themed colors (aligned with UITheme)
    private final Color BG_DARK = UITheme.BG_DARK;
    private final Color CARD_DARK = UITheme.CARD_DARK;
    private final Color CARD_BORDER = new Color(230, 232, 240); // subtle light border
    private final Color TEXT_PRIMARY = UITheme.TEXT_PRIMARY;
    private final Color TEXT_SECONDARY = UITheme.TEXT_SECONDARY;
    private final Color ACCENT = UITheme.ACCENT_PINK;
    private final String userRole; // Store current user's role
    private boolean isTeacher;

    public Project() {
        this(null); // Default to null role (admin)
    }
    
    public Project(String role) {
        this.userRole = role;
        this.isTeacher = role != null && "TEACHER".equalsIgnoreCase(role.trim());
        
        // Set window properties
        String title = "University Management System";
        if (isTeacher) {
            title += " - Teacher Dashboard";
        } else {
            title += " - Admin Dashboard";
        }
        setTitle(title);
        setSize(1540, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        
        // Initialize database tables (async to avoid blocking UI)
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseInitializer.initializeDatabase();
            } catch (Exception e) {
                e.printStackTrace(); // Log to console
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Database initialization warning:\n" + e.getMessage() + 
                            "\n\nSome features may not work until this is resolved.",
                            "Database Warning",
                            JOptionPane.WARNING_MESSAGE);
                });
            }
        });
        
        // Create main panel
        createMainPanel();
        
        setVisible(true);
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        
        if (isTeacher) {
            mainPanel.add(createTeacherDashboard(), BorderLayout.CENTER);
        } else {
            mainPanel.add(createAdminDashboard(), BorderLayout.CENTER);
        }
        
        add(mainPanel);
    }

    // ==========================================
    // TEACHER DASHBOARD
    // ==========================================
    private JPanel createTeacherDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BG_DARK);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(CARD_DARK);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER));

        // Profile / Header in Sidebar
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(40, 30, 40, 30));
        
        JLabel lblRole = new JLabel("Teacher Portal");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblRole.setForeground(ACCENT);
        
        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcome.setForeground(TEXT_SECONDARY);

        profilePanel.add(lblRole, BorderLayout.NORTH);
        profilePanel.add(lblWelcome, BorderLayout.SOUTH);
        sidebar.add(profilePanel);

        // Navigation Items
        sidebar.add(createSimpleNavItem("Dashboard", true));
        sidebar.add(Box.createVerticalGlue()); // Push logout to bottom
        
        // Logout Button
        JButton btnLogout = new JButton("  Logout");
        try {
            ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("university/management/system/icons/cancel.png"));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btnLogout.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            // invisible fallback
        }
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(UITheme.DANGER);
        btnLogout.setBackground(CARD_DARK);
        btnLogout.setBorder(new EmptyBorder(20, 30, 40, 0));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setContentAreaFilled(false);
        btnLogout.addActionListener(e -> handleMenuAction("Logout"));
        
        JPanel logoutContainer = new JPanel(new BorderLayout());
        logoutContainer.setOpaque(false);
        logoutContainer.add(btnLogout, BorderLayout.WEST);
        sidebar.add(logoutContainer);

        dashboard.add(sidebar, BorderLayout.WEST);

        // --- Main Content Area ---
        JPanel contentScrollWrapper = new JPanel(new BorderLayout());
        contentScrollWrapper.setBackground(BG_DARK);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Header
        JLabel lblDashboard = new JLabel("Dashboard Overview");
        lblDashboard.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblDashboard.setForeground(TEXT_PRIMARY);
        lblDashboard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblDashboard);
        content.add(Box.createVerticalStrut(30));

        // Section: Examination
        content.add(createSectionHeader("Examination Management"));
        content.add(Box.createVerticalStrut(20));
        
        JPanel examGrid = new JPanel(new GridLayout(2, 2, 25, 25));
        examGrid.setOpaque(false);
        examGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        examGrid.setMaximumSize(new Dimension(2000, 360)); // Restrict height

        examGrid.add(createMenuCard(new MenuItem("Enter Marks", "🎯", ACCENT)));
        examGrid.add(createMenuCard(new MenuItem("View Marks", "📑", ACCENT)));
        examGrid.add(createMenuCard(new MenuItem("Grade Distribution", "📊", ACCENT)));
        examGrid.add(createMenuCard(new MenuItem("Recheck Requests", "🔄", ACCENT)));
        
        content.add(examGrid);
        content.add(Box.createVerticalStrut(40));

        // Section: Requests
        content.add(createSectionHeader("Requests"));
        content.add(Box.createVerticalStrut(20));

        JPanel reqGrid = new JPanel(new GridLayout(1, 3, 25, 0));
        reqGrid.setOpaque(false);
        reqGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        reqGrid.setMaximumSize(new Dimension(2000, 160));

        reqGrid.add(createMenuCard(new MenuItem("Recheck Requests", "🔄", ACCENT)));
        JPanel reqSpacer1 = new JPanel(); reqSpacer1.setOpaque(false); reqGrid.add(reqSpacer1);
        JPanel reqSpacer2 = new JPanel(); reqSpacer2.setOpaque(false); reqGrid.add(reqSpacer2);

        content.add(reqGrid);
        content.add(Box.createVerticalStrut(40));

        // Section: Institution
        content.add(createSectionHeader("Institution Information"));
        content.add(Box.createVerticalStrut(20));
        
        JPanel instGrid = new JPanel(new GridLayout(1, 3, 25, 0));
        instGrid.setOpaque(false);
        instGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        instGrid.setMaximumSize(new Dimension(2000, 160));

        instGrid.add(createMenuCard(new MenuItem("Department Info", "🏛️", ACCENT)));
        JPanel spacer1 = new JPanel(); spacer1.setOpaque(false); instGrid.add(spacer1);
        JPanel spacer2 = new JPanel(); spacer2.setOpaque(false); instGrid.add(spacer2);

        content.add(instGrid);

        // Fill remaining vert space
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        dashboard.add(scrollPane, BorderLayout.CENTER);

        return dashboard;
    }

    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createSimpleNavItem(String text, boolean isActive) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 16));
        l.setForeground(isActive ? TEXT_PRIMARY : TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(12, 30, 12, 0));
        
        if (isActive) {
            JPanel line = new JPanel();
            line.setBackground(ACCENT);
            line.setPreferredSize(new Dimension(4, 50));
            p.add(line, BorderLayout.WEST);
            p.setBackground(new Color(236, 232, 254)); // Light accent bg
            p.setOpaque(true);
        }
        
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // ADMIN DASHBOARD (Legacy Grid)
    // ==========================================
    private JPanel createAdminDashboard() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Admin Dashboard", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel menuCardsPanel = createAdminMenuGrid();
        mainPanel.add(menuCardsPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private JPanel createAdminMenuGrid() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        // Define items
        java.util.List<MenuItem> allItems = new java.util.ArrayList<>();
        allItems.add(new MenuItem("New Student", "👨‍🎓", ACCENT));
        allItems.add(new MenuItem("New Faculty", "👨‍🏫", ACCENT));
        allItems.add(new MenuItem("View Students", "📋", ACCENT));
        allItems.add(new MenuItem("View Faculty", "📊", ACCENT));
        allItems.add(new MenuItem("Update Student", "🛠️", ACCENT));
        allItems.add(new MenuItem("Update Faculty", "📝", ACCENT));
        allItems.add(new MenuItem("Student Leave", "📅", ACCENT));
        allItems.add(new MenuItem("Faculty Leave", "🗓️", ACCENT));
        
        allItems.add(new MenuItem("Department Info", "🏛️", ACCENT));
        allItems.add(new MenuItem("View Marks", "📑", ACCENT));
        
        allItems.add(new MenuItem("Manage Subjects", "📚", ACCENT));
        allItems.add(new MenuItem("Manage Courses", "📖", ACCENT));
        allItems.add(new MenuItem("Approve Results", "✅", ACCENT));
        allItems.add(new MenuItem("Grade Distribution", "📊", ACCENT));
        allItems.add(new MenuItem("Recheck Requests", "🔄", ACCENT));
        allItems.add(new MenuItem("At-Risk Students", "⚠️", ACCENT));

        MenuItem[] menuItems = allItems.toArray(new MenuItem[0]);

        // Logout button
        MenuItem logoutItem = new MenuItem("Logout", "🚪", ACCENT);
        JPanel logoutCard = createMenuCard(logoutItem);
        JPanel logoutRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoutRow.setOpaque(false);
        logoutRow.add(logoutCard);

        // Grid Container
        JPanel rowsContainer = new JPanel();
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);

        int columnsPerRow = 3;
        
        // Dynamic row creation
        JPanel currentRow = null;
        for (int i = 0; i < menuItems.length; i++) {
            if (i % columnsPerRow == 0) {
                if (currentRow != null) {
                    rowsContainer.add(currentRow);
                    rowsContainer.add(Box.createRigidArea(new Dimension(0, 20)));
                }
                currentRow = new JPanel(new GridLayout(1, columnsPerRow, 20, 20));
                currentRow.setOpaque(false);
            }
            currentRow.add(createMenuCard(menuItems[i]));
        }
        
        if (currentRow != null) {
            int remaining = columnsPerRow - (menuItems.length % columnsPerRow);
            if (remaining < columnsPerRow && remaining > 0) {
                 // If there is a vacancy on the last row, place Logout there.
                 currentRow.add(logoutCard);
                 for (int j = 1; j < remaining; j++) {
                     JPanel dummy = new JPanel();
                     dummy.setOpaque(false);
                     currentRow.add(dummy);
                 }
                 logoutCard = null;
            }
            rowsContainer.add(currentRow);
        }

        if (logoutCard != null) {
            rowsContainer.add(Box.createRigidArea(new Dimension(0, 20)));
            rowsContainer.add(logoutRow);
        }

        rowsContainer.add(Box.createRigidArea(new Dimension(0, 80)));

        JScrollPane rowsScroll = new JScrollPane(rowsContainer);
        rowsScroll.setBorder(BorderFactory.createEmptyBorder());
        rowsScroll.getVerticalScrollBar().setUnitIncrement(16);
        rowsScroll.setOpaque(false);
        rowsScroll.getViewport().setOpaque(false);
        mainContainer.add(rowsScroll, BorderLayout.CENTER);
        
        return mainContainer;
    }

    // ==========================================
    // SHARED COMPONENT CREATION
    // ==========================================
    private JPanel createMenuCard(MenuItem menuItem) {
        // Create main card panel with fixed size
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_DARK);
        // Slightly taller to prevent icon/text cut-off
        card.setPreferredSize(new Dimension(320, 180)); // Preferred size for Admin grid
        // For Teacher grid (GridLayout), this size acts as a suggestion/minimum content size
        
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(24, 18, 18, 18)
        ));

        // Top accent strip
        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(10, 3));
        accent.setBackground(menuItem.accent);
        
        JPanel accentWrapper = new JPanel(new BorderLayout());
        accentWrapper.setOpaque(false);
        accentWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        accentWrapper.add(accent, BorderLayout.NORTH);
        
        card.add(accentWrapper, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        
        contentPanel.add(Box.createVerticalGlue());
        
        // Icon
        JLabel iconLabel;
        if ("At-Risk Students".equals(menuItem.text)) {
            Icon warn = UIManager.getIcon("OptionPane.warningIcon");
            if (warn instanceof ImageIcon) {
                Image img = ((ImageIcon) warn).getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH);
                iconLabel = new JLabel(new ImageIcon(img), JLabel.CENTER);
            } else if (warn != null) {
                BufferedImage bi = new BufferedImage(warn.getIconWidth(), warn.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                warn.paintIcon(null, g2, 0, 0);
                g2.dispose();
                Image img = bi.getScaledInstance(42, 42, Image.SCALE_SMOOTH);
                iconLabel = new JLabel(new ImageIcon(img), JLabel.CENTER);
            } else {
                iconLabel = new JLabel("!", JLabel.CENTER);
                iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
                iconLabel.setForeground(menuItem.accent);
            }
        } else {
            iconLabel = new JLabel(menuItem.icon, JLabel.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            iconLabel.setForeground(menuItem.accent);
        }

        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(56, 56));
        iconLabel.setMinimumSize(new Dimension(56, 56));
        
        JPanel iconContainer = new JPanel(new GridBagLayout());
        iconContainer.setOpaque(false);
        iconContainer.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        iconContainer.add(iconLabel);
        contentPanel.add(iconContainer);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Title
        JLabel title = new JLabel(menuItem.text, JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        
        // Subtitle
        JLabel subtitle = new JLabel("Click to open", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 94, 105));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitle);
        
        if ("Department Info".equals(menuItem.text)) {
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));
            subtitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, subtitle.getPreferredSize().height));
        }
        
        contentPanel.add(Box.createVerticalGlue());
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Hover
        final Color originalColor = CARD_DARK;
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(236, 232, 254));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(originalColor);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMenuAction(menuItem.text);
            }
        });
        
        return card;
    }

    private void handleMenuAction(String menuText) {
        switch (menuText) {
            case "New Student": new AddStudent(); break;
            case "New Faculty": new AddTeacher(); break;
            case "View Students": new StudentDetails(); break;
            case "View Faculty": new TeacherDetails(); break;
            case "Update Student":
                String regNo = JOptionPane.showInputDialog(this, "Enter Student Registration Number:");
                if (regNo != null && !regNo.trim().isEmpty()) new UpdateStudent(regNo.trim());
                break;
            case "Update Faculty":
                String empId = JOptionPane.showInputDialog(this, "Enter Faculty Employee ID:");
                if (empId != null && !empId.trim().isEmpty()) new UpdateTeacher(empId.trim());
                break;
            case "Student Leave": new LeaveStudent(); break;
            case "Faculty Leave": new LeaveTeacher(); break;
            case "View Marks": new ViewMarks(); break;
            case "Enter Marks":
                if (isTeacher) {
                    new TeacherEnterMarks();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Admins cannot enter marks.\n\nTeachers submit marks for approval, then approve them from 'Approve Results'.");
                    new AdminResultApproval();
                }
                break;
            case "Manage Subjects": new AdminManageSubjects(); break;
            case "Manage Courses": new AdminManageCourses(); break;
            case "Approve Results": new AdminResultApproval(); break;
            case "Grade Distribution": new AdminGradeDistribution(); break;
            case "Recheck Requests":
                if (isTeacher) {
                    new TeacherRecheckReview();
                } else {
                    new AdminRecheckApproval();
                }
                break;
            case "At-Risk Students":
                if (isTeacher) {
                    JOptionPane.showMessageDialog(this, "At-Risk dashboard is available for admins only.");
                } else {
                    new AdminAtRiskStudents();
                }
                break;
            case "Department Info": new Dept(); break;
            case "Logout":
                int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    this.dispose();
                    new RoleSelect();
                }
                break;
            default:
                JOptionPane.showMessageDialog(this, menuText + " feature will be implemented soon!", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new Project());
    }
    
    // Static helper class
    static class MenuItem {
        String text;
        String icon;
        Color accent;

        public MenuItem(String text, String icon, Color accent) {
            this.text = text;
            this.icon = icon;
            this.accent = accent;
        }
    }
}