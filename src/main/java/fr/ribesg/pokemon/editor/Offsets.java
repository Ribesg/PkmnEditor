package fr.ribesg.pokemon.editor;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ribesg
 */
public final class Offsets {

    private int starters;

    public Offsets(final String romKey) throws IOException {
        final String resourceName = "offsets/" + romKey + ".properties";

        final Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream(resourceName));

        try {
            this.setStarters(props);
            // Here goes other offsets
        } catch (final IOException e) {
            throw new IOException("Malformed file " + resourceName, e);
        }
    }

    public int getStarters() {
        return this.starters;
    }

    private void setStarters(final Properties properties) throws IOException {
        this.starters = this.fromProps(properties, "STARTERS");
    }

    private int fromProps(final Properties properties, final String key) throws IOException {
        final String value = properties.getProperty(key);
        if (value == null) {
            throw new IOException("Missing key: " + key);
        }
        return Integer.decode(value);
    }
}
