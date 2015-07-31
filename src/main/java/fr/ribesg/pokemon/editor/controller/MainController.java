package fr.ribesg.pokemon.editor.controller;

import fr.ribesg.pokemon.editor.Log;
import fr.ribesg.pokemon.editor.model.Context;
import fr.ribesg.pokemon.editor.model.Lang;
import fr.ribesg.pokemon.editor.view.MainView;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;

/**
 * @author Ribesg
 */
public final class MainController {

    /* package */ final Context model;
    private final MainView view;

    public MainController(final MainView view) throws IOException {
        this.model = new Context();
        this.view = view;
    }

    public void appBuilt() {
        Log.info("Started!");
        Log.info("First, load a clean HG/SS ROM using File -> Open");
    }

    public void loadRom(final File file) {
        try {
            this.model.loadRom(file.toPath());
            Platform.runLater(() -> {
                try {
                    this.view.romLoaded(
                        this.model.getStarters(),
                        this.model.getPkmnNames(),
                        this.model.getTextsAmount(),
                        this.model.getTrainersInfo()
                    );
                    Log.info("ROM loaded!");
                } catch (final Throwable t) {
                    Log.error(t);
                }
            });
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    public void saveRom(final File file) {
        try {
            this.model.saveRom(file.toPath());
            Platform.runLater(() -> {
                try {
                    this.view.romSaved();
                    Log.info("ROM saved!");
                } catch (final Throwable t) {
                    Log.error(t);
                }
            });
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    public Lang getLang() {
        return this.model.getLang();
    }

    public void changeLang(final String lang) {
        try {
            this.model.setLang(lang);
            Platform.runLater(this.view::useLang);
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    public int[] getStarters() {
        return this.model.getStarters();
    }

    public void setStarters(final int[] starters) {
        try {
            this.model.setStarters(starters);
        } catch (final Throwable t) {
            Log.error(t);
        }
    }
}
