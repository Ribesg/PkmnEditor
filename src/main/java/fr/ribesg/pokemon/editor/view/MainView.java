package fr.ribesg.pokemon.editor.view;

import fr.ribesg.pokemon.editor.*;
import fr.ribesg.pokemon.editor.controller.MainController;
import fr.ribesg.pokemon.editor.model.Lang;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class MainView extends Application {

    public static void launch() {
        Application.launch();
    }

    private MainController controller;

    private Stage stage;

    private Menu              fileMenu;
    private MenuItem          openFileMenuItem;
    private MenuItem          quitFileMenuItem;
    private Menu              langMenu;
    private MenuItem          enLangMenuItem;
    private MenuItem          frLangMenuItem;
    private MenuItem          saveFileMenuItem;
    private Label             startersLabel;
    private ComboBox<String>  starters1ComboBox;
    private ComboBox<String>  starters2ComboBox;
    private ComboBox<String>  starters3ComboBox;
    private Button            startersButton;
    private Label             textsLabel;
    private ComboBox<Integer> textsComboBox;
    private Button            textsButton;
    private Label             trainersLabel;
    private ComboBox<String>  trainersComboBox;
    private Button            trainersButton;

    private int secondaryWindowCounter;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.controller = new MainController(this);

        this.stage = primaryStage;

        this.secondaryWindowCounter = 0;

        final BorderPane rootPane = new BorderPane();
        {
            final MenuBar menu = new MenuBar();
            {
                rootPane.setTop(menu);

                this.fileMenu = new Menu();
                {
                    menu.getMenus().add(this.fileMenu);

                    this.openFileMenuItem = new MenuItem();
                    this.fileMenu.getItems().add(this.openFileMenuItem);
                    this.openFileMenuItem.setOnAction(this::onFileMenuOpenAction);

                    this.saveFileMenuItem = new MenuItem();
                    this.fileMenu.getItems().add(this.saveFileMenuItem);
                    this.saveFileMenuItem.setOnAction(this::onFileMenuSaveAction);

                    this.quitFileMenuItem = new MenuItem();
                    this.fileMenu.getItems().add(this.quitFileMenuItem);
                    this.quitFileMenuItem.setOnAction(this::onFileMenuQuitAction);
                }

                this.langMenu = new Menu();
                {
                    menu.getMenus().add(this.langMenu);

                    this.enLangMenuItem = new MenuItem();
                    this.langMenu.getItems().add(this.enLangMenuItem);
                    this.enLangMenuItem.setOnAction(this::onEnLangAction);

                    this.frLangMenuItem = new MenuItem();
                    this.langMenu.getItems().add(this.frLangMenuItem);
                    this.frLangMenuItem.setOnAction(this::onFrLangAction);
                }
            }

            final GridPane contentPane = new GridPane();
            {
                rootPane.setCenter(contentPane);

                final int colWidth = 190;

                contentPane.setAlignment(Pos.CENTER);
                contentPane.setHgap(10);
                contentPane.setVgap(10);

                this.startersLabel = new Label();
                {
                    contentPane.add(this.startersLabel, 0, 0, 4, 1);

                    this.starters1ComboBox = new ComboBox<>();
                    contentPane.add(this.starters1ComboBox, 0, 1);
                    this.starters1ComboBox.setPrefWidth(colWidth);
                    this.starters1ComboBox.setOnAction(this::onStartersChanged);

                    this.starters2ComboBox = new ComboBox<>();
                    contentPane.add(this.starters2ComboBox, 1, 1);
                    this.starters2ComboBox.setPrefWidth(colWidth);
                    this.starters2ComboBox.setOnAction(this::onStartersChanged);

                    this.starters3ComboBox = new ComboBox<>();
                    contentPane.add(this.starters3ComboBox, 2, 1);
                    this.starters3ComboBox.setPrefWidth(colWidth);
                    this.starters3ComboBox.setOnAction(this::onStartersChanged);

                    this.startersButton = new Button();
                    contentPane.add(this.startersButton, 3, 1);
                    this.startersButton.setMinWidth(Button.USE_PREF_SIZE);
                    this.startersButton.setOnAction(this::onStartersApply);
                }

                this.textsLabel = new Label();
                {
                    contentPane.add(this.textsLabel, 0, 5);

                    final HBox textsHBox = new HBox();
                    {
                        contentPane.add(textsHBox, 0, 6);

                        textsHBox.setPrefWidth(colWidth);
                        textsHBox.setSpacing(10);

                        this.textsComboBox = new ComboBox<>();
                        textsHBox.getChildren().add(this.textsComboBox);
                        this.textsComboBox.prefWidthProperty().bind(textsHBox.widthProperty());
                        this.textsComboBox.setOnAction(this::onTextChanged);

                        this.textsButton = new Button();
                        this.textsButton.setMinWidth(Button.USE_PREF_SIZE);
                        textsHBox.getChildren().add(this.textsButton);
                        this.textsButton.setOnAction(this::onEditText);
                    }
                }

                this.trainersLabel = new Label();
                {
                    contentPane.add(this.trainersLabel, 1, 5, 3, 1);

                    final HBox trainersHBox = new HBox();
                    {
                        contentPane.add(trainersHBox, 1, 6, 3, 1);

                        trainersHBox.setSpacing(10);

                        this.trainersComboBox = new ComboBox<>();
                        this.trainersComboBox.prefWidthProperty().bind(trainersHBox.widthProperty());
                        trainersHBox.getChildren().add(this.trainersComboBox);

                        this.trainersButton = new Button();
                        this.trainersButton.setMinWidth(Button.USE_PREF_SIZE);
                        trainersHBox.getChildren().add(this.trainersButton);
                    }
                }
            }

            final TextArea textArea = new TextArea();
            {
                rootPane.setBottom(textArea);

                textArea.getStyleClass().add("log");
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxHeight(90);
                Log.logInto(textArea);
            }

            // Initial lock of the UI
            this.saveFileMenuItem.setDisable(true);
            this.startersLabel.setDisable(true);
            this.starters1ComboBox.setDisable(true);
            this.starters2ComboBox.setDisable(true);
            this.starters3ComboBox.setDisable(true);
            this.startersButton.setDisable(true);
            this.textsLabel.setDisable(true);
            this.textsComboBox.setDisable(true);
            this.textsButton.setDisable(true);
            this.trainersLabel.setDisable(true);
            this.trainersComboBox.setDisable(true);
            this.trainersButton.setDisable(true);
        }
        this.useLang();
        this.stage.setScene(new Scene(rootPane, 780, 320));
        this.stage.getScene().getStylesheets().add("style.css");
        this.stage.setResizable(false);

        this.stage.setTitle(PomData.NAME + " - Version " + PomData.VERSION);
        this.stage.setOnCloseRequest(this::onFileMenuQuitAction);
        this.stage.show();

        this.controller.appBuilt();
    }

    // Controller callbacks

    public void romLoaded(
        final int[] starters,
        final String[] pkmnNames,
        final int textsAmount,
        final String[] trainersData
    ) {
        this.fileMenu.setDisable(false);
        this.langMenu.setDisable(false);
        this.saveFileMenuItem.setDisable(false);
        this.startersLabel.setDisable(false);
        this.starters1ComboBox.setDisable(false);
        this.starters2ComboBox.setDisable(false);
        this.starters3ComboBox.setDisable(false);
        this.textsLabel.setDisable(false);
        this.textsComboBox.setDisable(false);
        this.trainersLabel.setDisable(false);
        this.trainersComboBox.setDisable(false);

        // Maybe we already have a ROM loaded, clean first
        this.starters1ComboBox.getItems().clear();
        this.starters2ComboBox.getItems().clear();
        this.starters3ComboBox.getItems().clear();
        this.textsComboBox.getItems().clear();
        this.trainersComboBox.getItems().clear();

        // Put things
        for (int i = 0; i < pkmnNames.length; i++) {
            final String item = String.format("%03d - %s", i + 1, pkmnNames[i]);
            this.starters1ComboBox.getItems().add(item);
            this.starters2ComboBox.getItems().add(item);
            this.starters3ComboBox.getItems().add(item);
        }
        this.starters1ComboBox.getSelectionModel().select(starters[0] - 1);
        this.starters2ComboBox.getSelectionModel().select(starters[1] - 1);
        this.starters3ComboBox.getSelectionModel().select(starters[2] - 1);
        IntStream.range(0, textsAmount).forEach(this.textsComboBox.getItems()::add);
        for (final String trainerData : trainersData) {
            this.trainersComboBox.getItems().add(trainerData);
        }
    }

    public void romSaved() {
        this.fileMenu.setDisable(false);
        this.langMenu.setDisable(false);
    }

    public void useLang() {
        try {
            final Lang l = this.controller.getLang();
            this.fileMenu.setText(l.get("ui_menu_file"));
            this.openFileMenuItem.setText(l.get("ui_menu_file_load"));
            this.saveFileMenuItem.setText(l.get("ui_menu_file_save"));
            this.quitFileMenuItem.setText(l.get("ui_menu_file_quit"));
            this.langMenu.setText(l.get("ui_menu_lang"));
            this.enLangMenuItem.setText(l.get("ui_menu_lang_en"));
            this.frLangMenuItem.setText(l.get("ui_menu_lang_fr"));
            this.startersLabel.setText(l.get("ui_main_startersLabel"));
            this.startersButton.setText(l.get("ui_main_startersApplyButton"));
            this.textsLabel.setText(l.get("ui_main_textsLabel"));
            this.textsButton.setText(l.get("ui_main_textEditButton"));
            this.trainersLabel.setText(l.get("ui_main_trainersLabel"));
            this.trainersButton.setText(l.get("ui_main_trainerEditButton"));
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    // Event handlers

    private void onFileMenuOpenAction(final Event e) {
        e.consume();
        try {
            final FileChooser fileChooser = this.getConfiguredFileChooser(true);
            final File file = fileChooser.showOpenDialog(this.stage);
            if (file != null) {
                this.fileMenu.setDisable(true);
                this.langMenu.setDisable(true);
                Log.info("Loading ROM, please wait...");
                Main.EXECUTOR.submit(() -> this.controller.loadRom(file));
            }
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    private void onFileMenuSaveAction(final Event e) {
        e.consume();
        try {
            final FileChooser fileChooser = this.getConfiguredFileChooser(false);
            final File file = fileChooser.showOpenDialog(this.stage);
            if (file != null) {
                this.fileMenu.setDisable(true);
                this.langMenu.setDisable(true);
                Main.EXECUTOR.submit(() -> this.controller.saveRom(file));
            }
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    private void onFileMenuQuitAction(final Event e) {
        e.consume();
        Main.EXECUTOR.submit(() -> {
            try {
                Log.info("Exiting.");
                Thread.sleep(150);
            } catch (final Throwable t) {
                Log.error(t);
                System.exit(1);
            }
            System.exit(0);
        });
    }

    private void onEnLangAction(final Event e) {
        e.consume();
        Main.EXECUTOR.submit(() -> this.controller.changeLang("en"));
    }

    private void onFrLangAction(final Event e) {
        e.consume();
        Main.EXECUTOR.submit(() -> this.controller.changeLang("fr"));
    }

    private void onStartersChanged(final Event e) {
        final int[] starters = new int[] {
            this.starters1ComboBox.getSelectionModel().getSelectedIndex() + 1,
            this.starters2ComboBox.getSelectionModel().getSelectedIndex() + 1,
            this.starters3ComboBox.getSelectionModel().getSelectedIndex() + 1
        };
        if (!Arrays.stream(starters).anyMatch(i -> i == 0)) {
            this.startersButton.setDisable(
                Arrays.equals(starters, this.controller.getStarters())
            );
        }
    }

    private void onStartersApply(final Event e) {
        final int[] starters = new int[] {
            this.starters1ComboBox.getSelectionModel().getSelectedIndex() + 1,
            this.starters2ComboBox.getSelectionModel().getSelectedIndex() + 1,
            this.starters3ComboBox.getSelectionModel().getSelectedIndex() + 1
        };
        this.startersButton.setDisable(true);
        Main.EXECUTOR.submit(() -> this.controller.setStarters(starters));
    }

    private void onTextChanged(final Event e) {
        this.textsButton.setDisable(this.textsComboBox.getSelectionModel().getSelectedIndex() < 0);
    }

    private void onEditText(final Event e) {
        try {
            this.openingSecondaryWindow();
            new EditTextView(
                this.controller,
                this.textsComboBox.getSelectionModel().getSelectedIndex()
            );
        } catch (final Throwable t) {
            Log.error(t);
        } finally {
            this.closingSecondaryWindow();
        }
    }

    private void openingSecondaryWindow() {
        if (++this.secondaryWindowCounter > 0) {
            this.fileMenu.setDisable(true);
            this.langMenu.setDisable(true);
        }
    }

    private void closingSecondaryWindow() {
        if (--this.secondaryWindowCounter == 0) {
            this.fileMenu.setDisable(false);
            this.langMenu.setDisable(false);
        }
    }

    // Tools

    private FileChooser getConfiguredFileChooser(final boolean open) {
        final FileChooser res = new FileChooser();
        res.setTitle(this.controller.getLang().get("ui_main_fileChooser_" + (open ? "open" : "save")));
        res.setInitialDirectory(new File(Constants.JAR_PATH));
        res.getExtensionFilters().addAll(
            new ExtensionFilter("NDS ROMs", "*.nds"),
            new ExtensionFilter("All Files", "*.*")
        );
        return res;
    }
}
