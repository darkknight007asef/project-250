package university.management.system.ui;

import javax.swing.JFrame;

public final class SceneFactory {
    private SceneFactory() {}

    public static void showDashboard(JFrame frame) {
        ProjectView view = new ProjectView(frame);
        frame.setContentPane(view.getRoot());
        frame.setSize(1100, 720);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    public static void showForget(JFrame frame) {
        ForgetView view = new ForgetView(frame);
        frame.setContentPane(view.getRoot());
        frame.setSize(700, 520);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }
}


