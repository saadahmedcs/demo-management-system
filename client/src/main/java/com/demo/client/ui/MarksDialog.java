package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.model.Course;
import com.demo.client.model.DemoSlot;
import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MarksDialog {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  private record MarkRow(
      DemoSlot slot,
      SimpleStringProperty score,
      SimpleStringProperty feedback) {}

  private final BackendApiClient api;
  private final Course course;
  private final ObservableList<MarkRow> rows = FXCollections.observableArrayList();
  private final Stage stage = new Stage();

  public MarksDialog(BackendApiClient api, Course course, List<DemoSlot> allSlots) {
    this.api = api;
    this.course = course;
    allSlots.stream()
        .filter(s -> s.getStudentEmail() != null)
        .forEach(s -> rows.add(new MarkRow(s, new SimpleStringProperty(""), new SimpleStringProperty(""))));
  }

  public void show() {
    var title = new Label("Marks — " + course.displayName());
    title.getStyleClass().add("course-title");

    if (rows.isEmpty()) {
      var empty = new Label("No booked slots yet — marks will appear here once students book slots.");
      empty.getStyleClass().add("empty-subtitle");
      var root = new VBox(16, title, empty);
      root.getStyleClass().add("app-root");
      root.setPadding(new Insets(20));
      var scene = new Scene(root, 600, 200);
      applyStylesheet(scene);
      stage.setScene(scene);
      stage.setTitle("Marks — " + course.displayName());
      stage.show();
      return;
    }

    var table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    var saveAllBtn = new Button("Save All");
    saveAllBtn.getStyleClass().addAll("btn", "btn-primary");
    saveAllBtn.setOnAction(e -> onSaveAll(saveAllBtn));

    var exportBtn = new Button("Export CSV");
    exportBtn.getStyleClass().addAll("btn", "btn-ghost");
    exportBtn.setOnAction(e -> onExportCsv());

    var btnRow = new HBox(10, saveAllBtn, exportBtn);
    btnRow.setAlignment(Pos.CENTER_RIGHT);

    var root = new VBox(14, title, table, btnRow);
    root.getStyleClass().add("app-root");
    root.setPadding(new Insets(20));

    var scene = new Scene(root, 860, 520);
    applyStylesheet(scene);
    stage.setScene(scene);
    stage.setTitle("Marks — " + course.displayName());
    stage.initModality(Modality.NONE);
    stage.show();

    loadMarks();
  }

  private TableView<MarkRow> buildTable() {
    var table = new TableView<>(rows);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    table.getStyleClass().add("slots-table");

    var dateCol = new TableColumn<MarkRow, String>("Date");
    dateCol.setCellValueFactory(r -> new SimpleStringProperty(
        r.getValue().slot().getDate() == null ? "" : DATE_FMT.format(r.getValue().slot().getDate())));
    dateCol.setMinWidth(160);

    var startCol = new TableColumn<MarkRow, String>("Start");
    startCol.setCellValueFactory(r -> new SimpleStringProperty(
        r.getValue().slot().getStartTime() == null ? "" : TIME_FMT.format(r.getValue().slot().getStartTime())));
    startCol.setMinWidth(70);

    var studentCol = new TableColumn<MarkRow, String>("Student");
    studentCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().slot().getStudentEmail()));
    studentCol.setMinWidth(200);

    var scoreCol = new TableColumn<MarkRow, Void>("Score");
    scoreCol.setMinWidth(100);
    scoreCol.setCellFactory(col -> new BoundTextFieldCell(true));

    var feedbackCol = new TableColumn<MarkRow, Void>("Feedback");
    feedbackCol.setMinWidth(220);
    feedbackCol.setCellFactory(col -> new BoundTextFieldCell(false));

    table.getColumns().setAll(List.of(dateCol, startCol, studentCol, scoreCol, feedbackCol));
    return table;
  }

  /** A TableCell that renders a TextField always-visible, bound to the row's score or feedback. */
  private static class BoundTextFieldCell extends TableCell<MarkRow, Void> {
    private final TextField tf = new TextField();
    private final boolean isScore;
    private MarkRow boundRow = null;

    BoundTextFieldCell(boolean isScore) {
      this.isScore = isScore;
      tf.setMaxWidth(Double.MAX_VALUE);
      tf.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-text-base-color;");
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
        setGraphic(null);
        unbind();
        return;
      }
      var row = getTableView().getItems().get(getIndex());
      if (boundRow != row) {
        unbind();
        var prop = isScore ? row.score() : row.feedback();
        tf.textProperty().bindBidirectional(prop);
        boundRow = row;
      }
      setGraphic(tf);
    }

    private void unbind() {
      if (boundRow != null) {
        var prop = isScore ? boundRow.score() : boundRow.feedback();
        tf.textProperty().unbindBidirectional(prop);
        boundRow = null;
      }
    }
  }

  private void loadMarks() {
    var task = new Task<List<com.demo.client.api.dto.MarkDto>>() {
      @Override
      protected List<com.demo.client.api.dto.MarkDto> call() throws Exception {
        return api.listMarks(course.getId());
      }
    };
    task.setOnSucceeded(e -> {
      var marks = task.getValue();
      for (var row : rows) {
        marks.stream()
            .filter(m -> row.slot().getId().equals(m.slotId()))
            .findFirst()
            .ifPresent(m -> {
              row.score().set(m.score() != null ? String.valueOf(m.score()) : "");
              row.feedback().set(m.feedback() != null ? m.feedback() : "");
            });
      }
    });
    FxAsync.run(task);
  }

  private void onSaveAll(Button saveBtn) {
    saveBtn.setDisable(true);
    saveBtn.setText("Saving…");

    var task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        for (var row : rows) {
          Double score = null;
          var scoreText = row.score().get().trim();
          if (!scoreText.isEmpty()) {
            try { score = Double.parseDouble(scoreText); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid score for " + row.slot().getStudentEmail() + ": must be a number."); }
          }
          var feedback = row.feedback().get().trim();
          api.saveMark(course.getId(), row.slot().getId(), row.slot().getStudentEmail(), score, feedback.isEmpty() ? null : feedback);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      saveBtn.setDisable(false);
      saveBtn.setText("Save All");
    });
    task.setOnFailed(e -> {
      saveBtn.setDisable(false);
      saveBtn.setText("Save All");
      FxAsync.showError("Save marks failed", task.getException());
    });
    FxAsync.run(task);
  }

  private void onExportCsv() {
    var chooser = new FileChooser();
    chooser.setTitle("Export Marks as CSV");
    chooser.setInitialFileName(course.getCode() + "_marks.csv");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
    File file = chooser.showSaveDialog(stage);
    if (file == null) return;

    try (var pw = new PrintWriter(file)) {
      pw.println("Date,Start,Student,Score,Feedback");
      for (var row : rows) {
        var date = row.slot().getDate() == null ? "" : DATE_FMT.format(row.slot().getDate());
        var start = row.slot().getStartTime() == null ? "" : TIME_FMT.format(row.slot().getStartTime());
        var student = row.slot().getStudentEmail();
        var score = row.score().get().trim();
        var feedback = "\"" + row.feedback().get().trim().replace("\"", "\"\"") + "\"";
        pw.printf("%s,%s,%s,%s,%s%n", date, start, student, score, feedback);
      }
    } catch (Exception ex) {
      FxAsync.showError("Export failed", ex);
    }
  }

  private void applyStylesheet(Scene scene) {
    var css = getClass().getResource("/com/demo/client/styles.css");
    if (css != null) scene.getStylesheets().add(css.toExternalForm());
  }
}
