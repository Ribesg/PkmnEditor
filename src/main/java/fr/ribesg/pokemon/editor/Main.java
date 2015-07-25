package fr.ribesg.pokemon.editor;

import fr.ribesg.pokemon.editor.gui.MainWindow;
import fr.ribesg.pokemon.editor.tool.NdsTool;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarFile;

/**
 * @author Ribesg
 */
public final class Main {

    public static void main(final String[] args) {
        try {
            if (Arrays.asList(args).contains("--debug")) {
                Log.setDebugEnabled(true);
            }
            Main.extractTools();
            new MainWindow();
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    private static void extractTools() throws IOException {
        if (Constants.JAR_PATH.endsWith(".jar")) {
            // Running as JAR
            final Path tmp = Constants.TMP_PATH.resolve("PkmnEditor");
            if (Files.exists(tmp)) {
                FileUtils.deleteDirectory(tmp.toFile());
            }
            Files.createDirectories(tmp);
            final JarFile jar = new JarFile(Constants.JAR_PATH);
            try {
                jar.stream()
                   .filter(entry -> entry.getName().startsWith("tools"))
                   .forEach(entry -> {
                       try {
                           if (entry.isDirectory()) {
                               Files.createDirectories(tmp.resolve(entry.getName()));
                           } else {
                               Main.extract(jar.getInputStream(entry), tmp.resolve(entry.getName()));
                           }
                       } catch (final IOException e) {
                           throw new UncheckedIOException(e);
                       }
                   });
            } catch (final UncheckedIOException e) {
                // This is some weird lambda-throwing-exception thing... but works
                throw e.getCause();
            }
            NdsTool.setNdstoolLocation(tmp.toAbsolutePath().toString() + "/ndstool/ndstool.exe");
        } else {
            // Running in IDE
            NdsTool.setNdstoolLocation("src/main/resources/tools/ndstool/ndstool.exe");
        }
    }

    private static void extract(final InputStream in, final Path to) {

    }
}
