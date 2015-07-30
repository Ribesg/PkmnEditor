package fr.ribesg.pokemon.editor.controller;

import fr.ribesg.pokemon.editor.model.Context;
import fr.ribesg.pokemon.editor.model.Lang;
import fr.ribesg.pokemon.editor.view.MainApp;

import java.io.File;
import java.io.IOException;

/**
 * @author Ribesg
 */
public final class MainAppController {

    private final Context context;
    private final MainApp app;

    public MainAppController(final MainApp app) throws IOException {
        this.context = new Context();
        this.app = app;
    }

    public void loadRom(final File file) {
        this.context.loadRom(file.toPath());
        this.app.fillComboBoxes(
            this.context.getStarters(),
            this.context.getPkmnNames(),
            this.context.getTextsAmount(),
            this.context.getTrainersInfo()
        );
        this.app.romLoaded();
    }

    public void saveRom(final File file) {
        this.context.saveRom(file.toPath());
    }

    public Lang getLang() {
        return this.context.getLang();
    }
}
