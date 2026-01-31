package university.management.system.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;

public class ProjectView {
    private final JPanel root = new JPanel(new BorderLayout());
    private final JFrame frame;

    public ProjectView(JFrame frame) {
        this.frame = frame;

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
        grid.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JButton students = pill("Students");
        JButton addStudent = pill("Add Student");
        JButton teachers = pill("Teachers");
        JButton addTeacher = pill("Add Teacher");
        JButton marks = pill("Enter Marks");
        JButton dept = pill("Departments");
        JButton leaveStd = pill("Leave (Student)");
        JButton leaveTch = pill("Leave (Teacher)");

        grid.add(students);
        grid.add(addStudent);
        grid.add(teachers);
        grid.add(addTeacher);
        grid.add(marks);
        grid.add(dept);
        grid.add(leaveStd);
        grid.add(leaveTch);

        JPanel center = new JPanel(new BorderLayout());
        center.add(grid, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        students.addActionListener(e -> StudentScenes.showList(frame));
        addStudent.addActionListener(e -> StudentScenes.showAdd(frame));
        teachers.addActionListener(e -> new university.management.system.TeacherDetails());
        addTeacher.addActionListener(e -> new university.management.system.AddTeacher());
        //marks.addActionListener(e -> new university.management.system.EnterMarks());
        dept.addActionListener(e -> new university.management.system.Dept());
        leaveStd.addActionListener(e -> new university.management.system.LeaveStudent());
        leaveTch.addActionListener(e -> new university.management.system.LeaveTeacher());
    }

    private JButton pill(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new java.awt.Dimension(180, 48));
        return b;
    }

    public JPanel getRoot() { return root; }
}


