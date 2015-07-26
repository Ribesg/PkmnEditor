package fr.ribesg.pokemon.editor;

import com.dabomstew.pkrandom.pokemon.Pokemon;
import com.dabomstew.pkrandom.romhandlers.Gen4RomHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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

    public void setStarters(final int a, final int b, final int c) {
        this.checkLoaded();
        this.handler.setStarters(Arrays.asList(
            this.handler.getPokemon().get(a),
            this.handler.getPokemon().get(b),
            this.handler.getPokemon().get(c)
        ));
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

    private void checkLoaded() {
        if (this.handler == null) {
            throw new IllegalStateException("No ROM loaded!");
        }
    }
}
