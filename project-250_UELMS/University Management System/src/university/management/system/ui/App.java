package university.management.system.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import university.management.system.UITheme;

public class App {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}

			JFrame frame = new JFrame("University Management System");
			UITheme.applyFrame(frame);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new LoginView(frame).getRoot());
			frame.setSize(900, 600);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}


