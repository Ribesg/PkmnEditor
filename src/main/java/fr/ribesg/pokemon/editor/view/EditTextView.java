package fr.ribesg.pokemon.editor.view;

import fr.ribesg.pokemon.editor.Main;
import fr.ribesg.pokemon.editor.controller.EditTextController;
import fr.ribesg.pokemon.editor.controller.MainController;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * @author Ribesg
 */
public final class EditTextView {

    private final EditTextController controller;

    private final Stage stage;

    private final ListView<String> textsList;
    private final TextArea         textArea;
    private final Button           saveButton;

    private String textWithVars;

    public EditTextView(final MainController mainController, final int index) {
        this.controller = new EditTextController(mainController, this, index);

        final BorderPane rootPane = new BorderPane();
        {
            final GridPane contentPane = new GridPane();
            {
                rootPane.setCenter(contentPane);

                contentPane.setHgap(10);
                contentPane.setVgap(10);

                this.textsList = new ListView<>();
                contentPane.add(this.textsList, 0, 0);
                this.textsList.minWidthProperty().bind(contentPane.widthProperty());
                this.textsList.setPrefHeight(500);
                this.textsList.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                this.textsList.getSelectionModel().selectedIndexProperty().addListener(
                    (obs, o, n) -> {
                        if (!Objects.equals(o, n)) {
                            this.onTextSelected(o.intValue(), n.intValue());
                        }
                    }
                );

                this.textArea = new TextArea();
                contentPane.add(this.textArea, 0, 1);
                this.textArea.minWidthProperty().bind(contentPane.widthProperty());
                this.textArea.setPrefHeight(500);
                this.textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

            final GridPane bottomLine = new GridPane();
            {
                rootPane.setBottom(bottomLine);

                bottomLine.setPadding(new Insets(5));

                final HBox bottomLineLeft = new HBox();
                {
                    bottomLine.add(bottomLineLeft, 0, 0);

                    bottomLineLeft.setAlignment(Pos.CENTER_LEFT);
                    bottomLineLeft.setSpacing(10);
                    bottomLineLeft.setPrefWidth(500);
                    bottomLineLeft.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    final CheckBox hideVarsButton = new CheckBox(
                        this.controller.getLang().get("ui_textEditor_hideVars")
                    );
                    bottomLineLeft.getChildren().add(hideVarsButton);
                    hideVarsButton.selectedProperty().addListener((obs, o, n) -> {
                        if (!o.equals(n)) {
                            this.onHideVars(n);
                        }
                    });
                }

                final HBox bottomLineRight = new HBox();
                {
                    bottomLine.add(bottomLineRight, 1, 0);

                    bottomLineRight.setAlignment(Pos.CENTER_RIGHT);
                    bottomLineRight.setSpacing(10);
                    bottomLineRight.setPrefWidth(500);
                    bottomLineRight.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    this.saveButton = new Button(
                        this.controller.getLang().get("ui_textEditor_applyButton")
                    );
                    bottomLineRight.getChildren().add(this.saveButton);
                    this.saveButton.setOnAction(this::onSave);

                    final Button cancelButton = new Button(
                        this.controller.getLang().get("ui_textEditor_cancelButton")
                    );
                    bottomLineRight.getChildren().add(cancelButton);
                    cancelButton.setCancelButton(true);
                    cancelButton.setOnAction(this::onCancel);
                }
            }
        }

        this.controller.getTexts().forEach(this.textsList.getItems()::add);

        this.stage = new Stage();
        this.stage.setTitle(this.controller.getLang().get("ui_textEditor_textEditor", index));
        this.stage.setScene(new Scene(rootPane, 800, 600));
        this.stage.getScene().getStylesheets().add("style.css");
        this.stage.showAndWait();
    }

    private void onTextSelected(final int old, final int now) {
        this.textsList.getItems().set(old, this.textArea.getText().replace("\n", "\\n"));
        this.textArea.clear();
        this.textArea.setText(this.textsList.getItems().get(now).replace("\\n", "\n"));
    }

    private void onHideVars(final boolean value) {
        if (value) {
            this.textsList.setDisable(true);
            this.textArea.setEditable(false);
            this.saveButton.setDisable(true);
            this.textWithVars = this.textArea.getText();
            this.textArea.setText(
                this.textWithVars.replaceAll("(\\\\[vz][0-9A-F]{4})+", "")
            );
        } else {
            this.textsList.setDisable(false);
            this.textArea.setEditable(true);
            this.saveButton.setDisable(false);
            this.textArea.setText(this.textWithVars);
            this.textWithVars = null;
        }
    }

    private void onSave(final Event e) {
        this.textsList.getItems().set(
            this.textsList.getSelectionModel().getSelectedIndex(),
            this.textArea.getText().replace("\n", "\\n")
        );
        Main.EXECUTOR.submit(() -> this.controller.saveTexts(this.textsList.getItems()));
    }

    private void onCancel(final Event e) {
        this.close();
    }

    public void close() {
        this.stage.close();
    }
}
