package fr.ribesg.pokemon.editor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Ribesg
 */
public final class Lang {

    private final Properties props;

    private Map<Integer, String> pokemons;

    public Lang() throws IOException {
        this("en");
    }

    public Lang(final String lang) throws IOException {
        final String resourceName = "lang/" + lang + ".properties";

        this.props = new Properties();
        this.props.load(new InputStreamReader(
            this.getClass().getClassLoader().getResourceAsStream(resourceName), StandardCharsets.UTF_8
        ));

        try {
            this.setPokemons();
            // Here goes other offsets
        } catch (final IOException e) {
            throw new IOException("Malformed file " + resourceName, e);
        }
    }

    public String get(final String key, final Object... args) throws UncheckedIOException {
        try {
            return String.format(this.fromProps(this.props, key), args);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Map<Integer, String> getPokemons() {
        return this.pokemons;
    }

    private void setPokemons() throws IOException {
        final Map<Integer, String> tmp = new HashMap<>();
        for (int i = 1; i <= 493; i++) {
            tmp.put(i, this.fromProps(this.props, String.format("pkmn_%03d", i)));
        }
        this.pokemons = Collections.unmodifiableMap(tmp);
    }

    private String fromProps(final Properties properties, final String key) throws IOException {
        final String value = properties.getProperty(key);
        if (value == null) {
            throw new IOException("Missing key: " + key);
        }
        return value;
    }
}
