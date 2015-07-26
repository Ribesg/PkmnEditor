package fr.ribesg.pokemon.editor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Ribesg
 */
public final class Lang {

    private final Properties props;

    public Lang() throws IOException {
        this("en");
    }

    public Lang(final String lang) throws IOException {
        final String resourceName = "lang/" + lang + ".properties";

        this.props = new Properties();
        this.props.load(new InputStreamReader(
            this.getClass().getClassLoader().getResourceAsStream(resourceName), StandardCharsets.UTF_8
        ));
    }

    public String get(final String key, final Object... args) throws UncheckedIOException {
        try {
            return String.format(this.fromProps(this.props, key), args);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String fromProps(final Properties properties, final String key) throws IOException {
        final String value = properties.getProperty(key);
        if (value == null) {
            throw new IOException("Missing key: " + key);
        }
        return value;
    }
}
