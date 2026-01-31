package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UpdateStudent extends JFrame implements ActionListener {

    private final String regNo;

    JTextField tfname, tffname, tfdob, tfaddress, tfphone, tfemail, tfx, tfxii;
    JComboBox<String> cbcourse, cbbranch;
    JButton submit, cancel;

    private JButton btnUploadPhoto;
    private JLabel lblPhotoPreview;
    private String selectedPhotoPath;
    private String existingPhotoPath;

    public UpdateStudent(String regNo) {
        this.regNo = regNo.trim();

        setSize(900, 700);
        setLocation(350, 50);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Update Student Details");
        UITheme.styleTitle(heading);
        heading.setBounds(310, 30, 500, 50);
        add(heading);

        tfname = addLabelAndText("Name", 50, 150, 200, 150);
        tffname = addLabelAndText("Father's Name", 400, 150, 600, 150);
        addLabel("Date of Birth", 50, 200, 200, 30);
        tfdob = new JTextField();
        UITheme.styleField(tfdob);
        tfdob.setBounds(200, 200, 150, 36);
        tfdob.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfdob);
        tfaddress = addLabelAndText("Address", 400, 200, 600, 200);
        tfphone = addLabelAndText("Phone", 50, 250, 200, 250);
        tfemail = addLabelAndText("Email Id", 400, 250, 600, 250);
        tfx = addLabelAndText("Class X (%)", 50, 300, 200, 300);
        tfxii = addLabelAndText("Class XII (Year)", 400, 300, 600, 300);

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
        lblcourse.setBounds(400, 350, 200, 30);
        add(lblcourse);

        String[] courses = {"B.Tech", "BBA", "BCA", "Bsc", "Msc", "MBA", "MCA", "MCom", "MA", "BA"};
        cbcourse = new JComboBox<>(courses);
        cbcourse.setBounds(600, 350, 150, 36);
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

        loadStudent();

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

    private void loadStudent() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT name, fname, registration_no, dob, address, phone, email, class_x, class_xii, course, branch, photo_path FROM student WHERE registration_no='" + regNo + "'");
            if (rs.next()) {
                tfname.setText(rs.getString("name"));
                tffname.setText(rs.getString("fname"));
                tfdob.setText(rs.getString("dob"));
                tfaddress.setText(rs.getString("address"));
                tfphone.setText(rs.getString("phone"));
                tfemail.setText(rs.getString("email"));
                tfx.setText(rs.getString("class_x"));
                tfxii.setText(rs.getString("class_xii"));
                cbcourse.setSelectedItem(rs.getString("course"));
                cbbranch.setSelectedItem(rs.getString("branch"));

                existingPhotoPath = rs.getString("photo_path");
                if (existingPhotoPath != null && !existingPhotoPath.trim().isEmpty()) {
                    try {
                        ImageIcon icon = new ImageIcon(existingPhotoPath);
                        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        lblPhotoPreview.setIcon(new ImageIcon(img));
                    } catch (Exception ignore) {
                        lblPhotoPreview.setIcon(null);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No student found for: " + regNo);
                dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load: " + e.getMessage());
            dispose();
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == btnUploadPhoto) {
            choosePhoto();
        } else if (ae.getSource() == submit) {
            String name = tfname.getText();
            String fname = tffname.getText();
            String dob = tfdob.getText();
            String address = tfaddress.getText();
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String x = tfx.getText();
            String xii = tfxii.getText();
            String course = (String) cbcourse.getSelectedItem();
            String branch = (String) cbbranch.getSelectedItem();
            try {
                Conn c = new Conn();
                String storedPhotoPath = (existingPhotoPath == null ? "" : existingPhotoPath);
                if (selectedPhotoPath != null && !selectedPhotoPath.trim().isEmpty()) {
                    storedPhotoPath = copyPhotoToLocalDir(selectedPhotoPath, regNo, "students");
                }

                String q = "UPDATE student SET name='"+name+"', fname='"+fname+"', dob='"+dob+"', address='"+address+"', phone='"+phone+"', email='"+email+"', class_x='"+x+"', class_xii='"+xii+"', course='"+course+"', branch='"+branch+"', photo_path='"+storedPhotoPath+"' WHERE registration_no='"+regNo+"'";
                int updated = c.s.executeUpdate(q);
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Student updated successfully");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No changes made");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update: " + e.getMessage());
            }
        } else if (ae.getSource() == cancel) {
            dispose();
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
            return (existingPhotoPath == null ? "" : existingPhotoPath);
        }
    }
}
