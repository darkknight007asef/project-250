package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LeaveTeacher extends JFrame implements ActionListener {

    JComboBox<String> cbDept;
    JTextField tfPhone;
    JLabel lblName, lblEmpId, lblDept, lblPhone, lblEmail;
    JButton search, leave, cancel, print;

    public LeaveTeacher() {
        setSize(740, 420);
        setLocation(350, 100);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Leave a Teacher");
        UITheme.styleTitle(heading);
        heading.setBounds(250, 20, 300, 30);
        add(heading);

        JLabel lblDeptInput = new JLabel("Department:");
        UITheme.styleLabel(lblDeptInput);
        lblDeptInput.setBounds(50, 70, 120, 25);
        add(lblDeptInput);

        String[] deptArray = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        cbDept = new JComboBox<>(deptArray);
        cbDept.setBounds(180, 70, 170, 36);
        cbDept.setBackground(Color.WHITE);
        cbDept.setForeground(Color.BLACK);
        cbDept.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbDept.setBorder(BorderFactory.createLineBorder(new Color(230,232,240), 1, true));
        add(cbDept);

        JLabel lblPhoneInput = new JLabel("Phone:");
        UITheme.styleLabel(lblPhoneInput);
        lblPhoneInput.setBounds(50, 110, 120, 25);
        add(lblPhoneInput);

        tfPhone = new JTextField();
        UITheme.styleField(tfPhone);
        tfPhone.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfPhone.setBounds(180, 110, 170, 36);
        add(tfPhone);

        search = new JButton("Search");
        UITheme.stylePrimary(search);
        search.setBounds(370, 110, 110, 36);
        search.addActionListener(this);
        add(search);

        lblName = new JLabel("Name: ");
        UITheme.styleLabel(lblName);
        lblName.setBounds(50, 150, 600, 25);
        add(lblName);

        lblEmpId = new JLabel("Emp ID: ");
        UITheme.styleLabel(lblEmpId);
        lblEmpId.setBounds(50, 180, 600, 25);
        add(lblEmpId);

        lblDept = new JLabel("Department: ");
        UITheme.styleLabel(lblDept);
        lblDept.setBounds(50, 210, 600, 25);
        add(lblDept);

        lblPhone = new JLabel("Phone: ");
        UITheme.styleLabel(lblPhone);
        lblPhone.setBounds(50, 240, 600, 25);
        add(lblPhone);

        lblEmail = new JLabel("Email: ");
        UITheme.styleLabel(lblEmail);
        lblEmail.setBounds(50, 270, 600, 25);
        add(lblEmail);

        leave = new JButton("Leave");
        UITheme.stylePrimary(leave);
        leave.setBounds(50, 310, 100, 30);
        leave.addActionListener(this);
        add(leave);

        print = new JButton("Print");
        UITheme.styleGhost(print);
        print.setBounds(170, 310, 100, 30);
        print.addActionListener(this);
        add(print);

        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(290, 310, 100, 30);
        cancel.addActionListener(this);
        add(cancel);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == search) {
            String dept = (String) cbDept.getSelectedItem();
            String phone = tfPhone.getText().trim();

            try {
                Conn con = new Conn();
                // Case-insensitive search
                String query = "SELECT name, empId, department, phone, email FROM teacher " +
                               "WHERE LOWER(department) = LOWER('" + dept + "') AND phone = '" + phone + "'";
                ResultSet rs = con.s.executeQuery(query);

                if (rs.next()) {
                    lblName.setText("Name: " + rs.getString("name"));
                    lblEmpId.setText("Emp ID: " + rs.getString("empId"));
                    lblDept.setText("Department: " + rs.getString("department"));
                    lblPhone.setText("Phone: " + rs.getString("phone"));
                    lblEmail.setText("Email: " + rs.getString("email"));
                } else {
                    JOptionPane.showMessageDialog(null, "No teacher found with this Department & Phone.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ae.getSource() == leave) {
            String dept = (String) cbDept.getSelectedItem();
            String phone = tfPhone.getText().trim();
            try {
                Conn con = new Conn();
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this teacher?");
                if (confirm == JOptionPane.YES_OPTION) {
                    int deleted = con.s.executeUpdate("DELETE FROM teacher WHERE LOWER(department) = LOWER('" + dept + "') AND phone = '" + phone + "'");
                    if (deleted > 0) {
                        JOptionPane.showMessageDialog(null, "Teacher removed successfully.");
                        setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(null, "No teacher found to remove.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ae.getSource() == print) {
            try {
                String info = lblName.getText() + "\n" +
                              lblEmpId.getText() + "\n" +
                              lblDept.getText() + "\n" +
                              lblPhone.getText() + "\n" +
                              lblEmail.getText();
                JTextArea textArea = new JTextArea(info);
                boolean printed = textArea.print();
                if (!printed) {
                    JOptionPane.showMessageDialog(null, "Printing cancelled.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new LeaveTeacher();
    }
}
