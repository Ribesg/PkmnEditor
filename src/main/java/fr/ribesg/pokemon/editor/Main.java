package fr.ribesg.pokemon.editor;

import fr.ribesg.pokemon.editor.gui.MainWindow;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            if (args.length == 0 || args.length == 1 && Log.isDebugEnabled()) {
                Main.EXECUTOR.submit(MainWindow::new);
            } else {
                switch (args[0].toLowerCase()) {
                    case "--dev":
                        Main.dev(args[1]);
                        break;
                    default:
                        Log.info("Unknown command " + args[0]);
                }
            }
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    /**
     * Used only for testing during development, should stay empty in the
     * Github repo.
     */
    private static void dev(final String romPath) throws Throwable {
        Log.setDebugEnabled(true);
        final Rom rom = new Rom(Paths.get(romPath));
        rom.load();
        final Random rand = new Random();
        rom.setStarters(
            rand.nextInt(493) + 1,
            rand.nextInt(493) + 1,
            rand.nextInt(493) + 1
        );
        final List<String> messages = rom.getMessages(190).getLeft();
        for (int i = 0; i < messages.size(); i++) {
            final String message = messages.get(i);
            Log.info(i + " - " + message);
        }
    }
}
