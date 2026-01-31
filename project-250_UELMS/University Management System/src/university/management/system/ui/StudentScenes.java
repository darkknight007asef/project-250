package university.management.system.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import university.management.system.Conn;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

public final class StudentScenes {
    private StudentScenes() {}

    public static void showList(JFrame frame) {
        frame.setContentPane(buildListPanel(frame));
        frame.setSize(1100, 720);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    public static void showAdd(JFrame frame) {
        frame.setContentPane(buildAddPanel(frame));
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel buildListPanel(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField search = new JTextField(16);
        JButton searchBtn = new JButton("Search");
        JButton filterBtn = new JButton("Filter Dept");
        JButton addBtn = new JButton("Add");
        JButton back = new JButton("Back");
        top.add(search); top.add(searchBtn); top.add(filterBtn); top.add(addBtn); top.add(back);
        root.add(top, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        Runnable loadAll = () -> loadTable(table, "SELECT name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path FROM student");
        loadAll.run();

        searchBtn.addActionListener(e -> {
            String reg = search.getText().trim();
            if (reg.isEmpty()) {
                loadAll.run();
            } else {
                loadTable(table, "SELECT name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path FROM student WHERE registration_no='" + reg + "'");
            }
        });

        filterBtn.addActionListener(e -> {
            Object sel = JOptionPane.showInputDialog(frame, "Select Department", "Filter", JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"},
                    "CSE");
            if (sel != null) loadTable(table, "SELECT name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path FROM student WHERE branch='" + sel + "'");
        });

        addBtn.addActionListener(e -> showAdd(frame));
        back.addActionListener(e -> SceneFactory.showDashboard(frame));

        return root;
    }

    private static void loadTable(JTable table, String sql) {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= cols; i++) colNames.add(md.getColumnName(i));
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
                data.add(row);
            }
            table.setModel(new DefaultTableModel(data, colNames));
        } catch (Exception ex) {
            Dialogs.error("Load failed: " + ex.getMessage());
        }
    }

    private static JPanel buildAddPanel(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JTextField name = new JTextField(18);
        JTextField fname = new JTextField(18);
        JTextField dob = new JTextField(18);
        JTextField address = new JTextField(18);
        JTextField phone = new JTextField(18);
        JTextField email = new JTextField(18);
        JTextField x = new JTextField(18);
        JTextField xii = new JTextField(18);
        JComboBox<String> course = new JComboBox<>(new String[]{"B.Tech", "BBA", "BCA", "Bsc", "Msc", "MBA", "MCA", "MCom", "MA", "BA"});
        JComboBox<String> branch = new JComboBox<>(new String[]{"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"});

        addRow(form, gbc, "Name", name);
        addRow(form, gbc, "Father's Name", fname);
        addRow(form, gbc, "DOB", dob);
        addRow(form, gbc, "Address", address);
        addRow(form, gbc, "Phone", phone);
        addRow(form, gbc, "Email", email);
        addRow(form, gbc, "Class X (%)", x);
        addRow(form, gbc, "Class XII (Year)", xii);
        addRow(form, gbc, "Course", course);
        addRow(form, gbc, "Branch", branch);

        JPanel actions = new JPanel();
        JButton submit = new JButton("Submit");
        JButton cancel = new JButton("Cancel");
        actions.add(submit);
        actions.add(cancel);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        submit.addActionListener(e -> AddStudentLogic.insert(
                name.getText(), fname.getText(), dob.getText(), address.getText(), phone.getText(),
                email.getText(), x.getText(), xii.getText(),
                course.getSelectedItem() == null ? null : course.getSelectedItem().toString(),
                branch.getSelectedItem() == null ? null : branch.getSelectedItem().toString(),
                ""
        ));
        cancel.addActionListener(e -> SceneFactory.showDashboard(frame));

        return root;
    }

    private static void addRow(JPanel form, GridBagConstraints gbc, String label, java.awt.Component field) {
        gbc.gridx = 0; form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(field, gbc);
        gbc.gridy++;
        gbc.weightx = 0;
    }
}


