package view;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

// Panel that allows user to attach and manage files in the UI

public class AttachmentsPanel extends JPanel{
    private final DefaultListModel<Path> model = new DefaultListModel<>();
    private final JList<Path> list = new JList<>(model);
    private final JButton addBtn = new JButton("Add Files");
    private final JButton removeBtn = new JButton("Remove Selected");

    // Attachment panel layout and event listeners
    public AttachmentsPanel(){
        super(new BorderLayout());
        setPreferredSize(new Dimension(280, 1));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Title label
        JLabel title = new JLabel("Insert Files");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // File list configuration
        list.setVisibleRowCount(12);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        buttons.add(addBtn);
        buttons.add(removeBtn);
        add(buttons, BorderLayout.SOUTH);

        // Button actions
        addBtn.addActionListener(_ -> openFileChooser(this));
        removeBtn.addActionListener(_ -> removeSelected());

        // Support deleting files with the Delete key
        list.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_DELETE) removeSelected();
            }
        });


    }
    // Opens a file chooser dialog and adds selected files to the list
    public void openFileChooser(Component parent){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
            for (File f : fileChooser.getSelectedFiles()){
                model.addElement(f.toPath());

            }
        }
    }

    // Removes selected files from the list
    public void removeSelected(){
        List<Path> sel = list.getSelectedValuesList();
        for (int i = sel.size() - 1; i >=0; i--){
            model.removeElement(sel.get(i));
        }
    }
}