package fr.ribesg.pokemon.editor.model;

import com.dabomstew.pkrandom.pokemon.Trainer;
import fr.ribesg.pokemon.editor.config.YamlDocument;
import fr.ribesg.pokemon.editor.config.YamlFile;
import fr.ribesg.pokemon.editor.util.Pair;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Ribesg
 */
public final class Context {

    private Lang lang;

    private Path romPath;
    private Rom  rom;

    private int[] originalStarters;
    private int[] starters;

    private Map<Integer, Pair<List<String>, Boolean>> texts;

    private String[] trainersLocation;

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
        this.texts = new HashMap<>();
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
            throw new IllegalStateException("Failed to set Text which was not get before: " + index);
        } else {
            this.texts.get(index).getLeft().clear();
            this.texts.get(index).getLeft().addAll(text);
        }
    }

    // Trainers

    public String getTrainerLocation(final int trainerIndex) throws IOException {
        if (this.trainersLocation == null) {
            final YamlFile file = new YamlFile();
            file.loadFromString(
                IOUtils.toString(
                    ClassLoader.getSystemResourceAsStream("locationToTrainerMap.yml"),
                    StandardCharsets.UTF_8
                )
            );

            final List<Trainer> trainers = this.rom.getTrainers();
            this.trainersLocation = new String[trainers.size()];

            final YamlDocument doc = file.getDocuments().get(0);
            doc.getKeys().stream()
               .filter(key -> !"???".equals(key))
               .forEach(key -> {
                   final String keyString = this.rom.getString(key);
                   final List<String> list = doc.getList(key);
                   list.forEach(val -> {
                       final int intVal = Integer.parseInt(val);
                       this.trainersLocation[intVal] = keyString;
                   });
               })
            ;

            for (int i = 0; i < trainers.size(); i++) {
                if (this.trainersLocation[i] == null) {
                    this.trainersLocation[i] = "???";
                }
            }
        }
        return this.trainersLocation[trainerIndex];
    }

    public String[] getTrainersInfo() throws IOException {
        final List<Trainer> trainers = this.rom.getTrainers();
        final String[] res = new String[trainers.size()];
        for (int i = 0; i < trainers.size(); i++) {
            final Trainer t = trainers.get(i);
            res[i] = String.format(
                "%03d - %s (%s)",
                i,
                t.fullDisplayName,
                this.getTrainerLocation(i)
            );
        }
        return res;
    }

    // Pkmns

    public String[] getPkmnNames() {
        return this.rom.getPkmnNames();
    }
}
