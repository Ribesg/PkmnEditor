package fr.ribesg.pokemon.editor.gui;

import fr.ribesg.pokemon.editor.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Ribesg
 */
public final class TextEditorWindow {

    private final JDialog       main;
    private final JList<String> availableTextsList;
    private final JTextArea     editableArea;

    private final Context context;

    private final int                         index;
    private final Pair<List<String>, Boolean> texts;

    private int previousSelectedIndex = -1;

    public TextEditorWindow(final MainWindow main, final Context context, final int index) {
        this.context = context;
        this.index = index;

        try {
            this.texts = this.context.getRom().getMessages(index);

            this.main = new JDialog(
                main.main,
                this.context.getLang().get("ui_textEditor_textEditor", index),
                true
            );
            this.main.setLayout(new BorderLayout(5, 5));

            final JPanel content = new JPanel();
            {
                content.setLayout(new GridLayout(2, 1, 5, 5));

                this.availableTextsList = new JList<>(

                );
                final DefaultListModel<String> model = new DefaultListModel<>();
                this.texts.getKey().forEach(model::addElement);
                this.availableTextsList.setModel(model);
                this.availableTextsList.addListSelectionListener(this::listSelection);
                this.availableTextsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                final JScrollPane textsScrollPane = new JScrollPane(
                    this.availableTextsList,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                );
                content.add(textsScrollPane);

                this.editableArea = new JTextArea();
                this.editableArea.setEditable(false);
                content.add(this.editableArea);
            }
            this.main.add(content, BorderLayout.CENTER);

            final JPanel bottomLine = new JPanel();
            {
                bottomLine.setLayout(new FlowLayout(FlowLayout.RIGHT));

                final JButton saveButton = new JButton(
                    this.context.getLang().get("ui_textEditor_save")
                );
                saveButton.addActionListener(this::saveAction);
                bottomLine.add(saveButton);

                final JButton cancelButton = new JButton(
                    this.context.getLang().get("ui_textEditor_cancel")
                );
                cancelButton.addActionListener(this::cancelAction);
                bottomLine.add(cancelButton);
            }
            this.main.add(bottomLine, BorderLayout.SOUTH);

            this.main.pack();
            this.main.setSize(new Dimension(800, 500));
            this.main.setResizable(false);
            this.main.setLocationRelativeTo(null); // Center on screen
            this.main.setLocation(
                (int) this.main.getLocation().getX(),
                (int) this.main.getLocation().getY() - 50
            );
            this.main.setVisible(true);
        } catch (final Throwable t) {
            Log.error("Failed to create TextEditorWindow", t);
            throw new RuntimeException(t);
        }

        Log.info("Text Editor Started");
    }

    private void listSelection(final ListSelectionEvent e) {
        try {
            if (e.getValueIsAdjusting()) {
                this.setText();
            }
        } catch (final Throwable t) {
            Log.error("Failed to handle List Selection Event", t);
        }
    }

    private void setText() {
        String text;
        if (this.previousSelectedIndex >= 0) {
            text = this.editableArea.getText();
            text = text.replace("\n", "\\n");
            this.texts.getLeft().set(this.previousSelectedIndex, text);
            ((DefaultListModel<String>) this.availableTextsList.getModel())
                .set(this.previousSelectedIndex, text);
        }

        text = this.texts.getLeft().get(
            this.availableTextsList.getSelectedIndex()
        );
        text = text.replace("\\n", "\n");
        this.editableArea.setText(text);
        if (!this.editableArea.isEditable()) {
            this.editableArea.setEditable(true);
        }

        this.previousSelectedIndex = this.availableTextsList.getSelectedIndex();
    }

    private void saveAction(final ActionEvent e) {
        try {
            this.setText(); // Save latest change
            Main.EXECUTOR.submit(() -> {
                try {
                    Thread.sleep(150); // Let the user see the change
                    this.context.getRom().setMessages(this.index, this.texts.getLeft(), this.texts.getRight());
                    this.close();
                } catch (final Throwable t) {
                    Log.error("Failed to handle Cancel Action", t);
                }
            });
        } catch (final Throwable t) {
            Log.error("Failed to handle Cancel Action", t);
        }
    }

    private void cancelAction(final ActionEvent e) {
        try {
            this.close();
        } catch (final Throwable t) {
            Log.error("Failed to handle Cancel Action", t);
        }
    }

    private void close() {
        this.main.setVisible(false);
        this.main.dispose();
    }
}
