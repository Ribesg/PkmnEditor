package fr.ribesg.pokemon.editor.gui;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import fr.ribesg.pokemon.editor.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class MainWindow {

    private final JFrame main;

    private final JMenu              fileMenu;
    private final JMenuItem          fileMenuOpen;
    private final JMenuItem          fileMenuSaveAs;
    private final JMenuItem          fileMenuQuit;
    private final JMenu              langMenu;
    private final JMenuItem          langMenuEn;
    private final JMenuItem          langMenuFr;
    private final JPanel             content;
    private final TitledBorder       startersTitledBorder;
    private final JComboBox<String>  starter1ComboBox;
    private final JComboBox<String>  starter2ComboBox;
    private final JComboBox<String>  starter3ComboBox;
    private final TitledBorder       textTitledBorder;
    private final JComboBox<Integer> textNumberComboBox;
    private final JButton            editTextButton;

    private boolean updatingInterface = false;

    private final Context context;

    public MainWindow() {
        try {
            this.context = new Context(this);
        } catch (final IOException e) {
            Log.error("Failed to create Context", e);
            throw new RuntimeException(e);
        }

        try {
            Plastic3DLookAndFeel.setCurrentTheme(new ExperienceRoyale());
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            final Enumeration keys = UIManager.getLookAndFeelDefaults().keys();
            while (keys.hasMoreElements()) {
                final Object key = keys.nextElement();
                final Object value = UIManager.get(key);
                if (value != null && value instanceof FontUIResource) {
                    UIManager.put(key, Constants.FONT);
                }
            }
        } catch (final Exception e) {
            Log.error("Failed to set Look&Feel", e);
        }
        try {
            this.main = new JFrame(PomData.NAME + " - Version " + PomData.VERSION);
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
                    this.fileMenuSaveAs.setEnabled(false);
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
                    startersPanel.setLayout(new BoxLayout(startersPanel, BoxLayout.X_AXIS));
                    this.startersTitledBorder = BorderFactory.createTitledBorder(
                        this.context.getLang().get("ui_main_startersEditor")
                    );
                    startersPanel.setBorder(BorderFactory.createCompoundBorder(
                        this.startersTitledBorder,
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));

                    final Dimension starterComboBoxSize = new Dimension(140, 22);
                    this.starter1ComboBox = new JComboBox<>();
                    this.starter1ComboBox.setPreferredSize(starterComboBoxSize);
                    startersPanel.add(this.starter1ComboBox);
                    this.starter2ComboBox = new JComboBox<>();
                    this.starter2ComboBox.setPreferredSize(starterComboBoxSize);
                    startersPanel.add(this.starter2ComboBox);
                    this.starter3ComboBox = new JComboBox<>();
                    this.starter3ComboBox.setPreferredSize(starterComboBoxSize);
                    startersPanel.add(this.starter3ComboBox);
                }
                this.content.add(startersPanel);

                final JPanel textPanel = new JPanel();
                {
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
                    this.textTitledBorder = BorderFactory.createTitledBorder(
                        this.context.getLang().get("ui_main_textEdition")
                    );
                    textPanel.setBorder(BorderFactory.createCompoundBorder(
                        this.textTitledBorder,
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));

                    this.textNumberComboBox = new JComboBox<>();
                    this.textNumberComboBox.setEnabled(false);
                    this.textNumberComboBox.setPreferredSize(new Dimension(45, 22));
                    textPanel.add(this.textNumberComboBox);

                    this.editTextButton = new JButton(this.context.getLang().get("ui_main_textEditButton"));
                    this.editTextButton.setEnabled(false);
                    this.editTextButton.addActionListener(this::editTextAction);
                    textPanel.add(this.editTextButton);
                }
                this.content.add(textPanel);
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
            this.main.setSize(new Dimension(800, 600));
            this.main.setResizable(false);
            this.lockEditing();
            this.main.setLocationRelativeTo(null); // Center on screen
            this.main.setVisible(true);
        } catch (final Throwable t) {
            Log.error("Failed to create Window", t);
            throw new RuntimeException(t);
        }

        Log.info("Started!");
    }

    private void setTexts() {
        final Lang lang = this.context.getLang();
        this.fileMenu.setText(lang.get("ui_menu_file"));
        this.fileMenuOpen.setText(lang.get("ui_menu_file_load"));
        this.fileMenuSaveAs.setText(lang.get("ui_menu_file_save"));
        this.fileMenuQuit.setText(lang.get("ui_menu_file_quit"));
        this.langMenu.setText(lang.get("ui_menu_lang"));
        this.langMenuEn.setText(lang.get("ui_menu_lang_en"));
        this.langMenuFr.setText(lang.get("ui_menu_lang_fr"));
        this.startersTitledBorder.setTitle(lang.get("ui_main_startersEditor"));
        this.textTitledBorder.setTitle(lang.get("ui_main_textEdition"));
        this.editTextButton.setText(lang.get("ui_main_textEditButton"));
        this.fillStartersComboBoxes();
    }

    private void fillStartersComboBoxes() {
        this.starter1ComboBox.removeAllItems();
        this.starter2ComboBox.removeAllItems();
        this.starter3ComboBox.removeAllItems();
        final String[] pkmnNames = this.context.getRom().getPkmnNames();
        IntStream.range(0, pkmnNames.length).mapToObj(i -> new ImmutablePair<>(i + 1, pkmnNames[i])).forEach(p -> {
            final String item = String.format("%03d - %s", p.getLeft(), p.getRight());
            this.starter1ComboBox.addItem(item);
            this.starter2ComboBox.addItem(item);
            this.starter3ComboBox.addItem(item);
        });
        final int[] starters = this.context.getStarters();
        if (starters != null) {
            this.starter1ComboBox.setSelectedIndex(starters[0] - 1);
            this.starter2ComboBox.setSelectedIndex(starters[1] - 1);
            this.starter3ComboBox.setSelectedIndex(starters[2] - 1);
        }
    }

    private void lockEditing() {
        this.setEditingEnabled(false);
    }

    private void unlockEditing() {
        this.setEditingEnabled(true);
    }

    private void setEditingEnabled(final boolean value) {
        this.starter1ComboBox.setEnabled(value);
        this.starter2ComboBox.setEnabled(value);
        this.starter3ComboBox.setEnabled(value);
        this.textNumberComboBox.setEnabled(value);
        this.editTextButton.setEnabled(value);
    }

    private void fileOpenAction(final ActionEvent e) {
        try {
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
                            this.updatingInterface = true;
                            this.fileMenuSaveAs.setEnabled(true);
                            this.fillStartersComboBoxes();
                            this.starter1ComboBox.setSelectedIndex(Constants.STARTER_1 - 1);
                            this.starter2ComboBox.setSelectedIndex(Constants.STARTER_2 - 1);
                            this.starter3ComboBox.setSelectedIndex(Constants.STARTER_3 - 1);
                            this.starter1ComboBox.addActionListener(this::startersEditAction);
                            this.starter2ComboBox.addActionListener(this::startersEditAction);
                            this.starter3ComboBox.addActionListener(this::startersEditAction);
                            IntStream
                                .range(0, this.context.getRom().getMessageFilesAmount())
                                .forEach(this.textNumberComboBox::addItem);
                            this.unlockEditing();
                            this.updatingInterface = false;
                            Log.info("Selected ROM " + path.getFileName().toString());
                        } else {
                            JOptionPane.showMessageDialog(
                                this.main,
                                this.context.getLang().get(
                                    "ui_msg_loadFailed",
                                    path.getFileName().toString()
                                )
                            );
                        }
                    }
                });
            }
        } catch (final Throwable t) {
            Log.error("Failed to execute File Open Action", t);
        }
    }

    private void fileSaveAsAction(final ActionEvent e) {
        try {
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
                this.lockEditing();
                Main.EXECUTOR.submit(() -> {
                    Log.info("Building ROM...");
                    final boolean saved = this.context.saveRom(path);
                    if (saved) {
                        Log.info("Build success! Your new ROM is " + path.getFileName().toString());
                        JOptionPane.showMessageDialog(this.main, this.context.getLang().get("ui_msg_buildSuccess", path.getFileName().toString()));
                    } else {
                        JOptionPane.showMessageDialog(this.main, this.context.getLang().get("ui_msg_buildFailed"));
                    }
                    this.unlockEditing();
                });
            }
        } catch (final Throwable t) {
            Log.error("Failed to execute FileSaveAction", t);
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
            this.main.setVisible(false);
            this.main.dispose();
            System.exit(0);
        });
    }

    private void langAction(final ActionEvent e) {
        final Object src = e.getSource();
        try {
            if (src == this.langMenuEn) {
                this.context.setLang("en");
                Log.info("Setting language to English");
            } else if (src == this.langMenuFr) {
                this.context.setLang("fr");
                Log.info("Setting language to French");
            } else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(this.main, "Failed: " + ex.getMessage());
        }
        this.updatingInterface = true;
        this.setTexts();
        this.updatingInterface = false;
    }

    private void startersEditAction(final ActionEvent e) {
        if (!this.updatingInterface) {
            this.context.setStarters(new int[] {
                this.starter1ComboBox.getSelectedIndex() + 1,
                this.starter2ComboBox.getSelectedIndex() + 1,
                this.starter3ComboBox.getSelectedIndex() + 1
            });
        }
    }

    private void editTextAction(final ActionEvent e) {
        new TextEditorWindow(this.context, this.textNumberComboBox.getSelectedIndex());
    }
}
