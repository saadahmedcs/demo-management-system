package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.TimetableEntryDto;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TimetableView {

  static final String[] PERIOD_LABELS = {
    "08:30 – 09:50",
    "10:00 – 11:20",
    "11:30 – 12:50",
    "13:00 – 14:20",
    "14:30 – 15:55",
    "16:00 – 17:15"
  };

  private static final DayOfWeek[] DAYS = {
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
  };

  private static final String[] DAY_LABELS = { "Mon", "Tue", "Wed", "Thu", "Fri" };

  private final BackendApiClient api;
  private final String email;
  private final Consumer<List<TimetableEntryDto>> onSaved;

  // checkboxes[periodIndex][dayIndex]
  private final CheckBox[][] checkboxes = new CheckBox[PERIOD_LABELS.length][DAYS.length];

  public TimetableView(BackendApiClient api, String email, Consumer<List<TimetableEntryDto>> onSaved) {
    this.api = api;
    this.email = email;
    this.onSaved = onSaved;
  }

  public void show() {
    var stage = new Stage();
    stage.setTitle("My Class Timetable");
    stage.setResizable(false);

    var root = new VBox(16);
    root.getStyleClass().add("app-root");
    root.setPadding(new Insets(24));

    var header = new Label("Select your class time slots");
    header.getStyleClass().add("course-title");

    var sub = new Label("Slots that clash with your classes will be locked during booking.");
    sub.getStyleClass().add("course-subtitle");

    root.getChildren().addAll(header, sub, buildGrid());

    var saveBtn = new Button("Save timetable");
    saveBtn.getStyleClass().addAll("btn", "btn-primary");
    saveBtn.setOnAction(e -> onSave(stage));

    var btnRow = new HBox(saveBtn);
    btnRow.setAlignment(Pos.CENTER_RIGHT);
    root.getChildren().add(btnRow);

    var scene = new Scene(root, 540, 370);
    scene.getStylesheets().add(MainView.class.getResource("/styles/app.css").toExternalForm());
    stage.setScene(scene);
    stage.show();

    loadExisting();
  }

  private GridPane buildGrid() {
    var grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(8, 0, 8, 0));

    // Header row: day names starting at column 1
    for (int d = 0; d < DAYS.length; d++) {
      var lbl = new Label(DAY_LABELS[d]);
      lbl.getStyleClass().add("field-label");
      lbl.setMinWidth(70);
      lbl.setAlignment(Pos.CENTER);
      grid.add(lbl, d + 1, 0);
    }

    // Period rows
    for (int p = 0; p < PERIOD_LABELS.length; p++) {
      var periodLbl = new Label(PERIOD_LABELS[p]);
      periodLbl.getStyleClass().add("course-subtitle");
      periodLbl.setMinWidth(130);
      grid.add(periodLbl, 0, p + 1);

      for (int d = 0; d < DAYS.length; d++) {
        var cb = new CheckBox();
        cb.setStyle("-fx-padding: 0 0 0 18;");
        checkboxes[p][d] = cb;
        grid.add(cb, d + 1, p + 1);
      }
    }

    return grid;
  }

  private void loadExisting() {
    var task = new Task<List<TimetableEntryDto>>() {
      @Override
      protected List<TimetableEntryDto> call() throws Exception {
        return api.listTimetable(email);
      }
    };
    task.setOnSucceeded(e -> {
      for (var entry : task.getValue()) {
        int d = dayIndex(entry.dayOfWeek());
        int p = entry.periodIndex();
        if (d >= 0 && p >= 0 && p < PERIOD_LABELS.length) {
          checkboxes[p][d].setSelected(true);
        }
      }
    });
    FxAsync.run(task);
  }

  private void onSave(Stage stage) {
    var selected = new ArrayList<TimetableEntryDto>();
    for (int p = 0; p < PERIOD_LABELS.length; p++) {
      for (int d = 0; d < DAYS.length; d++) {
        if (checkboxes[p][d].isSelected()) {
          selected.add(new TimetableEntryDto(DAYS[d].name(), p));
        }
      }
    }

    var task = new Task<List<TimetableEntryDto>>() {
      @Override
      protected List<TimetableEntryDto> call() throws Exception {
        return api.saveTimetable(email, selected);
      }
    };
    task.setOnSucceeded(e -> {
      onSaved.accept(task.getValue());
      stage.close();
    });
    task.setOnFailed(e -> FxAsync.showError("Save timetable failed", task.getException()));
    FxAsync.run(task);
  }

  private static int dayIndex(String dayOfWeek) {
    for (int i = 0; i < DAYS.length; i++) {
      if (DAYS[i].name().equals(dayOfWeek)) return i;
    }
    return -1;
  }
}
