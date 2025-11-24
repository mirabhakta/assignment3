package view;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

//  Text editor panel where users can paste or type their writing.

public class EditorPanel extends JPanel{
    private final JTextArea editor = new JTextArea();
    private final JButton enterBtn = new JButton("Enter");
    private Consumer<String> onEnter = _ -> {};

    // Builds editor layout and sets up event handling
    public EditorPanel(){
        setLayout(new BorderLayout());

        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Configure editor appearance and paper here
        final String placeholder = "Paste your paper here...";
        editor.setText(placeholder);
        editor.setForeground(Color.BLACK);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        editor.setFont(editor.getFont().deriveFont(15f));

        // Scrollable text area
        JScrollPane scroll = new JScrollPane(editor);
        // Bottom bar with the Enter button
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(new EmptyBorder(10, 0,0 ,0));
        bottomBar.add(enterBtn, BorderLayout.EAST);

        // Defines what happens when the user submits text
        Runnable send = () -> {
            String text = editor.getText();
            if (text.equals(placeholder) || text.isBlank()) return;
            onEnter.accept(text); // trigger callback
        };

        // Button and Enter-key trigger the same action
        enterBtn.addActionListener(_ -> send.run());
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "send");
        editor.getActionMap().put("send", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { send.run(); }
        });

        // Adds components to layout
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        
    }
    // Sets the handler to run when the user presses Enter or clicks the button
    public void setOnEnter(Consumer<String> handler){this.onEnter = handler != null ? handler : _ -> {};}
    // Returns the current text from the editor
    public String getText(){return editor.getText();}
}
