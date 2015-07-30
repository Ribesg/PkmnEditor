package fr.ribesg.pokemon.editor.model;

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

    // Lang

    public Lang getLang() {
        return this.lang;
    }

    public void setLang(final String lang) throws IOException {
        this.lang = new Lang(lang);
    }

    // Rom

    public Path getRomPath() {
        return this.romPath;
    }

    public void loadRom(final Path path) {
        this.romPath = path;
        this.rom = new Rom(this.romPath);
        this.rom.load();
    }

    public void saveRom(final Path path) {
        this.rom.setStarters(this.starters);
        this.texts.forEach((i, t) -> this.rom.setMessages(i, t.getLeft(), t.getRight()));
        this.rom.save(path);
    }

    // Starters

    public int[] getStarters() {
        if (this.starters == null) {
            this.starters = this.rom.getStarters();
        }
        return this.starters;
    }

    public void setStarters(final int[] starters) {
        assert starters.length == 3;
        this.starters = starters;
    }

    // Texts

    public List<String> getTexts(final int index) {
        if (!this.texts.containsKey(index)) {
            this.texts.put(index, this.rom.getMessages(index));
        }
        return new ArrayList<>(this.texts.get(index).getLeft());
    }

    public int getTextsAmount() {
        return this.rom.getMessageFilesAmount();
    }

    public void setTexts(final int index, final List<String> text) {
        if (!this.texts.containsKey(index)) {
            throw new IllegalStateException("Failed to set Text which were not get before: " + index);
        } else {
            this.texts.get(index).getLeft().clear();
            this.texts.get(index).getLeft().addAll(text);
        }
    }

    // Trainers

    public String[] getTrainersInfo() {
        return new String[0]; // TODO
    }

    // Pkmns

    public String[] getPkmnNames() {
        return this.rom.getPkmnNames();
    }
}
