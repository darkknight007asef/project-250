package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import com.toedter.calendar.JDateChooser;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddTeacher extends JFrame implements ActionListener {

    JTextField tfname, tffname, tfaddress, tfphone, tfemail, tfbscSub, tfmscSub, tfcgpaBsc, tfcgpaMsc;
    JLabel labelempId;
    JDateChooser dcdob;
    JComboBox<String> cbPHD, cbDepartment, cbPosition;
    JButton submit, cancel;

    private JButton btnUploadPhoto;
    private JLabel lblPhotoPreview;
    private String selectedPhotoPath;

    Random ran = new Random();
    long first4 = Math.abs((ran.nextLong() % 9000L) + 1000L);

    public AddTeacher() {
        setSize(900, 750);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("New Teacher Details");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 30, 900, 50);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(heading);

        JLabel lblname = new JLabel("Name");
        UITheme.styleLabel(lblname);
        lblname.setBounds(50, 150, 100, 30);
        add(lblname);

        tfname = new JTextField();
        UITheme.styleField(tfname);
        tfname.setBounds(200, 150, 150, 36);
        tfname.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfname);

        JLabel lblfname = new JLabel("Father's Name");
        UITheme.styleLabel(lblfname);
        lblfname.setBounds(450, 150, 200, 30);
        add(lblfname);

        tffname = new JTextField();
        UITheme.styleField(tffname);
        tffname.setBounds(650, 150, 150, 36);
        tffname.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tffname);

        JLabel lblempIdLabel = new JLabel("Employee Id");
        UITheme.styleLabel(lblempIdLabel);
        lblempIdLabel.setBounds(50, 200, 200, 30);
        add(lblempIdLabel);

        labelempId = new JLabel("101" + first4);
        UITheme.styleLabel(labelempId);
        labelempId.setBounds(200, 200, 200, 30);
        add(labelempId);

        JLabel lbldob = new JLabel("Date of Birth");
        UITheme.styleLabel(lbldob);
        lbldob.setBounds(450, 200, 200, 30);
        add(lbldob);

        dcdob = new JDateChooser();
        dcdob.setBounds(650, 200, 150, 36);
        add(dcdob);
        JTextField dobField = (JTextField) dcdob.getDateEditor().getUiComponent();
        UITheme.styleField(dobField);
        dobField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel lbladdress = new JLabel("Address");
        UITheme.styleLabel(lbladdress);
        lbladdress.setBounds(50, 250, 200, 30);
        add(lbladdress);

        tfaddress = new JTextField();
        UITheme.styleField(tfaddress);
        tfaddress.setBounds(200, 250, 150, 36);
        tfaddress.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfaddress);

        JLabel lblphone = new JLabel("Phone");
        UITheme.styleLabel(lblphone);
        lblphone.setBounds(450, 250, 200, 30);
        add(lblphone);

        tfphone = new JTextField();
        UITheme.styleField(tfphone);
        tfphone.setBounds(650, 250, 150, 36);
        tfphone.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfphone);

        JLabel lblemail = new JLabel("Email Id");
        UITheme.styleLabel(lblemail);
        lblemail.setBounds(50, 300, 200, 30);
        add(lblemail);

        tfemail = new JTextField();
        UITheme.styleField(tfemail);
        tfemail.setBounds(200, 300, 150, 36);
        tfemail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfemail);

        JLabel lblbscSub = new JLabel("BSc in Subject");
        UITheme.styleLabel(lblbscSub);
        lblbscSub.setBounds(450, 300, 200, 30);
        add(lblbscSub);

        tfbscSub = new JTextField();
        UITheme.styleField(tfbscSub);
        tfbscSub.setBounds(650, 300, 150, 36);
        tfbscSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfbscSub);

        JLabel lblmscSub = new JLabel("MSc in Subject");
        UITheme.styleLabel(lblmscSub);
        lblmscSub.setBounds(50, 350, 200, 30);
        add(lblmscSub);

        tfmscSub = new JTextField();
        UITheme.styleField(tfmscSub);
        tfmscSub.setBounds(200, 350, 150, 36);
        tfmscSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfmscSub);

        JLabel lblPhoto = new JLabel("Photo");
        UITheme.styleLabel(lblPhoto);
        lblPhoto.setBounds(450, 350, 200, 30);
        add(lblPhoto);

        btnUploadPhoto = new JButton("Upload Photo");
        UITheme.styleGhost(btnUploadPhoto);
        btnUploadPhoto.setBounds(650, 350, 150, 36);
        btnUploadPhoto.addActionListener(this);
        add(btnUploadPhoto);

        lblPhotoPreview = new JLabel();
        lblPhotoPreview.setBounds(810, 330, 80, 80);
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 240), 1, true));
        add(lblPhotoPreview);

        JLabel lblcgpaBsc = new JLabel("CGPA in BSc");
        UITheme.styleLabel(lblcgpaBsc);
        lblcgpaBsc.setBounds(50, 400, 200, 30);
        add(lblcgpaBsc);

        tfcgpaBsc = new JTextField();
        UITheme.styleField(tfcgpaBsc);
        tfcgpaBsc.setBounds(200, 400, 150, 36);
        tfcgpaBsc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfcgpaBsc);

        JLabel lblcgpaMsc = new JLabel("CGPA in MSc");
        UITheme.styleLabel(lblcgpaMsc);
        lblcgpaMsc.setBounds(450, 400, 200, 30);
        add(lblcgpaMsc);

        tfcgpaMsc = new JTextField();
        UITheme.styleField(tfcgpaMsc);
        tfcgpaMsc.setBounds(650, 400, 150, 36);
        tfcgpaMsc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(tfcgpaMsc);

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
        lblDepartment.setBounds(450, 450, 200, 30);
        add(lblDepartment);

        String departments[] = {"CSE","EEE","SWE","MATH","PHY","CHE","GEO","GE","BMB","CEP","ME","CE","FET","BAN","ENG","ANP","PAD","SOC"};
        cbDepartment = new JComboBox<>(departments);
        cbDepartment.setBounds(650, 450, 150, 36);
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

        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            String name = tfname.getText();
            String fname = tffname.getText();
            String empId = labelempId.getText();
            String dob = ((JTextField) dcdob.getDateEditor().getUiComponent()).getText();
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
                String storedPhotoPath = "";
                if (selectedPhotoPath != null && !selectedPhotoPath.trim().isEmpty()) {
                    storedPhotoPath = copyPhotoToLocalDir(selectedPhotoPath, empId, "teachers");
                }

                String query = "INSERT INTO teacher(name, fname, empId, dob, address, phone, email, bsc_in_sub, msc_in_sub, cgpa_in_bsc, cgpa_in_msc, PHD, department, position, photo_path) " +
                               "VALUES('"+name+"', '"+fname+"', '"+empId+"', '"+dob+"', '"+address+"', '"+phone+"', '"+email+"', '"+bscSub+"', '"+mscSub+"', '"+cgpaBsc+"', '"+cgpaMsc+"', '"+phd+"', '"+department+"', '"+position+"', '" + storedPhotoPath + "')";

                Conn con = new Conn();
                con.s.executeUpdate(query);

                JOptionPane.showMessageDialog(null, "Teacher Details Inserted Successfully");
                setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ae.getSource() == btnUploadPhoto) {
            choosePhoto();
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
        new AddTeacher();
    }
}
