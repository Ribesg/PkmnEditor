package fr.ribesg.pokemon.editor.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ribesg
 */
public class YamlFile {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static Yaml yaml;

    private static Yaml getYaml() {
        if (YamlFile.yaml == null) {
            final DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            YamlFile.yaml = new Yaml(options);
        }
        return YamlFile.yaml;
    }

    private final List<YamlDocument> documents;

    public YamlFile() {
        this.documents = new ArrayList<>();
    }

    public List<YamlDocument> getDocuments() {
        return this.documents;
    }

    public void load(final String filePath) throws IOException {
        final Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        } else {
            try (BufferedReader reader = Files.newBufferedReader(path, YamlFile.CHARSET)) {
                final StringBuilder s = new StringBuilder();
                while (reader.ready()) {
                    s.append(reader.readLine()).append('\n');
                }
                this.loadFromString(s.toString());
            }
        }
    }

    public void loadFromString(final String yamlFileContent) {
        final Iterable<Object> documents = YamlFile.getYaml().loadAll(yamlFileContent);
        for (final Object o : documents) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> documentMap = (Map<String, Object>) o;
            this.documents.add(new YamlDocument(documentMap));
        }
    }

    public void save(final String filePath) throws IOException {
        final Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path, YamlFile.CHARSET)) {
            writer.write(this.saveToString());
        }
    }

    public String saveToString() {
        final List<Map<String, Object>> rawContentMap =
            this.documents.stream().map(YamlDocument::asMap).collect(Collectors.toList());
        return YamlFile.getYaml().dumpAll(rawContentMap.iterator());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final YamlFile yamlFile = (YamlFile) o;
        return Objects.equals(this.documents, yamlFile.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.documents);
    }
}
