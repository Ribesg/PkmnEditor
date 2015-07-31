package fr.ribesg.pokemon.editor;

import com.dabomstew.pkrandom.pokemon.Trainer;
import fr.ribesg.pokemon.editor.config.YamlDocument;
import fr.ribesg.pokemon.editor.config.YamlFile;
import fr.ribesg.pokemon.editor.model.Rom;
import fr.ribesg.pokemon.editor.view.MainView;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
                    case "--search-string":
                    case "-ss":
                        Main.search(args[1]);
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
     * Used only for testing during development.
     */
    private static void dev(final String romPath) throws Throwable {
        Log.setDebugEnabled(true);
        final Rom rom = new Rom(Paths.get(romPath));
        rom.load();

        final Map<String, List<String>> map = new TreeMap<>();

        final YamlFile file = new YamlFile();
        file.loadFromString(
            IOUtils.toString(
                ClassLoader.getSystemResourceAsStream("locationToTrainerMap.yml"),
                StandardCharsets.UTF_8
            )
        );
        final YamlDocument doc = file.getDocuments().get(0);
        doc.getKeys().stream()
           .filter(key -> !"???".equals(key))
           .forEach(key -> {
               final List<String> list = doc.getList(key);
               Collections.sort(list);
               map.put(key, list);
           })
        ;

        final List<Trainer> trainers = rom.getHandler().getTrainers();
        final StringBuilder res = new StringBuilder();
        for (final String key : map.keySet()) {
            final List<String> list = map.get(key);
            res
                .append('"')
                .append(key)
                .append('"')
                .append(": # ")
                .append(rom.getString(key))
                .append('\n')
            ;
            for (final String val : list) {
                final int intVal = Integer.parseInt(val);
                res
                    .append("  - ")
                    .append('"')
                    .append(val)
                    .append('"')
                    .append(" # ")
                    .append(trainers.get(intVal - 1).fullDisplayName)
                    .append('\n')
                ;
            }
            res.append('\n');
        }
        res
            .append('"')
            .append("???")
            .append("\":\n")
        ;
        kek:
        for (int i = 0; i < trainers.size(); i++) {
            final String num = String.format("%03d", i + 1);
            for (final String key : map.keySet()) {
                for (final String val : map.get(key)) {
                    if (val.equals(num)) {
                        continue kek;
                    }
                }
            }
            res
                .append("  - \"")
                .append(i + 1)
                .append("\" # ")
                .append(trainers.get(i).fullDisplayName)
                .append('\n')
            ;
        }
        Files.write(
            Paths.get("src/main/resources/locationToTrainerMap.yml"),
            res.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void search(final String romPath) throws Throwable {
        final Rom rom = new Rom(Paths.get(romPath));
        rom.load();
        final Scanner in = new Scanner(System.in);
        while (true) {
            Log.info("Query: ");
            final String query = in.nextLine().toLowerCase();
            if (query.isEmpty()) {
                break;
            } else if (query.startsWith("dump")) {
                final int n = Integer.parseInt(query.substring(5));
                final List<String> mes = rom.getHandler().getMessages(n).getLeft();
                for (int j = 0; j < mes.size(); j++) {
                    Log.info(n + " - " + j + " - " + mes.get(j));
                }
            } else {
                for (int i = 0; i < rom.getHandler().getMessageFilesAmount(); i++) {
                    final List<String> mes = rom.getHandler().getMessages(i).getLeft();
                    for (int j = 0; j < mes.size(); j++) {
                        if (mes.get(j).toLowerCase().contains(query)) {
                            Log.info(i + " - " + j + " - " + mes.get(j));
                        }
                    }
                }
            }
            Log.info("");
        }
    }
}
