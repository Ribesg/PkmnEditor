package fr.ribesg.pokemon.editor;

import fr.ribesg.pokemon.editor.util.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Ribesg
 */
public final class Context {

    private Lang lang;

    private Path romPath;
    private Rom  rom;

    private int[]                                     starters;
    private Map<Integer, Pair<List<String>, Boolean>> texts;

    public Context() throws IOException {
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
            this.rom.setStarters(this.starters);
            this.texts.forEach((i, t) -> this.rom.setMessages(i, t.getLeft(), t.getRight()));
            this.rom.save(path);
            return true;
        } catch (final Throwable t) {
            Log.error("Failed to save ROM", t);
            return false;
        }
    }

    public int[] getStarters() {
        try {
            if (this.starters == null) {
                this.starters = this.rom.getStarters();
            }
            return this.starters;
        } catch (final Throwable t) {
            Log.error("Failed to set Starters", t);
            return null;
        }
    }

    public boolean setStarters(final int[] starters) {
        try {
            assert starters.length == 3;
            this.starters = starters;
            return true;
        } catch (final Throwable t) {
            Log.error("Failed to set Starters", t);
            return false;
        }
    }

    public List<String> getTexts(final int index) {
        try {
            if (!this.texts.containsKey(index)) {
                this.texts.put(index, this.rom.getMessages(index));
            }
            return new ArrayList<>(this.texts.get(index).getLeft());
        } catch (final Throwable t) {
            Log.error("Failed to get Text " + index, t);
            return null;
        }
    }

    public int getTextsAmount() {
        return this.rom.getMessageFilesAmount();
    }

    public void setTexts(final int index, final List<String> text) {
        if (!this.texts.containsKey(index)) {
            Log.error("Failed to set Text which were not get before: " + index);
        } else {
            this.texts.get(index).getLeft().clear();
            this.texts.get(index).getLeft().addAll(text);
        }
    }

    public String[] getPkmnNames() {
        try {
            return this.rom.getPkmnNames();
        } catch (final Throwable t) {
            Log.error("Failed to get Pkmn Names ", t);
            return null;
        }
    }
}
