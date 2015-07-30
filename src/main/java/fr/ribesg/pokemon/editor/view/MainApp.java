package fr.ribesg.pokemon.editor.view;

import fr.ribesg.pokemon.editor.*;
import fr.ribesg.pokemon.editor.controller.MainAppController;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Ribesg
 */
public final class MainApp extends Application {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static void launch() {
        Application.launch();
    }

    private MainAppController controller;

    private Stage stage;

    private BorderPane        rootPane;
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

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.controller = new MainAppController(this);

        this.stage = primaryStage;

        this.rootPane = new BorderPane();
        {
            final MenuBar menu = new MenuBar();
            {
                final Menu fileMenu = new Menu("File");
                {
                    final MenuItem openFileMenuItem = new MenuItem("Open");
                    openFileMenuItem.setOnAction(this::onFileMenuOpenAction);
                    fileMenu.getItems().add(openFileMenuItem);

                    this.saveFileMenuItem = new MenuItem("Save");
                    this.saveFileMenuItem.setOnAction(this::onFileMenuSaveAction);
                    fileMenu.getItems().add(this.saveFileMenuItem);

                    final MenuItem quitFileMenuItem = new MenuItem("Quit");
                    quitFileMenuItem.setOnAction(this::onFileMenuQuitAction);
                    fileMenu.getItems().add(quitFileMenuItem);
                }
                menu.getMenus().add(fileMenu);

                final Menu langMenu = new Menu("Language");
                {
                    final MenuItem enLangMenuItem = new MenuItem("English");
                    enLangMenuItem.setOnAction(this::onEnLangAction);
                    langMenu.getItems().add(enLangMenuItem);

                    final MenuItem frLangMenuItem = new MenuItem("French");
                    frLangMenuItem.setOnAction(this::onFrLangAction);
                    langMenu.getItems().add(frLangMenuItem);
                }
                menu.getMenus().add(langMenu);
            }
            this.rootPane.setTop(menu);

            final GridPane contentPane = new GridPane();
            {
                contentPane.setAlignment(Pos.CENTER);
                contentPane.setHgap(10);
                contentPane.setVgap(10);

                this.startersLabel = new Label("Starters Edition");
                {
                    contentPane.add(this.startersLabel, 0, 0, 4, 1);

                    this.starters1ComboBox = new ComboBox<>();
                    this.starters1ComboBox.setMinWidth(160);
                    contentPane.add(this.starters1ComboBox, 0, 1);
                    this.starters2ComboBox = new ComboBox<>();
                    this.starters2ComboBox.setMinWidth(160);
                    contentPane.add(this.starters2ComboBox, 1, 1);
                    this.starters3ComboBox = new ComboBox<>();
                    this.starters3ComboBox.setMinWidth(160);
                    contentPane.add(this.starters3ComboBox, 2, 1);

                    this.startersButton = new Button("Apply");
                    contentPane.add(this.startersButton, 3, 1);
                }

                this.textsLabel = new Label("Texts Edition");
                {
                    contentPane.add(this.textsLabel, 0, 5);

                    final HBox textsHBox = new HBox();
                    {
                        textsHBox.setSpacing(10);

                        this.textsComboBox = new ComboBox<>();
                        this.textsComboBox.setMinWidth(88);
                        textsHBox.getChildren().add(this.textsComboBox);

                        this.textsButton = new Button("Edit Text");
                        textsHBox.getChildren().add(this.textsButton);
                    }
                    contentPane.add(textsHBox, 0, 6);
                }

                this.trainersLabel = new Label("Trainers Edition");
                {
                    contentPane.add(this.trainersLabel, 1, 5, 3, 1);

                    final HBox trainersHBox = new HBox();
                    {
                        trainersHBox.setSpacing(10);

                        this.trainersComboBox = new ComboBox<>();
                        this.trainersComboBox.setMinWidth(300);
                        trainersHBox.getChildren().add(this.trainersComboBox);

                        this.trainersButton = new Button("Edit Trainer");
                        trainersHBox.getChildren().add(this.trainersButton);
                    }
                    contentPane.add(trainersHBox, 1, 6, 3, 1);
                }
            }
            this.rootPane.setCenter(contentPane);

            final TextArea textArea = new TextArea();
            {
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxSize(Double.MAX_VALUE, 90);
                Log.logInto(textArea);
            }
            this.rootPane.setBottom(textArea);

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
        this.stage.setScene(new Scene(this.rootPane, 640, 320));
        this.stage.setResizable(false);

        this.stage.setTitle(PomData.NAME + " - Version " + PomData.VERSION);
        this.stage.setOnCloseRequest(this::onFileMenuQuitAction);
        this.stage.show();

        Log.info("Started!");
        Log.info("First, load a HG/SS ROM using File -> Open");
    }

    // Controller callbacks

    public void romLoaded() {
        this.saveFileMenuItem.setDisable(false);
        this.startersLabel.setDisable(false);
        this.starters1ComboBox.setDisable(false);
        this.starters2ComboBox.setDisable(false);
        this.starters3ComboBox.setDisable(false);
        this.textsLabel.setDisable(false);
        this.textsComboBox.setDisable(false);
        this.trainersLabel.setDisable(false);
        this.trainersComboBox.setDisable(false);
    }

    public void fillComboBoxes(
        final int[] starters,
        final String[] pkmnNames,
        final int textsAmount,
        final String[] trainersData
    ) {
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

    // Event handlers

    private void onFileMenuOpenAction(final Event e) {
        e.consume();
        try {
            final FileChooser fileChooser = this.getConfiguredFileChooser(true);
            final File file = fileChooser.showOpenDialog(this.stage);
            if (file != null) {
                this.controller.loadRom(file);
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
                this.controller.saveRom(file);
            }
        } catch (final Throwable t) {
            Log.error(t);
        }
    }

    private void onFileMenuQuitAction(final Event e) {
        e.consume();
        MainApp.EXECUTOR.submit(() -> {
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
        // TODO
    }

    private void onFrLangAction(final Event e) {
        e.consume();
        // TODO
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
