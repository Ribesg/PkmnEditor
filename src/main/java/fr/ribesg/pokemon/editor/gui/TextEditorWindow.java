package fr.ribesg.pokemon.editor.gui;

import fr.ribesg.pokemon.editor.Context;
import fr.ribesg.pokemon.editor.Log;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

/**
 * @author Ribesg
 */
public final class TextEditorWindow {

    private final JFrame    main;
    private final JTextArea editableArea;

    private final Context context;

    private final Pair<List<String>, Boolean> texts;

    public TextEditorWindow(final Context context, final int text) {
        this.context = context;

        try {
            this.texts = this.context.getRom().getMessages(text);

            this.main = new JFrame(this.context.getLang().get("ui_textEditor_textEditor", text));
            this.main.setLayout(new BorderLayout(5, 5));

            final JPanel content = new JPanel();
            {
                content.setLayout(new GridLayout(2, 1, 5, 5));

                final JList<String> availableTextsList = new JList<>(
                    this.texts.getKey().toArray(new String[this.texts.getKey().size()])
                );
                availableTextsList.addListSelectionListener(this::listSelection);
                availableTextsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                final JScrollPane textsScrollPane = new JScrollPane(
                    availableTextsList,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                );
                content.add(textsScrollPane);

                this.editableArea = new JTextArea();
                this.editableArea.setEditable(false);
                content.add(this.editableArea);
            }
            this.main.add(content, BorderLayout.CENTER);

            final JPanel buttonLine = new JPanel();
            {
                buttonLine.setLayout(new BorderLayout(5, 5));

                final JButton saveButton = new JButton("Save");
                buttonLine.add(saveButton, BorderLayout.LINE_END);

                final JButton cancelButton = new JButton("Cancel");
                buttonLine.add(cancelButton, BorderLayout.AFTER_LINE_ENDS);
            }
            this.main.add(buttonLine, BorderLayout.SOUTH);

            this.main.pack();
            this.main.setSize(new Dimension(800, 400));
            this.main.setResizable(false);
            this.main.setLocationRelativeTo(null); // Center on screen
            this.main.setVisible(true);
        } catch (final Throwable t) {
            Log.error("Failed to create TextEditorWindow", t);
            throw new RuntimeException(t);
        }

        Log.info("Text Editor Started");
    }

    private void listSelection(final ListSelectionEvent e) {
        this.editableArea.setEditable(true);
        this.editableArea.setText(this.texts.getLeft().get(e.getFirstIndex()));
        Log.info(this.editableArea.getText());
    }
}
