package fr.ribesg.pokemon.editor.gui;

import fr.ribesg.pokemon.editor.Context;
import fr.ribesg.pokemon.editor.PomData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Ribesg
 */
public final class MainApplication extends Application {

    public static void launch() {
        Application.launch();
    }

    private Context context;

    private BorderPane rootPane;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.context = new Context();

        this.rootPane = new BorderPane();
        {
            final MenuBar menu = new MenuBar();
            {
                final Menu fileMenu = new Menu("File");
                {
                    final MenuItem openFileMenuItem = new MenuItem("Open");
                    fileMenu.getItems().add(openFileMenuItem);

                    final MenuItem saveFileMenuItem = new MenuItem("Save");
                    fileMenu.getItems().add(saveFileMenuItem);

                    final MenuItem quitFileMenuItem = new MenuItem("Quit");
                    fileMenu.getItems().add(quitFileMenuItem);
                }
                menu.getMenus().add(fileMenu);

                final Menu langMenu = new Menu("Language");
                {
                    final MenuItem enLangMenuItem = new MenuItem("English");
                    langMenu.getItems().add(enLangMenuItem);

                    final MenuItem frLangMenuItem = new MenuItem("Français");
                    langMenu.getItems().add(frLangMenuItem);
                }
                menu.getMenus().add(langMenu);
            }
            this.rootPane.setTop(menu);

            this.rootPane.setBottom(new TextArea());
        }
        primaryStage.setScene(new Scene(this.rootPane, 800, 600));

        primaryStage.setTitle(PomData.NAME + " - Version " + PomData.VERSION);
        primaryStage.show();
    }
}
