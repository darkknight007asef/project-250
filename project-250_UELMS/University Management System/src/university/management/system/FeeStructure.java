package university.management.system;

import javax.swing.*;
import java.awt.*;

public class FeeStructure extends JFrame {
    public FeeStructure() {
        setTitle("Fee Structure (Coming Soon)");
        setSize(600, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Fee Structure", SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("This section will display your fee breakdown.", SwingConstants.CENTER);
        UITheme.styleLabel(placeholder);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(placeholder, BorderLayout.CENTER);

        setVisible(true);
    }
}
