package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import java.net.URI;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("i\\d{6}@nu\\.edu\\.pk");
  private static final BackendApiClient api = new BackendApiClient(URI.create("http://localhost:8080"));

  private final StackPane root = new StackPane();

  public LoginView(BiConsumer<String, String> onLogin) {
    root.getStyleClass().add("login-bg");

    var card = new VBox(18);
    card.getStyleClass().add("login-card");
    card.setMaxWidth(440);
    card.setPadding(new Insets(44, 44, 44, 44));
    card.setAlignment(Pos.CENTER_LEFT);

    var title = new Label("Demo Management System");
    title.getStyleClass().add("app-title");

    var subtitle = new Label("Sign in to continue");
    subtitle.getStyleClass().add("empty-subtitle");

    var titleBox = new VBox(6, title, subtitle);

    var emailLabel = new Label("University Email");
    emailLabel.getStyleClass().add("field-label");

    var emailField = new TextField();
    emailField.setPromptText("i220928@nu.edu.pk");
    emailField.getStyleClass().add("login-field");
    emailField.setMaxWidth(Double.MAX_VALUE);

    var passwordLabel = new Label("Password");
    passwordLabel.getStyleClass().add("field-label");

    var passwordField = new PasswordField();
    passwordField.setPromptText("Your password");
    passwordField.getStyleClass().add("login-field");
    passwordField.setMaxWidth(Double.MAX_VALUE);

    var roleLabel = new Label("Role");
    roleLabel.getStyleClass().add("field-label");

    var group = new ToggleGroup();
    var studentRb = new RadioButton("Student");
    studentRb.setToggleGroup(group);
    studentRb.getStyleClass().add("login-radio");
    studentRb.setSelected(true);
    var taRb = new RadioButton("Teaching Assistant");
    taRb.setToggleGroup(group);
    taRb.getStyleClass().add("login-radio");

    var roleRow = new HBox(28, studentRb, taRb);
    roleRow.setAlignment(Pos.CENTER_LEFT);

    var errorLabel = new Label();
    errorLabel.getStyleClass().add("login-error");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    var loginBtn = new Button("Log in");
    loginBtn.getStyleClass().addAll("btn", "btn-primary");
    loginBtn.setMaxWidth(Double.MAX_VALUE);

    var createAccountBtn = new Button("Create account");
    createAccountBtn.getStyleClass().addAll("btn", "btn-ghost");
    createAccountBtn.setMaxWidth(Double.MAX_VALUE);
    createAccountBtn.setOnAction(
        e -> {
          var win = root.getScene() != null ? root.getScene().getWindow() : null;
          CreateAccountDialog.show(api, win);
        });

    loginBtn.setOnAction(e -> {
      var email = emailField.getText().trim().toLowerCase();
      if (!EMAIL_PATTERN.matcher(email).matches()) {
        showError(errorLabel, "Enter a valid email (e.g. i220928@nu.edu.pk)");
        return;
      }
      var password = passwordField.getText();
      if (password.isEmpty()) {
        showError(errorLabel, "Password is required.");
        return;
      }

      String role = taRb.isSelected() ? "TA" : "STUDENT";
      loginBtn.setDisable(true);
      loginBtn.setText("Signing in…");

      var task = new Task<String>() {
        @Override
        protected String call() throws Exception {
          var result = api.login(email, password, role);
          return result.role();
        }
      };

      task.setOnSucceeded(ev -> {
        loginBtn.setDisable(false);
        loginBtn.setText("Log in");
        onLogin.accept(task.getValue(), email);
      });

      task.setOnFailed(ev -> {
        loginBtn.setDisable(false);
        loginBtn.setText("Log in");
        var msg = task.getException().getMessage();
        if (msg != null && msg.contains("Teaching Assistant")) {
          taRb.setSelected(true);
          studentRb.setDisable(true);
        } else if (msg != null && msg.contains("Student")) {
          studentRb.setSelected(true);
          taRb.setDisable(true);
        }
        showError(errorLabel, msg != null ? msg : "Login failed. Is the backend running?");
      });

      FxAsync.run(task);
    });

    emailField.setOnAction(e -> loginBtn.fire());
    passwordField.setOnAction(e -> loginBtn.fire());

    card.getChildren()
        .addAll(
            titleBox,
            emailLabel,
            emailField,
            passwordLabel,
            passwordField,
            roleLabel,
            roleRow,
            errorLabel,
            loginBtn,
            createAccountBtn);
    root.getChildren().add(card);
    StackPane.setAlignment(card, Pos.CENTER);
  }

  private void showError(Label label, String msg) {
    label.setText(msg);
    label.setVisible(true);
    label.setManaged(true);
  }

  public Parent getRoot() {
    return root;
  }
}
