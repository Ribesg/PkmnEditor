package fr.ribesg.pokemon.editor.config;

import java.util.*;

/**
 * @author Ribesg
 */
public class ConfigurationSection {

    protected final Map<String, Object> contentMap;

    public ConfigurationSection() {
        this.contentMap = new LinkedHashMap<>();
    }

    public ConfigurationSection(final Map<String, Object> contentMap) {
        this.contentMap = contentMap;
    }

    public boolean is(final String key, final Class clazz) {
        final Object o = this.contentMap.get(key);
        return o != null && clazz.isInstance(o);
    }

    public <T> T getAs(final String key, final Class<T> clazz) {
        if (!this.is(key, clazz)) {
            throw new IllegalArgumentException();
        } else {
            return clazz.cast(this.contentMap.get(key));
        }
    }

    public void set(final String key, final Object o) {
        if (o == null) {
            this.contentMap.remove(key);
        } else if (o instanceof ConfigurationSection) {
            this.contentMap.put(key, ((ConfigurationSection) o).contentMap);
        } else {
            this.contentMap.put(key, o);
        }
    }

    public boolean isString(final String key) {
        return this.is(key, String.class);
    }

    public String getString(final String key) {
        return this.getAs(key, String.class);
    }

    public String getString(final String key, final String def) {
        try {
            return this.getString(key);
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    public boolean isInt(final String key) {
        return this.is(key, Integer.class);
    }

    public Integer getInt(final String key) {
        return this.getAs(key, Integer.class);
    }

    public int getInt(final String key, final int def) {
        try {
            return this.getInt(key);
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    public boolean isBoolean(final String key) {
        return this.is(key, Boolean.class);
    }

    public Boolean getBoolean(final String key) {
        return this.getAs(key, Boolean.class);
    }

    public boolean getBoolean(final String key, final boolean def) {
        try {
            return this.getBoolean(key);
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    public boolean isList(final String key) {
        return this.is(key, List.class);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(final String key) {
        return this.getAs(key, List.class);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(final String key, final List<T> def) {
        try {
            return this.getList(key);
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    public boolean isStringList(final String key) {
        if (!this.isList(key)) {
            return false;
        } else {
            final List<?> list = this.getList(key);
            for (final Object o : list) {
                if (!(o instanceof String)) {
                    return false;
                }
            }
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(final String key) {
        return (List<String>) this.getAs(key, List.class);
    }

    public List<String> getStringList(final String key, final List<String> def) {
        try {
            return this.getStringList(key);
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    public boolean isConfigurationSection(final String key) {
        return this.is(key, Map.class);
    }

    @SuppressWarnings("unchecked")
    public ConfigurationSection getConfigurationSection(final String key) {
        return new ConfigurationSection(this.getAs(key, Map.class));
    }

    public Set<String> getKeys() {
        return this.contentMap.keySet();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ConfigurationSection that = (ConfigurationSection) o;
        return Objects.equals(this.contentMap, that.contentMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.contentMap);
    }
}
