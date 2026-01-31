package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import com.toedter.calendar.JDateChooser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddStudent extends JFrame implements ActionListener {

    JTextField tfname, tffname, tfaddress, tfphone, tfemail, tfx, tfxii;
    JDateChooser dcdob;
    JComboBox<String> cbcourse, cbbranch;
    JButton submit, cancel;

    private JButton btnUploadPhoto;
    private JLabel lblPhotoPreview;
    private String selectedPhotoPath;

    AddStudent() {
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("New Student Details");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 30, 900, 50);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(heading);

        tfname = addLabelAndText("Name", 50, 150, 200, 150);
        tffname = addLabelAndText("Father's Name", 450, 150, 650, 150);
        dcdob = new JDateChooser();
        addLabel("Date of Birth", 50, 200, 200, 30);
        dcdob.setBounds(200, 200, 150, 36);
        add(dcdob);
        JTextField dobField = (JTextField) dcdob.getDateEditor().getUiComponent();
        UITheme.styleField(dobField);
        dobField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tfaddress = addLabelAndText("Address", 450, 200, 650, 200);
        tfphone = addLabelAndText("Phone", 50, 250, 200, 250);
        tfemail = addLabelAndText("Email Id", 450, 250, 650, 250);
        tfx = addLabelAndText("SSC (%)", 50, 300, 200, 300);
        tfxii = addLabelAndText("HSC (Year)", 450, 300, 650, 300);

        // Photo upload
        JLabel lblPhoto = new JLabel("Photo");
        UITheme.styleLabel(lblPhoto);
        lblPhoto.setBounds(50, 350, 200, 30);
        add(lblPhoto);

        btnUploadPhoto = new JButton("Upload Photo");
        UITheme.styleGhost(btnUploadPhoto);
        btnUploadPhoto.setBounds(200, 350, 150, 36);
        btnUploadPhoto.addActionListener(this);
        add(btnUploadPhoto);

        lblPhotoPreview = new JLabel();
        lblPhotoPreview.setBounds(360, 330, 80, 80);
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
        add(lblPhotoPreview);

        JLabel lblcourse = new JLabel("Course");
        UITheme.styleLabel(lblcourse);
        lblcourse.setBounds(450, 350, 200, 30);
        add(lblcourse);

        String[] courses = {"B.Tech", "BBA", "BCA", "Bsc", "Msc", "MBA", "MCA", "MCom", "MA", "BA"};
        cbcourse = new JComboBox<>(courses);
        cbcourse.setBounds(650, 350, 150, 36);
        cbcourse.setBackground(Color.WHITE);
        cbcourse.setForeground(Color.BLACK);
        cbcourse.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
        cbcourse.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(cbcourse);

        JLabel lblbranch = new JLabel("Branch");
        UITheme.styleLabel(lblbranch);
        lblbranch.setBounds(50, 400, 200, 30);
        add(lblbranch);

        String[] branches = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        cbbranch = new JComboBox<>(branches);
        cbbranch.setBounds(200, 400, 150, 36);
        cbbranch.setBackground(Color.WHITE);
        cbbranch.setForeground(Color.BLACK);
        cbbranch.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
        cbbranch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(cbbranch);

        submit = new JButton("Submit");
        UITheme.stylePrimary(submit);
        submit.setBounds(250, 550, 120, 30);
        submit.addActionListener(this);
        add(submit);

        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(450, 550, 120, 30);
        cancel.addActionListener(this);
        add(cancel);

        setVisible(true);
    }

    private JTextField addLabelAndText(String text, int lx, int ly, int tx, int ty) {
        addLabel(text, lx, ly, 200, 30);
        JTextField tf = new JTextField();
        UITheme.styleField(tf);
        tf.setBounds(tx, ty, 150, 36);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tf);
        return tf;
    }

    private void addLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        UITheme.styleLabel(lbl);
        lbl.setBounds(x, y, w, h);
        add(lbl);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == btnUploadPhoto) {
            choosePhoto();
        } else if (ae.getSource() == submit) {
            String name = tfname.getText();
            String fname = tffname.getText();
            String dob = ((JTextField) dcdob.getDateEditor().getUiComponent()).getText();
            String address = tfaddress.getText();
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String x = tfx.getText();
            String xii = tfxii.getText();
            String course = (String) cbcourse.getSelectedItem();
            String branch = (String) cbbranch.getSelectedItem();

            try {
                Conn con = new Conn();

                // Map branch to department code
                HashMap<String, String> deptCode = new HashMap<>();
                deptCode.put("CSE", "331"); deptCode.put("EEE", "332"); deptCode.put("SWE", "333");
                deptCode.put("MATH", "334"); deptCode.put("PHY", "335"); deptCode.put("CHE", "336");
                deptCode.put("ME", "337"); deptCode.put("CE", "338"); deptCode.put("PAD", "321");
                deptCode.put("ENG", "432"); deptCode.put("BAN", "121"); deptCode.put("SOC", "545");
                deptCode.put("GE", "231"); deptCode.put("BMB", "878");

                String dept = deptCode.get(branch);

                // Count existing students for same year & branch
                String countQuery = "SELECT COUNT(*) FROM student WHERE class_xii='"+xii+"' AND branch='"+branch+"'";
                ResultSet rs = con.s.executeQuery(countQuery);
                int count = 0;
                if (rs.next()) count = rs.getInt(1);

                String rollFormatted = String.format("%03d", count + 1);
                String registrationNo = xii + "-" + dept + "-" + rollFormatted;

                String storedPhotoPath = "";
                if (selectedPhotoPath != null && !selectedPhotoPath.trim().isEmpty()) {
                    storedPhotoPath = copyPhotoToLocalDir(selectedPhotoPath, registrationNo, "students");
                }

                // Insert into student table
                String studentQuery = "INSERT INTO student (name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path) " +
                        "VALUES('" + name + "', '" + fname + "', '" + registrationNo + "', '" + dob + "', '" + address + "', '" + phone + "', '" + email + "', '" + x + "', '" + xii + "', '" + course + "', '" + branch + "', '" + storedPhotoPath + "')";
                con.s.executeUpdate(studentQuery);

                // Auto-fill student_semester table
                String semesterQuery = "INSERT INTO student_semester(registration_no, dept) VALUES('" + registrationNo + "', '" + branch + "')";
                con.s.executeUpdate(semesterQuery);

                JOptionPane.showMessageDialog(null, "Student Added Successfully\nRegistration No: " + registrationNo);
                setVisible(false);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error adding student: " + e.getMessage());
            }

        } else {
            setVisible(false);
        }
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Photo");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedPhotoPath = chooser.getSelectedFile().getAbsolutePath();
            try {
                ImageIcon icon = new ImageIcon(selectedPhotoPath);
                Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                lblPhotoPreview.setIcon(new ImageIcon(img));
            } catch (Exception ignore) {
                lblPhotoPreview.setIcon(null);
            }
        }
    }

    private String copyPhotoToLocalDir(String sourcePath, String id, String typeFolder) {
        try {
            Path src = Paths.get(sourcePath);
            String fileName = src.getFileName().toString();
            String ext = "";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0 && dot < fileName.length() - 1) {
                ext = fileName.substring(dot);
            }

            Path dir = Paths.get("photos", typeFolder);
            Files.createDirectories(dir);

            Path dst = dir.resolve(id + ext);
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            return dst.toString().replace('\\', '/');
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        new AddStudent();
    }
}
