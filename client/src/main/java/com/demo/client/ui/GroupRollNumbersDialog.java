package com.demo.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** One narrow text field per vacant seat in the same timeslot; at least one non-blank value is required. */
final class GroupRollNumbersDialog {

  private GroupRollNumbersDialog() {}

  /**
   * @param fieldCount number of text fields (open seats in this timeslot window)
   * @return distinct trimmed rolls, or empty if cancelled
   */
  static Optional<List<String>> show(int fieldCount, String courseDisplayName) {
    if (fieldCount < 1) {
      return Optional.of(List.of());
    }

    var dialog = new Dialog<List<String>>();
    dialog.setTitle("Group roll numbers");
    dialog.setHeaderText(
        "This timeslot has "
            + fieldCount
            + " open seat"
            + (fieldCount == 1 ? "" : "s")
            + " in "
            + courseDisplayName
            + ".\n"
            + "Enter one roll number per box (optional boxes can stay empty). At least one entry is required; each roll must belong to a student enrolled in this course.");

    var fields = new ArrayList<TextField>(fieldCount);
    var fieldBox = new VBox(6);
    for (int i = 0; i < fieldCount; i++) {
      var tf = new TextField();
      tf.setPromptText("Roll " + (i + 1));
      tf.setMaxWidth(168);
      tf.setPrefColumnCount(14);
      var row = new HBox();
      row.setPadding(new Insets(0, 20, 0, 20));
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      row.getChildren().addAll(tf, spacer);
      fields.add(tf);
      fieldBox.getChildren().add(row);
    }

    var hint =
        new Label(
            "Empty boxes are ignored. The server checks enrollment and assigns teammates to the other open rows in this slot.");
    hint.setWrapText(true);
    hint.getStyleClass().add("course-subtitle");
    hint.setPadding(new Insets(0, 20, 0, 20));

    var root = new VBox(10, hint, fieldBox);
    root.setPadding(new Insets(8, 8, 8, 8));

    DialogPane pane = dialog.getDialogPane();
    pane.setContent(root);
    pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    var okBtn = (Button) pane.lookupButton(ButtonType.OK);
    okBtn.addEventFilter(
        ActionEvent.ACTION,
        ev -> {
          if (collectRolls(fields).isEmpty()) {
            ev.consume();
            FxAsync.showError(
                "Roll numbers required",
                new IllegalArgumentException("Enter at least one roll number."));
          }
        });

    dialog.setResultConverter(btn -> btn == ButtonType.OK ? collectRolls(fields) : null);

    return dialog.showAndWait();
  }

  private static List<String> collectRolls(List<TextField> fields) {
    return fields.stream()
        .map(TextField::getText)
        .map(s -> s == null ? "" : s.trim())
        .filter(s -> !s.isBlank())
        .distinct()
        .toList();
  }
}
