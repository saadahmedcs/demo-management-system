package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class CreateAccountDialog {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("i\\d{6}@nu\\.edu\\.pk");

  private CreateAccountDialog() {}

  public static void show(BackendApiClient api, Window owner) {
    var stage = new Stage();
    if (owner != null) {
      stage.initOwner(owner);
    }
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle("Create account");

    var emailField = new TextField();
    emailField.setPromptText("i220928@nu.edu.pk");
    emailField.getStyleClass().add("login-field");
    emailField.setMaxWidth(Double.MAX_VALUE);

    var nameField = new TextField();
    nameField.setPromptText("Full name");
    nameField.getStyleClass().add("login-field");
    nameField.setMaxWidth(Double.MAX_VALUE);

    var rollField = new TextField();
    rollField.setPromptText("Roll number");
    rollField.getStyleClass().add("login-field");
    rollField.setMaxWidth(Double.MAX_VALUE);

    var sectionField = new TextField();
    sectionField.setPromptText("Section (e.g., BCS-4A)");
    sectionField.getStyleClass().add("login-field");
    sectionField.setMaxWidth(Double.MAX_VALUE);

    var passField = new PasswordField();
    passField.setPromptText("At least 8 characters");
    passField.getStyleClass().add("login-field");
    passField.setMaxWidth(Double.MAX_VALUE);

    var confirmField = new PasswordField();
    confirmField.setPromptText("Confirm password");
    confirmField.getStyleClass().add("login-field");
    confirmField.setMaxWidth(Double.MAX_VALUE);

    var grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setMaxWidth(420);

    int r = 0;
    addRow(grid, r++, "University email", emailField);
    addRow(grid, r++, "Name", nameField);
    addRow(grid, r++, "Roll number", rollField);
    addRow(grid, r++, "Section", sectionField);
    addRow(grid, r++, "New password", passField);
    addRow(grid, r++, "Confirm password", confirmField);

    var errorLabel = new Label();
    errorLabel.getStyleClass().add("login-error");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    errorLabel.setWrapText(true);
    errorLabel.setMaxWidth(400);

    var createBtn = new Button("Create account");
    createBtn.getStyleClass().addAll("btn", "btn-primary");
    createBtn.setDefaultButton(true);

    var cancelBtn = new Button("Cancel");
    cancelBtn.getStyleClass().addAll("btn", "btn-ghost");
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(e -> stage.close());

    var buttonRow = new HBox(12, createBtn, cancelBtn);
    buttonRow.setAlignment(Pos.CENTER_LEFT);

    var root = new VBox(16, grid, errorLabel, buttonRow);
    root.setPadding(new Insets(28, 32, 28, 32));
    root.setAlignment(Pos.TOP_LEFT);
    root.getStyleClass().add("login-card");
    root.setMaxWidth(480);

    var wrap = new javafx.scene.layout.StackPane(root);
    wrap.getStyleClass().add("login-bg");
    wrap.setPadding(new Insets(24));

    var scene = new javafx.scene.Scene(wrap);
    scene.getStylesheets().add(CreateAccountDialog.class.getResource("/styles/app.css").toExternalForm());
    stage.setScene(scene);
    stage.setResizable(false);

    createBtn.setOnAction(
        e -> {
          errorLabel.setVisible(false);
          errorLabel.setManaged(false);

          var email = emailField.getText().trim().toLowerCase();
          if (!EMAIL_PATTERN.matcher(email).matches()) {
            showErr(errorLabel, "Enter a valid email (e.g. i220928@nu.edu.pk)");
            return;
          }
          var name = nameField.getText().trim();
          if (name.isEmpty()) {
            showErr(errorLabel, "Name is required.");
            return;
          }
          var roll = rollField.getText().trim();
          if (roll.isEmpty()) {
            showErr(errorLabel, "Roll number is required.");
            return;
          }
          var section = sectionField.getText().trim();
          var p1 = passField.getText();
          var p2 = confirmField.getText();
          if (p1.length() < 8) {
            showErr(errorLabel, "Password must be at least 8 characters.");
            return;
          }
          if (!p1.equals(p2)) {
            showErr(errorLabel, "Passwords do not match.");
            return;
          }

          createBtn.setDisable(true);
          cancelBtn.setDisable(true);
          var task =
              new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                  api.createAccount(email, name, roll, section, p1, p2);
                  return null;
                }
              };
          task.setOnSucceeded(
              ev -> {
                createBtn.setDisable(false);
                cancelBtn.setDisable(false);
                stage.close();
              });
          task.setOnFailed(
              ev -> {
                createBtn.setDisable(false);
                cancelBtn.setDisable(false);
                var msg = task.getException().getMessage();
                showErr(errorLabel, msg != null ? msg : "Could not create account.");
              });
          FxAsync.run(task);
        });

    stage.show();
  }

  private static void addRow(GridPane grid, int row, String labelText, TextField field) {
    var lab = new Label(labelText);
    lab.getStyleClass().add("field-label");
    GridPane.setHgrow(field, Priority.ALWAYS);
    grid.add(lab, 0, row);
    grid.add(field, 1, row);
  }

  private static void showErr(Label errorLabel, String msg) {
    errorLabel.setText(msg);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);
  }
}
