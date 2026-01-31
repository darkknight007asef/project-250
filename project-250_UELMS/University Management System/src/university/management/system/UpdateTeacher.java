package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UpdateTeacher extends JFrame implements ActionListener {

    private final String empId;

    JTextField tfname, tffname, tfdob, tfaddress, tfphone, tfemail, tfbscSub, tfmscSub, tfcgpaBsc, tfcgpaMsc;
    JComboBox<String> cbPHD, cbDepartment, cbPosition;
    JButton submit, cancel;

    private JButton btnUploadPhoto;
    private JLabel lblPhotoPreview;
    private String selectedPhotoPath;
    private String existingPhotoPath;

    public UpdateTeacher(String empId) {
        this.empId = empId.trim();

        setSize(900, 750);
        setLocation(350, 50);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Update Teacher Details");
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

        tfbscSub = addLabelAndText("BSc in Subject", 400, 300, 600, 300);
        tfmscSub = addLabelAndText("MSc in Subject", 50, 350, 200, 350);

        JLabel lblPhoto = new JLabel("Photo");
        UITheme.styleLabel(lblPhoto);
        lblPhoto.setBounds(400, 350, 200, 30);
        add(lblPhoto);

        btnUploadPhoto = new JButton("Upload Photo");
        UITheme.styleGhost(btnUploadPhoto);
        btnUploadPhoto.setBounds(600, 350, 150, 36);
        btnUploadPhoto.addActionListener(this);
        add(btnUploadPhoto);

        lblPhotoPreview = new JLabel();
        lblPhotoPreview.setBounds(760, 330, 80, 80);
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
        add(lblPhotoPreview);
        tfcgpaBsc = addLabelAndText("CGPA in BSc", 50, 400, 200, 400);
        tfcgpaMsc = addLabelAndText("CGPA in MSc", 400, 400, 600, 400);

        JLabel lblPHD = new JLabel("PHD Status");
        UITheme.styleLabel(lblPHD);
        lblPHD.setBounds(50, 450, 200, 30);
        add(lblPHD);
        String phdOptions[] = {"none", "done", "running"};
        cbPHD = new JComboBox<>(phdOptions);
        cbPHD.setBounds(200, 450, 150, 36);
        cbPHD.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbPHD.setBackground(Color.WHITE);
        add(cbPHD);

        JLabel lblDepartment = new JLabel("Department");
        UITheme.styleLabel(lblDepartment);
        lblDepartment.setBounds(400, 450, 200, 30);
        add(lblDepartment);
        String departments[] = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        cbDepartment = new JComboBox<>(departments);
        cbDepartment.setBounds(600, 450, 150, 36);
        cbDepartment.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbDepartment.setBackground(Color.WHITE);
        add(cbDepartment);

        JLabel lblPosition = new JLabel("Position");
        UITheme.styleLabel(lblPosition);
        lblPosition.setBounds(50, 500, 200, 30);
        add(lblPosition);
        String positions[] = {"Lecturer", "Professor", "Assistant Professor", "Associate Professor"};
        cbPosition = new JComboBox<>(positions);
        cbPosition.setBounds(200, 500, 150, 36);
        cbPosition.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbPosition.setBackground(Color.WHITE);
        add(cbPosition);

        submit = new JButton("Submit");
        UITheme.stylePrimary(submit);
        submit.setBounds(250, 600, 120, 30);
        submit.addActionListener(this);
        add(submit);

        cancel = new JButton("Cancel");
        UITheme.styleGhost(cancel);
        cancel.setBounds(450, 600, 120, 30);
        cancel.addActionListener(this);
        add(cancel);

        loadTeacher();

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

    private void loadTeacher() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT name, fname, empId, dob, address, phone, email, bsc_in_sub, msc_in_sub, cgpa_in_bsc, cgpa_in_msc, phd, department, position, photo_path FROM teacher WHERE empId='" + empId + "'");
            if (rs.next()) {
                tfname.setText(rs.getString("name"));
                tffname.setText(rs.getString("fname"));
                tfdob.setText(rs.getString("dob"));
                tfaddress.setText(rs.getString("address"));
                tfphone.setText(rs.getString("phone"));
                tfemail.setText(rs.getString("email"));
                tfbscSub.setText(rs.getString("bsc_in_sub"));
                tfmscSub.setText(rs.getString("msc_in_sub"));
                tfcgpaBsc.setText(rs.getString("cgpa_in_bsc"));
                tfcgpaMsc.setText(rs.getString("cgpa_in_msc"));
                cbPHD.setSelectedItem(rs.getString("PHD"));
                cbDepartment.setSelectedItem(rs.getString("department"));
                cbPosition.setSelectedItem(rs.getString("position"));

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
                JOptionPane.showMessageDialog(this, "No teacher found for: " + empId);
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
            String bscSub = tfbscSub.getText();
            String mscSub = tfmscSub.getText();
            String cgpaBsc = tfcgpaBsc.getText();
            String cgpaMsc = tfcgpaMsc.getText();
            String phd = (String) cbPHD.getSelectedItem();
            String department = (String) cbDepartment.getSelectedItem();
            String position = (String) cbPosition.getSelectedItem();
            try {
                Conn c = new Conn();
                String storedPhotoPath = (existingPhotoPath == null ? "" : existingPhotoPath);
                if (selectedPhotoPath != null && !selectedPhotoPath.trim().isEmpty()) {
                    storedPhotoPath = copyPhotoToLocalDir(selectedPhotoPath, empId, "teachers");
                }

                String q = "UPDATE teacher SET name='"+name+"', fname='"+fname+"', dob='"+dob+"', address='"+address+"', phone='"+phone+"', email='"+email+"', bsc_in_sub='"+bscSub+"', msc_in_sub='"+mscSub+"', cgpa_in_bsc='"+cgpaBsc+"', cgpa_in_msc='"+cgpaMsc+"', PHD='"+phd+"', department='"+department+"', position='"+position+"', photo_path='"+storedPhotoPath+"' WHERE empId='"+empId+"'";
                int updated = c.s.executeUpdate(q);
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Teacher updated successfully");
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
