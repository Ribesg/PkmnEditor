package fr.ribesg.pokemon.editor.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import fr.ribesg.pokemon.editor.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class MainWindow {

    private final JFrame            main;
    private final JMenuItem         fileMenuSaveAs;
    private final JPanel            content;
    private final JComboBox<String> starter1ComboBox;
    private final JComboBox<String> starter2ComboBox;
    private final JComboBox<String> starter3ComboBox;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Context context;

    public MainWindow() throws IOException {
        this.context = new Context(this);

        final BiMap<String, Integer> pokemons = HashBiMap.create(this.context.getLang().getPokemons().size());
        for (final Entry<Integer, String> e : this.context.getLang().getPokemons().entrySet()) {
            pokemons.put(String.format("%03d - %s", e.getKey(), e.getValue()), e.getKey());
        }

        try {
            Plastic3DLookAndFeel.setCurrentTheme(new ExperienceRoyale());
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (final Exception e) {
            Log.error("Failed to set Look&Feel", e);
        }

        this.main = new JFrame("PkmnEditor");
        this.main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Menu
        final JMenuBar menuBar = new JMenuBar();
        {
            final JMenu fileMenu = new JMenu(this.context.getLang().get("ui_menu_file"));
            {
                final JMenuItem fileMenuOpen = new JMenuItem(this.context.getLang().get("ui_menu_load"));
                fileMenuOpen.addActionListener(this::fileOpenAction);
                fileMenu.add(fileMenuOpen);

                this.fileMenuSaveAs = new JMenuItem(this.context.getLang().get("ui_menu_save"));
                this.fileMenuSaveAs.addActionListener(this::fileSaveAsAction);
                fileMenu.add(this.fileMenuSaveAs);

                final JCheckBoxMenuItem fileMenuQuit = new JCheckBoxMenuItem(
                    this.context.getLang().get("ui_menu_quit"), false
                );
                fileMenuQuit.addActionListener(this::fileQuitAction);
                fileMenu.add(fileMenuQuit);
            }
            menuBar.add(fileMenu);
        }
        this.main.setJMenuBar(menuBar);

        // Content
        final JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;

        this.content = new JPanel();
        {
            this.content.setLayout(new GridBagLayout());

            final JPanel startersPanel = new JPanel();
            {
                startersPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        this.context.getLang().get("ui_content_startersEditor")
                    ),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                this.starter1ComboBox = new JComboBox<>();
                IntStream.range(1, 493 + 1).mapToObj(pokemons.inverse()::get).forEach(this.starter1ComboBox::addItem);
                this.starter1ComboBox.setSelectedIndex(Constants.STARTER_1 - 1);
                this.starter1ComboBox.addActionListener(this::startersEditAction);
                startersPanel.add(this.starter1ComboBox);

                this.starter2ComboBox = new JComboBox<>();
                IntStream.range(1, 493 + 1).mapToObj(pokemons.inverse()::get).forEach(this.starter2ComboBox::addItem);
                this.starter2ComboBox.setSelectedIndex(Constants.STARTER_2 - 1);
                this.starter2ComboBox.addActionListener(this::startersEditAction);
                startersPanel.add(this.starter2ComboBox);

                this.starter3ComboBox = new JComboBox<>();
                IntStream.range(1, 493 + 1).mapToObj(pokemons.inverse()::get).forEach(this.starter3ComboBox::addItem);
                this.starter3ComboBox.setSelectedIndex(Constants.STARTER_3 - 1);
                this.starter3ComboBox.addActionListener(this::startersEditAction);
                startersPanel.add(this.starter3ComboBox);
            }
            this.content.add(startersPanel);
        }
        constraints.weighty = 5;
        constraints.gridy = 0;
        contentContainer.add(this.content, constraints);

        final JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.X_AXIS));

        final JTextArea logTextArea = new JTextArea();
        ((DefaultCaret) logTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logTextArea.setAutoscrolls(true);
        logTextArea.setEditable(false);
        Log.logInto(logTextArea);
        logTextArea.append("Ready to log things!\n");
        final JScrollPane logScrollPane = new JScrollPane(
            logTextArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        logPanel.add(logScrollPane);

        constraints.weighty = 1;
        constraints.gridy = 1;
        contentContainer.add(logPanel, constraints);

        this.main.add(contentContainer);

        this.main.pack();
        this.main.setSize(new Dimension(800, 480));
        this.main.setResizable(false);
        this.noRomLock();
        this.main.setLocationRelativeTo(null); // Center on screen
        this.main.setVisible(true);
    }

    public int getStarter1() {
        return this.starter1ComboBox.getSelectedIndex() + 1;
    }

    public int getStarter2() {
        return this.starter2ComboBox.getSelectedIndex() + 1;
    }

    public int getStarter3() {
        return this.starter3ComboBox.getSelectedIndex() + 1;
    }

    private void noRomLock() {
        this.setContentEnabled(this.content, false);
        this.fileMenuSaveAs.setEnabled(false);
    }

    private void noRomUnlock() {
        this.setContentEnabled(this.content, true);
    }

    private void setContentEnabled(final Container container, final boolean value) {
        for (final Component c : container.getComponents()) {
            c.setEnabled(value);
            if (c instanceof Container) {
                this.setContentEnabled((Container) c, value);
            }
        }
    }

    private void fileOpenAction(final ActionEvent e) {
        final JFileChooser fc = new JFileChooser(Paths.get(".").toFile());
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(".nds");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        final int res = fc.showOpenDialog(this.main);
        if (res == JFileChooser.APPROVE_OPTION) {
            final Path path = fc.getSelectedFile().toPath();
            this.executorService.submit(() -> {
                final boolean loaded = this.context.loadRom(path);
                if (loaded) {
                    this.noRomUnlock();
                    Log.info("Selected ROM " + path.getFileName().toString());
                } else {
                    JOptionPane.showMessageDialog(
                        this.main,
                        this.context.getLang().get(
                            "ui_msg_loadFailed",
                            path.getFileName().toString()
                        )
                    );
                    Log.info("Failed to handle ROM " + path.getFileName().toString());
                }
            });
        }
    }

    private void fileSaveAsAction(final ActionEvent e) {
        final JFileChooser fc = new JFileChooser(Paths.get(".").toFile());
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(".nds");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        int res = fc.showSaveDialog(this.main);
        if (res == JFileChooser.APPROVE_OPTION) {
            final Path path = fc.getSelectedFile().toPath();
            if (Files.exists(path)) {
                final StringBuilder messageBuilder = new StringBuilder();
                if (path.equals(this.context.getRomPath())) {
                    messageBuilder.append(
                        this.context.getLang().get("ui_msg_originalOverrideConfirm")
                    ).append("\n");
                }
                messageBuilder.append(
                    this.context.getLang().get("ui_msg_overrideConfirm", path.getFileName().toString())
                );
                res = JOptionPane.showConfirmDialog(this.main, messageBuilder.toString());
                if (res == JOptionPane.NO_OPTION) {
                    this.fileSaveAsAction(e);
                } else if (res == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            Log.info("Building ROM, please wait...");
            this.noRomLock();
            this.executorService.submit(() -> {
                final boolean saved = this.context.saveRomAs(path);
                if (saved) {
                    Log.info("Build success! Your new ROM is " + path.getFileName().toString());
                    JOptionPane.showMessageDialog(this.main, this.context.getLang().get("ui_msg_buildSuccess", path.getFileName().toString()));
                } else {
                    Log.info("Build failed!");
                    JOptionPane.showMessageDialog(this.main, this.context.getLang().get("ui_msg_buildFailed"));
                }
                this.noRomUnlock();
            });
        }
    }

    private void fileQuitAction(final ActionEvent e) {
        Log.info("Exiting.");
        Log.flush();
        System.exit(0);
    }

    private void startersEditAction(final ActionEvent e) {
        final boolean nothingToDoBefore = !this.context.getStartersChanged();
        this.context.setStartersChanged(
            Constants.STARTER_1 - 1 != this.starter1ComboBox.getSelectedIndex() ||
            Constants.STARTER_2 - 1 != this.starter2ComboBox.getSelectedIndex() ||
            Constants.STARTER_3 - 1 != this.starter3ComboBox.getSelectedIndex()
        );
        final boolean nothingToDoAfter = !this.context.getStartersChanged();

        if (nothingToDoBefore && !nothingToDoAfter) {
            Log.info("Changes selected, you may now build your new ROM");
        } else if (!nothingToDoBefore && nothingToDoAfter) {
            Log.info("No more changes, your new ROM would be the same as original");
        }

        this.fileMenuSaveAs.setEnabled(!nothingToDoAfter);
    }
}
