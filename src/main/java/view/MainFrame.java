package view;

import controller.MainController;
import model.SessionModel;
import model.WritingMode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


// Main application window for the writing assistant
// Sets up the layout, menus, and connects the UI to the controller and session model.
public class MainFrame {

    // main window
    private final JFrame frame = new JFrame("Writing assistant");
    private final JLabel header = new JLabel("CREATIVE MODE", SwingConstants.CENTER);

    // menu bar for selecting writing modes
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu modeMenu = new JMenu("Mode");
    private final JMenuItem creative = new JMenuItem("Creative");
    private final JMenuItem professional = new JMenuItem("Professional");
    private final JMenuItem academic = new JMenuItem("Academic");

    // split layout
    private final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final EditorPanel editorPanel = new EditorPanel();

    // right panel (toggling between file view and tips view)
    private static final String CARD_FILES = "files";
    private static final String CARD_TIPS  = "tips";
    private final JPanel rightCard = new JPanel(new CardLayout());
    private final AttachmentsPanel attachmentsPanel = new AttachmentsPanel();
    private final TipsPanel tipsPanel = new TipsPanel();

    // MVC components
    private final SessionModel session = new SessionModel();
    private final MainController controller = new MainController(session);

    // builds and displays the main window layout
    public MainFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);

        // header label showing current writing mode
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 22f));
        frame.add(header, BorderLayout.NORTH);

        // menu
        modeMenu.add(creative);
        modeMenu.add(professional);
        modeMenu.add(academic);
        menuBar.add(modeMenu);
        frame.setJMenuBar(menuBar);

        // mode selection events
        creative.addActionListener(_ -> setMode("CREATIVE MODE"));
        professional.addActionListener(_ -> setMode("PROFESSIONAL MODE"));
        academic.addActionListener(_ -> setMode("ACADEMIC MODE"));

        // adds right side cards
        rightCard.add(attachmentsPanel, CARD_FILES);
        rightCard.add(tipsPanel, CARD_TIPS);

        // configure split pane layout
        split.setLeftComponent(editorPanel);
        split.setRightComponent(rightCard);
        split.setResizeWeight(1.0);
        split.setDividerLocation(0.72);
        frame.add(split, BorderLayout.CENTER);

        // when user presses Enter in the editor
        editorPanel.setOnEnter(text -> {

            showRight(CARD_TIPS);
            tipsPanel.showGenerating();

            // detects selected writing mode
            WritingMode mode = switch (header.getText()) {
                case "CREATIVE MODE" -> WritingMode.CREATIVE;
                case "ACADEMIC MODE" -> WritingMode.ACADEMIC;
                default -> WritingMode.PROFESSIONAL;
            };

            // send request (async handled in controller)
            controller.onGenerate(text, mode);
        });

        // sync model -> UI
        wireModelToUI();

        showRight(CARD_FILES); // shows attachment panels by default
        frame.setVisible(true);
    }

    // listens for session state changes and updates the UI accordingly
    private void wireModelToUI() {
        session.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "loading" -> {
                    boolean busy = (boolean) evt.getNewValue();
                    if (busy) tipsPanel.showGenerating();
                }
                case "responseText" -> {
                    String text = (String) evt.getNewValue();
                    tipsPanel.showTips(text);
                }
                case "errorMessage" -> {
                    String msg = (String) evt.getNewValue();
                    if (msg != null && !msg.isBlank()) {
                        JOptionPane.showMessageDialog(frame, msg, "API Error", JOptionPane.ERROR_MESSAGE);
                        tipsPanel.showTips("Error: " + msg);
                    }
                }
            }
        });
    }

    private void setMode(String title) { header.setText(title); } // updates header when new mode is selected

    // switches the right-side view between attachments and tips
    private void showRight(String cardId) {
        ((CardLayout) rightCard.getLayout()).show(rightCard, cardId);
        rightCard.revalidate();
        rightCard.repaint();
    }
}
