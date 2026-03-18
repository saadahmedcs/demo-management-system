package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.MessageDto;
import com.demo.client.model.Course;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChatDialog {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  private final BackendApiClient api;
  private final Course course;
  private final String email;

  private final VBox messageList = new VBox(8);
  private final ScrollPane scroll = new ScrollPane(messageList);
  private final Timeline poller = new Timeline();
  private String lastMessageId = null;

  public ChatDialog(BackendApiClient api, Course course, String email) {
    this.api = api;
    this.course = course;
    this.email = email;
  }

  public void show() {
    var stage = new Stage();
    String taLabel = course.getTaEmail() != null ? " (TA: " + course.getTaEmail() + ")" : "";
    stage.setTitle("Messages — " + course.displayName() + taLabel);
    stage.setMinWidth(460);
    stage.setMinHeight(520);

    var root = new BorderPane();
    root.getStyleClass().add("app-root");

    // Header
    var header = new HBox();
    header.getStyleClass().add("topbar");
    header.setPadding(new Insets(12, 16, 12, 16));
    header.setAlignment(Pos.CENTER_LEFT);
    String taInfo = course.getTaEmail() != null ? "TA: " + course.getTaEmail() : "";
    var titleText = course.displayName() + " — Messages" + (taInfo.isEmpty() ? "" : "\n" + taInfo);
    var title = new Label(titleText);
    title.getStyleClass().add("course-title");
    header.getChildren().add(title);
    root.setTop(header);

    // Message area
    messageList.setPadding(new Insets(12));
    messageList.setFillWidth(true);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("chat-scroll");
    scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    root.setCenter(scroll);

    // Input bar
    var inputField = new TextField();
    inputField.setPromptText("Type a message…");
    inputField.getStyleClass().add("login-field");
    HBox.setHgrow(inputField, Priority.ALWAYS);

    var sendBtn = new Button("Send");
    sendBtn.getStyleClass().addAll("btn", "btn-primary");
    sendBtn.setOnAction(e -> sendMessage(inputField));
    inputField.setOnAction(e -> sendMessage(inputField));

    var inputBar = new HBox(8, inputField, sendBtn);
    inputBar.setPadding(new Insets(10, 12, 10, 12));
    inputBar.setAlignment(Pos.CENTER);
    inputBar.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0;");
    root.setBottom(inputBar);

    var scene = new Scene(root, 500, 580);
    scene.getStylesheets().add(MainView.class.getResource("/styles/app.css").toExternalForm());
    stage.setScene(scene);
    stage.show();

    // Start polling
    poller.getKeyFrames().add(new KeyFrame(Duration.seconds(3), e -> pollMessages()));
    poller.setCycleCount(Timeline.INDEFINITE);
    poller.play();
    stage.setOnHidden(e -> poller.stop());

    // Initial load
    pollMessages();
  }

  private void pollMessages() {
    var task = new Task<List<MessageDto>>() {
      @Override
      protected List<MessageDto> call() throws Exception {
        return api.listMessages(course.getId());
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> refreshMessages(task.getValue())));
    FxAsync.run(task);
  }

  private void refreshMessages(List<MessageDto> messages) {
    if (messages.isEmpty()) return;
    String newestId = messages.getLast().id().toString();
    if (newestId.equals(lastMessageId)) return;
    lastMessageId = newestId;

    messageList.getChildren().clear();
    for (var msg : messages) {
      messageList.getChildren().add(buildBubble(msg));
    }
    // Scroll to bottom
    scroll.layout();
    scroll.setVvalue(1.0);
  }

  private HBox buildBubble(MessageDto msg) {
    boolean mine = email.equals(msg.senderEmail());

    var text = new Label(msg.text());
    text.setWrapText(true);
    text.setMaxWidth(300);
    text.setStyle(
        "-fx-background-color: " + (mine ? "rgba(56,189,248,0.18)" : "rgba(255,255,255,0.07)") + ";"
        + "-fx-background-radius: 12;"
        + "-fx-padding: 8 12 8 12;"
        + "-fx-text-fill: #e5e7eb;");

    var sender = new Label(mine ? "You" : msg.senderEmail());
    sender.setStyle("-fx-text-fill: rgba(229,231,235,0.5); -fx-font-size: 11px;");

    var time = new Label(msg.sentAt() != null ? TIME_FMT.format(msg.sentAt()) : "");
    time.setStyle("-fx-text-fill: rgba(229,231,235,0.4); -fx-font-size: 10px;");

    var meta = new HBox(6, sender, time);
    meta.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    var bubble = new VBox(3, meta, text);
    bubble.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    var row = new HBox(bubble);
    if (mine) {
      row.getChildren().add(0, spacer);
    } else {
      row.getChildren().add(spacer);
    }
    row.setPadding(new Insets(2, 0, 2, 0));
    return row;
  }

  private void sendMessage(TextField inputField) {
    var text = inputField.getText().trim();
    if (text.isBlank()) return;
    inputField.clear();

    var task = new Task<MessageDto>() {
      @Override
      protected MessageDto call() throws Exception {
        return api.sendMessage(course.getId(), email, text);
      }
    };
    task.setOnSucceeded(e -> pollMessages());
    task.setOnFailed(e -> FxAsync.showError("Send failed", task.getException()));
    FxAsync.run(task);
  }
}
