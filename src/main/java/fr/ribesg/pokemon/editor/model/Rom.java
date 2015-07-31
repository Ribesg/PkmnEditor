package fr.ribesg.pokemon.editor.model;

import com.dabomstew.pkrandom.pokemon.Pokemon;
import com.dabomstew.pkrandom.pokemon.Trainer;
import com.dabomstew.pkrandom.romhandlers.Gen4RomHandler;
import fr.ribesg.pokemon.editor.util.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class Rom {

    private final Path filePath;

    private Gen4RomHandler handler;

    public Rom(final Path filePath) {
        assert Files.exists(filePath) : "File not found: " + filePath;
        this.filePath = filePath;
    }

    public Gen4RomHandler getHandler() {
        return this.handler;
    }

    public void load() {
        this.handler = new Gen4RomHandler();
        this.handler.loadRom(this.filePath.toAbsolutePath().toString());
    }

    public void save(final Path path) {
        this.checkLoaded();
        this.handler.saveRom(path.toAbsolutePath().toString());
    }

    public int[] getStarters() {
        this.checkLoaded();
        return this.handler.getStarters().stream().mapToInt(p -> p.number).toArray();
    }

    public void setStarters(final int... starters) {
        assert starters.length == 3;
        this.checkLoaded();
        this.handler.setStarters(
            Arrays.asList(
                this.handler.getPokemon().get(starters[0]),
                this.handler.getPokemon().get(starters[1]),
                this.handler.getPokemon().get(starters[2])
            )
        );
    }

    public List<Trainer> getTrainers() {
        return Collections.unmodifiableList(new ArrayList<>(this.handler.getTrainers()));
    }

    public void setTrainers(final List<Trainer> trainerData) {
        this.handler.setTrainers(new ArrayList<>(trainerData));
    }

    public String getPkmnName(final int num) {
        this.checkLoaded();
        return this.handler.getPokemon().get(num - 1).name;
    }

    public String[] getPkmnNames() {
        this.checkLoaded();
        final List<Pokemon> pkmns = this.handler.getPokemon();
        final String[] result = new String[pkmns.size() - 1];
        IntStream.range(1, pkmns.size()).mapToObj(pkmns::get).forEach(p -> result[p.number - 1] = p.name);
        return result;
    }

    public int getMessageFilesAmount() {
        return this.handler.getMessageFilesAmount();
    }

    public Pair<List<String>, Boolean> getMessages(final int index) {
        return this.handler.getMessages(index);
    }

    public void setMessages(final int index, final List<String> messages, final boolean compressed) {
        this.handler.setMessages(index, messages, compressed);
    }

    public String getString(String key) {
        final int file, line;
        int index = -1;
        final String comment;
        if (!key.contains("-")) {
            key = "279-" + key;
        }
        if (key.contains("/")) {
            comment = key.substring(key.indexOf('/') + 1);
            key = key.substring(0, key.indexOf('/'));
        } else {
            comment = null;
        }
        final String[] split = key.split("-");
        file = Integer.parseInt(split[0]);
        line = Integer.parseInt(split[1]);
        if (split.length > 2) {
            index = Integer.parseInt(split[2]);
        }

        final String s = this.handler.getMessages(file).getLeft().get(line);
        String res;
        if (index == -1) {
            res = s;
        } else {
            final String[] ss = s.split("\\\\n|\\\\r");
            res = ss[index];
        }
        if (comment != null) {
            res += " (" + comment + ')';
        }
        return res;
    }

    private void checkLoaded() {
        if (this.handler == null) {
            throw new IllegalStateException("No ROM loaded!");
        }
    }
}
