package fr.ribesg.pokemon.editor.controller;

import fr.ribesg.pokemon.editor.model.Context;
import fr.ribesg.pokemon.editor.model.Lang;
import fr.ribesg.pokemon.editor.view.EditTextView;
import javafx.application.Platform;

import java.util.List;

/**
 * @author Ribesg
 */
public final class EditTextController {

    private final Context      model;
    private final EditTextView view;

    private final int index;

    public EditTextController(final MainController main, final EditTextView view, final int index) {
        this.model = main.model;
        this.view = view;

        this.index = index;
    }

    public List<String> getTexts() {
        return this.model.getTexts(this.index);
    }

    public void saveTexts(final List<String> texts) {
        this.model.setTexts(this.index, texts);
        try {
            Thread.sleep(150);
        } catch (final InterruptedException ignored) {
        }
        Platform.runLater(this.view::close);
    }

    public Lang getLang() {
        return this.model.getLang();
    }
}
