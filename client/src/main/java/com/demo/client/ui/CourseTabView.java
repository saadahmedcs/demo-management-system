package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.GenerateSlotsRequest;
import com.demo.client.model.Course;
import com.demo.client.model.DemoSlot;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class CourseTabView extends BorderPane {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  private final BackendApiClient api;
  private final Course course;
  private final ObservableList<DemoSlot> slots;
  private final TableView<DemoSlot> table = new TableView<>();
  private final Label subtitle = new Label();
  private final java.util.function.Consumer<String> onTabNameChanged;

  public CourseTabView(BackendApiClient api, Course course, java.util.function.Consumer<String> onTabNameChanged) {
    this.api = api;
    this.course = course;
    this.onTabNameChanged = onTabNameChanged;
    this.slots = FXCollections.observableArrayList(course.getSlots());

    getStyleClass().add("course-tab");
    setPadding(new Insets(16));

    setTop(buildHeader());
    setCenter(buildTable());

    refreshSubtitle();
    loadSlots();
  }

  private Node buildHeader() {
    var wrap = new HBox(12);
    wrap.getStyleClass().add("course-header");
    wrap.setPadding(new Insets(0, 0, 12, 0));
    wrap.setAlignment(Pos.CENTER_LEFT);

    var title = new Label(course.displayName());
    title.getStyleClass().add("course-title");

    subtitle.getStyleClass().add("course-subtitle");

    var titleWrap = new VBox2(2, title, subtitle);
    HBox.setHgrow(titleWrap, Priority.ALWAYS);

    var rename = new Button("Rename");
    rename.getStyleClass().addAll("btn", "btn-ghost");
    rename.setTooltip(new Tooltip("Update course tab name"));
    rename.setOnAction(e -> onRename(title));

    var generate = new Button("Generate slots");
    generate.getStyleClass().addAll("btn", "btn-primary");
    generate.setTooltip(new Tooltip("Automatically create demo slots"));
    generate.setOnAction(e -> onGenerateSlots());

    var clear = new Button("Clear");
    clear.getStyleClass().addAll("btn", "btn-danger");
    clear.setTooltip(new Tooltip("Remove all slots in this course"));
    clear.setOnAction(e -> onClearSlots());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    wrap.getChildren().addAll(titleWrap, spacer, rename, generate, clear);
    return wrap;
  }

  private Node buildTable() {
    table.getStyleClass().add("slots-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    table.setItems(slots);

    var dateCol = new TableColumn<DemoSlot, java.time.LocalDate>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    dateCol.setCellFactory(col -> new PrettyCell<>(d -> d == null ? "" : DATE_FMT.format(d)));
    dateCol.setMinWidth(240);

    var startCol = new TableColumn<DemoSlot, java.time.LocalTime>("Start");
    startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
    startCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    startCol.setMinWidth(120);

    var endCol = new TableColumn<DemoSlot, java.time.LocalTime>("End");
    endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    endCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    endCol.setMinWidth(120);

    var noteCol = new TableColumn<DemoSlot, String>("Note");
    noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
    noteCol.setMinWidth(260);

    table.getColumns().setAll(dateCol, startCol, endCol, noteCol);

    var container = new BorderPane(table);
    container.getStyleClass().add("table-card");
    container.setPadding(new Insets(8));
    return container;
  }

  private void onRename(Label titleLabel) {
    var dialog = new TextInputDialog(course.displayName());
    dialog.setTitle("Rename course tab");
    dialog.setHeaderText("Rename course");
    dialog.setContentText("New name:");

    var res = dialog.showAndWait();
    if (res.isEmpty()) return;
    var val = res.get().trim();
    if (val.isBlank()) return;

    course.setName(val);
    onTabNameChanged.accept(course.displayName());
    titleLabel.setText(course.displayName());
  }

  private void onGenerateSlots() {
    var dialog = new GenerateSlotsDialog();
    var res = dialog.showAndWait();
    if (res.isEmpty()) return;

    var req = res.get();
    var task =
        new Task<java.util.List<DemoSlot>>() {
          @Override
          protected java.util.List<DemoSlot> call() throws Exception {
            var dtos =
                api.generateSlots(
                    course.getId(),
                    new GenerateSlotsRequest(
                        req.startDate(),
                        req.endDate(),
                        req.daysOfWeek(),
                        req.dayStart(),
                        req.dayEnd(),
                        req.slotMinutes(),
                        req.breakMinutes()));
            return dtos.stream()
                .map(s -> new DemoSlot(s.id(), s.date(), s.startTime(), s.endTime(), s.note()))
                .toList();
          }
        };
    task.setOnSucceeded(e -> setSlots(task.getValue()));
    task.setOnFailed(e -> FxAsync.showError("Generate slots failed", task.getException()));
    FxAsync.run(task);
  }

  private void onClearSlots() {
    var task =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            api.clearSlots(course.getId());
            return null;
          }
        };
    task.setOnSucceeded(e -> setSlots(java.util.List.of()));
    task.setOnFailed(e -> FxAsync.showError("Clear slots failed", task.getException()));
    FxAsync.run(task);
  }

  private void loadSlots() {
    var task =
        new Task<java.util.List<DemoSlot>>() {
          @Override
          protected java.util.List<DemoSlot> call() throws Exception {
            var dtos = api.listSlots(course.getId());
            return dtos.stream()
                .map(s -> new DemoSlot(s.id(), s.date(), s.startTime(), s.endTime(), s.note()))
                .toList();
          }
        };
    task.setOnSucceeded(e -> setSlots(task.getValue()));
    task.setOnFailed(e -> {
      // Backend may not be running; keep UI usable.
    });
    FxAsync.run(task);
  }

  private void setSlots(java.util.List<DemoSlot> newSlots) {
    course.getSlots().clear();
    if (newSlots != null) course.getSlots().addAll(newSlots);
    slots.setAll(course.getSlots());
    refreshSubtitle();
  }

  private void refreshSubtitle() {
    if (slots.isEmpty()) {
      subtitle.setText("No slots yet — generate slots to save time.");
      return;
    }
    subtitle.setText(slots.size() + " slots");
  }
}

