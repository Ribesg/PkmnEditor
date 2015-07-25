package fr.ribesg.pokemon.editor.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import fr.ribesg.pokemon.editor.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class MainWindow {

    private final JFrame            main;
    private final JMenu             fileMenu;
    private final JMenuItem         fileMenuOpen;
    private final JMenuItem         fileMenuSaveAs;
    private final JMenuItem         fileMenuQuit;
    private final JMenu             langMenu;
    private final JMenuItem         langMenuEn;
    private final JMenuItem         langMenuFr;
    private final JPanel            content;
    private final TitledBorder      startersTitledBorder;
    private final JComboBox<String> starter1ComboBox;
    private final JComboBox<String> starter2ComboBox;
    private final JComboBox<String> starter3ComboBox;

    private final Context context;

    private final BiMap<String, Integer> pokemons;

    private boolean changingLang;

    public MainWindow() throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Log.error("Uncaught Exception", e));

        this.context = new Context(this);

        this.pokemons = HashBiMap.create(this.context.getLang().getPokemons().size());
        this.loadPokemons();

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
            this.fileMenu = new JMenu(this.context.getLang().get("ui_menu_file"));
            {
                this.fileMenuOpen = new JMenuItem(this.context.getLang().get("ui_menu_file_load"));
                this.fileMenuOpen.addActionListener(this::fileOpenAction);
                this.fileMenu.add(this.fileMenuOpen);

                this.fileMenuSaveAs = new JMenuItem(this.context.getLang().get("ui_menu_file_save"));
                this.fileMenuSaveAs.addActionListener(this::fileSaveAsAction);
                this.fileMenu.add(this.fileMenuSaveAs);

                this.fileMenuQuit = new JMenuItem(this.context.getLang().get("ui_menu_file_quit"));
                this.fileMenuQuit.addActionListener(this::fileQuitAction);
                this.fileMenu.add(this.fileMenuQuit);
            }
            menuBar.add(this.fileMenu);

            this.langMenu = new JMenu(this.context.getLang().get("ui_menu_lang"));
            {
                this.langMenuEn = new JMenuItem(this.context.getLang().get("ui_menu_lang_en"));
                this.langMenuEn.addActionListener(this::langAction);
                this.langMenu.add(this.langMenuEn);

                this.langMenuFr = new JMenuItem(this.context.getLang().get("ui_menu_lang_fr"));
                this.langMenuFr.addActionListener(this::langAction);
                this.langMenu.add(this.langMenuFr);

                this.changingLang = false;
            }
            menuBar.add(this.langMenu);
        }
        this.main.setJMenuBar(menuBar);

        // Content
        final JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));

        this.content = new JPanel();
        {
            this.content.setLayout(new GridBagLayout());

            final JPanel startersPanel = new JPanel();
            {
                this.startersTitledBorder = BorderFactory.createTitledBorder(
                    this.context.getLang().get("ui_content_startersEditor")
                );
                startersPanel.setBorder(BorderFactory.createCompoundBorder(
                    this.startersTitledBorder,
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                this.starter1ComboBox = new JComboBox<>();
                startersPanel.add(this.starter1ComboBox);
                this.starter2ComboBox = new JComboBox<>();
                startersPanel.add(this.starter2ComboBox);
                this.starter3ComboBox = new JComboBox<>();
                startersPanel.add(this.starter3ComboBox);

                this.fillStartersComboBoxes();

                this.starter1ComboBox.setSelectedIndex(Constants.STARTER_1 - 1);
                this.starter2ComboBox.setSelectedIndex(Constants.STARTER_2 - 1);
                this.starter3ComboBox.setSelectedIndex(Constants.STARTER_3 - 1);

                this.starter1ComboBox.addActionListener(this::startersEditAction);
                this.starter2ComboBox.addActionListener(this::startersEditAction);
                this.starter3ComboBox.addActionListener(this::startersEditAction);
            }
            this.content.add(startersPanel);
        }
        contentContainer.add(this.content);

        final JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.X_AXIS));

        final JTextArea logTextArea = new JTextArea();
        ((DefaultCaret) logTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logTextArea.setAutoscrolls(true);
        logTextArea.setEditable(false);
        logTextArea.setRows(5);
        Log.logInto(logTextArea);
        final JScrollPane logScrollPane = new JScrollPane(
            logTextArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        logPanel.add(logScrollPane);
        contentContainer.add(logPanel);

        this.main.add(contentContainer);

        this.main.pack();
        this.main.setSize(new Dimension(800, 400));
        this.main.setResizable(false);
        this.noRomLock();
        this.main.setLocationRelativeTo(null); // Center on screen
        this.main.setVisible(true);

        Log.info("Started!");
    }

    private void loadPokemons() {
        this.pokemons.clear();
        for (final Entry<Integer, String> e : this.context.getLang().getPokemons().entrySet()) {
            this.pokemons.put(String.format("%03d - %s", e.getKey(), e.getValue()), e.getKey());
        }
    }

    private void setTexts() {
        this.changingLang = true;

        this.fileMenu.setText(this.context.getLang().get("ui_menu_file"));
        this.fileMenuOpen.setText(this.context.getLang().get("ui_menu_file_load"));
        this.fileMenuSaveAs.setText(this.context.getLang().get("ui_menu_file_save"));
        this.fileMenuQuit.setText(this.context.getLang().get("ui_menu_file_quit"));
        this.langMenu.setText(this.context.getLang().get("ui_menu_lang"));
        this.langMenuEn.setText(this.context.getLang().get("ui_menu_lang_en"));
        this.langMenuFr.setText(this.context.getLang().get("ui_menu_lang_fr"));
        this.startersTitledBorder.setTitle(this.context.getLang().get("ui_content_startersEditor"));
        this.fillStartersComboBoxes();

        this.changingLang = false;
    }

    private void fillStartersComboBoxes() {
        this.loadPokemons();
        final int selected1 = this.starter1ComboBox.getSelectedIndex();
        final int selected2 = this.starter2ComboBox.getSelectedIndex();
        final int selected3 = this.starter3ComboBox.getSelectedIndex();
        this.starter1ComboBox.removeAllItems();
        this.starter2ComboBox.removeAllItems();
        this.starter3ComboBox.removeAllItems();
        IntStream.range(1, 493 + 1).mapToObj(this.pokemons.inverse()::get).forEach(this.starter1ComboBox::addItem);
        IntStream.range(1, 493 + 1).mapToObj(this.pokemons.inverse()::get).forEach(this.starter2ComboBox::addItem);
        IntStream.range(1, 493 + 1).mapToObj(this.pokemons.inverse()::get).forEach(this.starter3ComboBox::addItem);
        this.starter1ComboBox.setSelectedIndex(selected1);
        this.starter2ComboBox.setSelectedIndex(selected2);
        this.starter3ComboBox.setSelectedIndex(selected3);
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
                return f.isDirectory() || f.getName().endsWith(".nds");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        final int res = fc.showOpenDialog(this.main);
        if (res == JFileChooser.APPROVE_OPTION) {
            final Path path = fc.getSelectedFile().toPath();
            Main.EXECUTOR.submit(() -> {
                if (!path.getFileName().toString().endsWith(".nds")) {
                    Log.info("Wrong file type selected!");
                    JOptionPane.showMessageDialog(
                        this.main,
                        this.context.getLang().get("ui_msg_loadFailedNotNDS")
                    );
                } else {
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
                }
            });
        }
    }

    private void fileSaveAsAction(final ActionEvent e) {
        final JFileChooser fc = new JFileChooser(Paths.get(".").toFile());
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().endsWith(".nds");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        int res = fc.showSaveDialog(this.main);
        if (res == JFileChooser.APPROVE_OPTION) {
            final File f = fc.getSelectedFile();
            final Path path;
            if (!f.getName().endsWith(".nds")) {
                path = Paths.get(f.getParent()).resolve(f.getName() + ".nds");
            } else {
                path = f.toPath();
            }
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
            Log.info("Building ROM " + path.getFileName().toString() + ", please wait...");
            this.noRomLock();
            Main.EXECUTOR.submit(() -> {
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
        Main.EXECUTOR.submit(() -> {
            try {
                Thread.sleep(750);
            } catch (final InterruptedException ignored) {
            }
            System.exit(0);
        });
    }

    private void langAction(final ActionEvent e) {
        final Object src = e.getSource();
        try {
            if (src == this.langMenuEn) {
                this.context.loadLang("en");
                Log.info("Setting language to English");
            } else if (src == this.langMenuFr) {
                this.context.loadLang("fr");
                Log.info("Setting language to French");
            } else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(this.main, "Failed: " + ex.getMessage());
        }
        this.setTexts();
    }

    private void startersEditAction(final ActionEvent e) {
        if (!this.changingLang) {
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
}
