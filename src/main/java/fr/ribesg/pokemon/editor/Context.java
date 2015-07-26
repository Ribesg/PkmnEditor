package fr.ribesg.pokemon.editor;

import fr.ribesg.pokemon.editor.gui.MainWindow;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Ribesg
 */
public final class Context {

    private Lang lang;

    private Path romPath;
    private Rom  rom;

    public Context(final MainWindow mainWindow) throws IOException {
        this.lang = new Lang();

        this.romPath = null;
        this.rom = null;
    }

    public Lang getLang() {
        return this.lang;
    }

    public void setLang(final String lang) throws IOException {
        this.lang = new Lang(lang);
    }

    public Path getRomPath() {
        return this.romPath;
    }

    public Rom getRom() {
        return this.rom;
    }

    public boolean loadRom(final Path path) {
        try {
            this.romPath = path;
            this.rom = new Rom(this.romPath);
            this.rom.load();
            return true;
        } catch (final Throwable t) {
            Log.error("Failed to load ROM", t);
            return false;
        }
    }

    public boolean saveRom(final Path path) {
        try {
            this.rom.save(path);
            return true;
        } catch (final Throwable t) {
            Log.error("Failed to save ROM", t);
            return false;
        }
    }

    public int[] getStarters() {
        try {
            return this.rom.getStarters();
        } catch (final Throwable t) {
            Log.error("Failed to set Starters", t);
            return null;
        }
    }

    public boolean setStarters(final int[] starters) {
        try {
            this.rom.setStarters(starters[0], starters[1], starters[2]);
            return true;
        } catch (final Throwable t) {
            Log.error("Failed to set Starters", t);
            return false;
        }
    }
}
