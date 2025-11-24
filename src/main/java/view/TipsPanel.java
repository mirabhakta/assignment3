package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Displays status messages and AI-generated tips/results
// Shown on the right side of the main window when the API response is ready
public class TipsPanel extends JPanel {

    private final JLabel title = new JLabel("Tips"); // panel title
    private final JTextArea tipsTextArea = new JTextArea(); // area to display text output

    // builds the panel layout and initializes default appearance
    public TipsPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // header label
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        add(title, BorderLayout.NORTH);

        // configure text area for tips
        tipsTextArea.setLineWrap(true);
        tipsTextArea.setWrapStyleWord(true);
        tipsTextArea.setEditable(false);
        tipsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // scrollable text area
        add(new JScrollPane(tipsTextArea,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);

        showTips("Ready."); // default state
    }

    // shows loading message used by MainFrame while the API call runs
    public void showGenerating() {
        tipsTextArea.setText("Generating tips...");
        tipsTextArea.setCaretPosition(0);
    }

    // replaces tip screen with API response
    public void showTips(String text) {
        tipsTextArea.setText(text == null ? "" : text);
        tipsTextArea.setCaretPosition(0);
    }
}
