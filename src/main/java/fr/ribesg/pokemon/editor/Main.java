package fr.ribesg.pokemon.editor;

import com.dabomstew.pkrandom.pokemon.Trainer;
import fr.ribesg.pokemon.editor.model.Rom;
import fr.ribesg.pokemon.editor.view.MainView;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ribesg
 */
public final class Main {

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static void main(final String[] args) {
        try {
            final List<String> argsList = Arrays.asList(args);
            if (argsList.contains("--debug")) {
                Log.setDebugEnabled(true);
            }

            if (args.length == 0 || args.length == 1 && Log.isDebugEnabled()) {
                MainView.launch();
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
        final List<Trainer> trainers = rom.getHandler().getTrainers();
        for (int i = 0; i < trainers.size(); i++) {
            final Trainer t = trainers.get(i);
            Log.info(
                i +
                " - Trainer{" +
                "offset=" + t.offset +
                ", pokemon=" + t.pokemon.size() +
                ", tag='" + t.tag + '\'' +
                ", importantTrainer=" + t.importantTrainer +
                ", poketype=" + t.poketype +
                ", name='" + t.name + '\'' +
                ", trainerclass=" + t.trainerclass +
                ", fullDisplayName='" + t.fullDisplayName + '\'' +
                '}'
            );
        }
    }
}
