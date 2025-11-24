import view.MainFrame;
import javax.swing.*;
import javax.swing.UIManager;

// entry point for the application

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }
            new MainFrame(); // launches main app window
        });
    }
}
