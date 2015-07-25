package fr.ribesg.pokemon.editor;

import fr.ribesg.pokemon.editor.gui.MainWindow;
import fr.ribesg.pokemon.editor.tool.Arm9Tool;
import fr.ribesg.pokemon.editor.tool.NdsTool;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;

/**
 * @author Ribesg
 */
public final class Main {

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

    public static void main(final String[] args) {
        try {
            final List<String> argsList = Arrays.asList(args);
            if (argsList.contains("--debug")) {
                Log.setDebugEnabled(true);
            }

            Main.extractTools();

            if (args.length == 0 || args.length == 1 && Log.isDebugEnabled()) {
                Main.EXECUTOR.submit(MainWindow::new);
                Runtime.getRuntime().addShutdownHook(new Thread(Main::removeTools));
            } else {
                switch (args[0].toLowerCase()) {
                    case "-fmo":
                    case "--findmagicffset":
                        for (int i = 1; i < args.length; i++) {
                            Main.findMagicOffset(args[i]);
                        }
                        break;
                    case "-cso":
                    case "--checkstartersoffset":
                        for (int i = 1; i < args.length; i++) {
                            Main.checkStartersOffset(args[i]);
                        }
                        break;
                    case "-e":
                    case "-ed":
                    case "--extract":
                    case "--extractdecompressarm9":
                        for (int i = 1; i < args.length; i++) {
                            Main.extract(args[i], args[0].contains("d") || args[0].contains("D"));
                        }
                        break;
                    case "-h":
                    case "--header":
                        for (int i = 1; i < args.length; i++) {
                            Main.printHeader(args[i], false);
                        }
                        break;
                    case "-fh":
                    case "--fullheader":
                        for (int i = 1; i < args.length; i++) {
                            Main.printHeader(args[i], true);
                        }
                        break;
                    default:
                        Log.info("Unknown command " + args[0]);
                }
                Main.removeTools();
            }
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    private static void findMagicOffset(final String romPath) throws IOException {
        final Path path = Paths.get(romPath);
        final Path folderPath = Paths.get(".").resolve(path.getFileName().toString() + "_content");
        final Path arm9Path = folderPath.resolve("arm9.bin");
        NdsTool.extract(path, folderPath);
        Arm9Tool.decompressArm9(arm9Path);
        final int res = Arm9Tool.findMagicOffset(path, arm9Path);
        Log.info(path.getFileName().toString() + ": " + (res < 0 ? res : "0x" + Integer.toHexString(res).toUpperCase()));
        FileUtils.deleteDirectory(folderPath.toFile());
    }

    private static void checkStartersOffset(final String romPath) throws IOException {
        final Path path = Paths.get(romPath);
        final Path folderPath = Paths.get(".").resolve(path.getFileName().toString() + "_content");
        final Path arm9Path = folderPath.resolve("arm9.bin");
        NdsTool.extract(path, folderPath);
        Arm9Tool.decompressArm9(arm9Path);
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        boolean res = arm9.getInt(0x00108538) == 152;
        res &= arm9.getInt(0x00108538 + 4) == 155;
        res &= arm9.getInt(0x00108538 + 8) == 158;
        Log.info(path.getFileName().toString() + " identical to HG_FRA: " + res);
        FileUtils.deleteDirectory(folderPath.toFile());
    }

    private static void extract(final String romPath, final boolean decompressArm9) throws IOException {
        final Path path = Paths.get(romPath);
        final Path folderPath = Paths.get(".").resolve(path.getFileName().toString() + "_content");
        final Path arm9Path = folderPath.resolve("arm9.bin");
        NdsTool.extract(path, folderPath);
        if (decompressArm9) {
            Arm9Tool.decompressArm9(arm9Path);
        }
        Log.info(path.getFileName().toString() + " extracted!");
    }

    private static void printHeader(final String romPath, final boolean full) throws IOException {
        final Path path = Paths.get(romPath);
        final String fileName = path.getFileName().toString();
        final Rom rom = new Rom(path);
        rom.load();
        if (full) {
            final boolean debugWasEnabled = Log.isDebugEnabled();
            Log.setDebugEnabled(true);
            rom.printHeader();
            Log.info("");
            Log.setDebugEnabled(debugWasEnabled);
        } else {
            Log.info(fileName + ": " + rom.getGameTitle() + "|" + rom.getGameCode() + "|" + rom.getIntAsHex(Rom.HEADER_CHECKSUM));
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
                               Files.copy(jar.getInputStream(entry), tmp.resolve(entry.getName()));
                           }
                       } catch (final IOException e) {
                           throw new UncheckedIOException(e);
                       }
                   });
            } catch (final UncheckedIOException e) {
                // This is some weird lambda-throwing-exception thing... but works
                throw e.getCause();
            }
            NdsTool.setNdstoolLocation(tmp.resolve("tools").resolve("ndstool").resolve("ndstool.exe").toString());
        } else {
            // Running in IDE
            NdsTool.setNdstoolLocation("src/main/resources/tools/ndstool/ndstool.exe");
        }
    }

    private static void removeTools() {
        if (Constants.JAR_PATH.endsWith(".jar")) {
            final Path tmp = Constants.TMP_PATH.resolve("PkmnEditor");
            if (Files.exists(tmp)) {
                try {
                    FileUtils.deleteDirectory(tmp.toFile());
                } catch (IOException e) {
                    Log.error("Failed to delete temporary directory", e);
                    Log.flush();
                }
            }
        }
    }
}
