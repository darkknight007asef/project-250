package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LeaveStudent extends JFrame implements ActionListener {

    JTextField tfregno;
    JLabel lblName, lblReg, lblDept, lblSemester, lblCgpa, lblTotalCredit, lblObtainCredit;
    JButton leave, cancel, search, print;

    public LeaveStudent() {
        setSize(700, 480);
        setLocation(350, 100);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Leave a Student");
        UITheme.styleTitle(heading);
        heading.setBounds(200, 20, 200, 30);
        add(heading);

        JLabel lblRegNoInput = new JLabel("Enter Registration No:");
        UITheme.styleLabel(lblRegNoInput);
        lblRegNoInput.setBounds(50, 70, 200, 25);
        add(lblRegNoInput);

        tfregno = new JTextField();
        UITheme.styleField(tfregno);
        tfregno.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfregno.setBounds(220, 70, 170, 36);
        add(tfregno);

        search = new JButton("Search");
        UITheme.stylePrimary(search);
        search.setBounds(410, 70, 110, 36);
        search.addActionListener(this);
        add(search);

        lblName = new JLabel("Name: ");
        UITheme.styleLabel(lblName);
        lblName.setBounds(50, 120, 500, 25);
        add(lblName);

        lblReg = new JLabel("Registration No: ");
        UITheme.styleLabel(lblReg);
        lblReg.setBounds(50, 150, 500, 25);
        add(lblReg);

        lblDept = new JLabel("Dept: ");
        UITheme.styleLabel(lblDept);
        lblDept.setBounds(50, 180, 500, 25);
        add(lblDept);

        lblSemester = new JLabel("Current Semester: ");
        UITheme.styleLabel(lblSemester);
        lblSemester.setBounds(50, 210, 500, 25);
        add(lblSemester);

        lblCgpa = new JLabel("CGPA: ");
        UITheme.styleLabel(lblCgpa);
        lblCgpa.setBounds(50, 240, 500, 25);
        add(lblCgpa);

        lblTotalCredit = new JLabel("Total Credit: ");
        UITheme.styleLabel(lblTotalCredit);
        lblTotalCredit.setBounds(50, 270, 500, 25);
        add(lblTotalCredit);

        lblObtainCredit = new JLabel("Obtain Credit: ");
        UITheme.styleLabel(lblObtainCredit);
        lblObtainCredit.setBounds(50, 300, 500, 25);
        add(lblObtainCredit);

        leave = new JButton("Leave");
        UITheme.stylePrimary(leave);
        leave.setBounds(50, 340, 100, 30);
        leave.addActionListener(this);
        add(leave);

        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(170, 340, 100, 30);
        cancel.addActionListener(this);
        add(cancel);

        print = new JButton("Print");
        UITheme.styleGhost(print);
        print.setBounds(290, 340, 100, 30);
        print.addActionListener(this);
        add(print);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == search) {
            String regno = tfregno.getText();
            try {
                Conn con = new Conn();

                String query = "SELECT s.name, s.registration_no, s.branch, ss.current_semester " +
                               "FROM student s JOIN student_semester ss ON s.registration_no = ss.registration_no " +
                               "WHERE s.registration_no = '"+regno+"'";
                ResultSet rs = con.s.executeQuery(query);

                if(rs.next()) {
                    String name = rs.getString("name");
                    String branch = rs.getString("branch");
                    int sem = rs.getInt("current_semester");

                    ResultSet rsCredit = con.s.executeQuery("SELECT total_credit, sem"+sem+"_credit AS semCredit FROM department_credit WHERE dept='"+branch+"'");
                    int totalCredit = 0;
                    int obtainCredit = 0;
                    if(rsCredit.next()) {
                        totalCredit = rsCredit.getInt("total_credit");
                        obtainCredit = rsCredit.getInt("semCredit")*(sem-1);
                    }

                    lblName.setText("Name: "+name);
                    lblReg.setText("Registration No: "+regno);
                    lblDept.setText("Dept: "+branch);
                    lblSemester.setText("Current Semester: "+sem);
                    lblCgpa.setText("CGPA: N/A");
                    lblTotalCredit.setText("Total Credit: "+totalCredit);
                    lblObtainCredit.setText("Obtain Credit: "+obtainCredit);

                } else {
                    JOptionPane.showMessageDialog(null, "No Student Found with Registration No: "+regno);
                }

            } catch(Exception e) {
                e.printStackTrace();
            }

        } else if (ae.getSource() == leave) {
            String regno = tfregno.getText() == null ? "" : tfregno.getText().trim();
            if (regno.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a registration number.");
                return;
            }
            try {
                Conn con = new Conn();
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this student?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION){
                    try {
                        con.c.setAutoCommit(false);
                        // Delete child records first to satisfy FK constraints
                        try (PreparedStatement psMarks = con.c.prepareStatement("DELETE FROM student_marks WHERE registration_no=?");
                             PreparedStatement psSem = con.c.prepareStatement("DELETE FROM student_semester WHERE registration_no=?");
                             PreparedStatement psStudent = con.c.prepareStatement("DELETE FROM student WHERE registration_no=?")) {
                            psMarks.setString(1, regno);
                            psMarks.executeUpdate();

                            psSem.setString(1, regno);
                            psSem.executeUpdate();

                            psStudent.setString(1, regno);
                            int deleted = psStudent.executeUpdate();

                            if (deleted > 0) {
                                con.c.commit();
                                JOptionPane.showMessageDialog(this, "Student removed successfully.");
                                setVisible(false);
                            } else {
                                con.c.rollback();
                                JOptionPane.showMessageDialog(this, "No student found with this registration number.");
                            }
                        }
                    } catch (Exception exTx) {
                        try { con.c.rollback(); } catch (Exception ignore) {}
                        throw exTx;
                    } finally {
                        try { con.c.setAutoCommit(true); } catch (Exception ignore) {}
                    }
                }
                
            } catch(Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error removing student: " + e.getMessage());
            }

        } else if(ae.getSource() == print) {
            try {
                String info = lblName.getText() + "\n" +
                              lblReg.getText() + "\n" +
                              lblDept.getText() + "\n" +
                              lblSemester.getText() + "\n" +
                              lblCgpa.getText() + "\n" +
                              lblTotalCredit.getText() + "\n" +
                              lblObtainCredit.getText();
                JTextArea textArea = new JTextArea(info);
                textArea.print();
            } catch(Exception e) {
                e.printStackTrace();
            }

        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new LeaveStudent();
    }
}
