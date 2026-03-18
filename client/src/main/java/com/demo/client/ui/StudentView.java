package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.TimetableEntryDto;
import com.demo.client.model.Course;
import com.demo.client.model.DemoSlot;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class StudentView {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  /** Exact start/end times matching the 6 fixed class periods. */
  private static final LocalTime[][] PERIODS = {
    { LocalTime.of(8, 30),  LocalTime.of(9, 50)  },
    { LocalTime.of(10, 0),  LocalTime.of(11, 20) },
    { LocalTime.of(11, 30), LocalTime.of(12, 50) },
    { LocalTime.of(13, 0),  LocalTime.of(14, 20) },
    { LocalTime.of(14, 30), LocalTime.of(15, 55) },
    { LocalTime.of(16, 0),  LocalTime.of(17, 15) }
  };

  private final BackendApiClient api = new BackendApiClient(URI.create("http://localhost:8080"));
  private final String email;
  private final Runnable onLogout;

  private final BorderPane root = new BorderPane();
  private final ObservableList<Course> courses = FXCollections.observableArrayList();
  private final ObservableList<DemoSlot> slots = FXCollections.observableArrayList();
  private Course selectedCourse;

  private final Label courseHeaderLabel = new Label("Select a course");
  private final Label slotCountLabel = new Label();
  private final TableView<DemoSlot> slotTable = new TableView<>();

  /** Current timetable entries — updated after each timetable save. */
  private List<TimetableEntryDto> timetableEntries = new ArrayList<>();

  public StudentView(String email, Runnable onLogout) {
    this.email = email;
    this.onLogout = onLogout;

    root.getStyleClass().add("app-root");
    root.setTop(buildTopBar());
    root.setCenter(buildContent());

    loadTimetable();
    loadCourses();
  }

  public Parent getRoot() {
    return root;
  }

  // ── Top bar ──────────────────────────────────────────────────────────────

  private Parent buildTopBar() {
    var bar = new HBox(12);
    bar.getStyleClass().add("topbar");
    bar.setPadding(new Insets(14, 16, 14, 16));
    bar.setAlignment(Pos.CENTER_LEFT);

    var title = new Label("Demo Management System");
    title.getStyleClass().add("app-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    var emailLabel = new Label(email);
    emailLabel.getStyleClass().add("topbar-email");

    var timetableBtn = new Button("My Timetable");
    timetableBtn.getStyleClass().addAll("btn", "btn-ghost");
    timetableBtn.setOnAction(e ->
        new TimetableView(api, email, saved -> {
          timetableEntries = saved;
          slotTable.refresh(); // re-evaluate clash status for current slots
        }).show()
    );

    var logoutBtn = new Button("Log out");
    logoutBtn.getStyleClass().addAll("btn", "btn-ghost");
    logoutBtn.setOnAction(e -> onLogout.run());

    bar.getChildren().addAll(title, spacer, emailLabel, timetableBtn, logoutBtn);
    return bar;
  }

  // ── Main content ─────────────────────────────────────────────────────────

  private Parent buildContent() {
    var split = new HBox(16);
    split.setPadding(new Insets(16));
    HBox.setHgrow(split, Priority.ALWAYS);
    split.getChildren().addAll(buildCoursePanel(), buildSlotPanel());
    return split;
  }

  // ── Left: course list ─────────────────────────────────────────────────────

  private Parent buildCoursePanel() {
    var header = new Label("Courses");
    header.getStyleClass().add("course-title");

    var list = new ListView<>(courses);
    list.getStyleClass().add("course-list");
    list.setPrefWidth(260);
    list.setMaxHeight(Double.MAX_VALUE);
    VBox.setVgrow(list, Priority.ALWAYS);

    list.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Course item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.displayName());
      }
    });

    list.getSelectionModel().selectedItemProperty().addListener(
        (obs, old, course) -> { if (course != null) onCourseSelected(course); });

    var panel = new VBox(10, header, list);
    panel.getStyleClass().add("table-card");
    panel.setPadding(new Insets(14));
    panel.setMaxHeight(Double.MAX_VALUE);
    VBox.setVgrow(list, Priority.ALWAYS);
    return panel;
  }

  // ── Right: slot table ─────────────────────────────────────────────────────

  private Parent buildSlotPanel() {
    var headerRow = new HBox(12);
    headerRow.setAlignment(Pos.CENTER_LEFT);

    courseHeaderLabel.getStyleClass().add("course-title");
    slotCountLabel.getStyleClass().add("course-subtitle");

    var titleWrap = new VBox2(2, courseHeaderLabel, slotCountLabel);
    HBox.setHgrow(titleWrap, Priority.ALWAYS);

    var messagesBtn = new Button("Messages");
    messagesBtn.getStyleClass().addAll("btn", "btn-ghost");
    messagesBtn.setOnAction(e -> {
      if (selectedCourse != null) new ChatDialog(api, selectedCourse, email).show();
    });

    headerRow.getChildren().addAll(titleWrap, messagesBtn);

    buildSlotTable();

    var tableCard = new BorderPane(slotTable);
    tableCard.getStyleClass().add("table-card");
    tableCard.setPadding(new Insets(8));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    var panel = new VBox(12, headerRow, tableCard);
    panel.setMaxHeight(Double.MAX_VALUE);
    HBox.setHgrow(panel, Priority.ALWAYS);
    return panel;
  }

  private void buildSlotTable() {
    slotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    slotTable.setItems(slots);
    slotTable.setMaxHeight(Double.MAX_VALUE);

    var dateCol = new TableColumn<DemoSlot, java.time.LocalDate>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    dateCol.setCellFactory(col -> new PrettyCell<>(d -> d == null ? "" : DATE_FMT.format(d)));
    dateCol.setMinWidth(180);

    var startCol = new TableColumn<DemoSlot, java.time.LocalTime>("Start");
    startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
    startCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    startCol.setMinWidth(80);

    var endCol = new TableColumn<DemoSlot, java.time.LocalTime>("End");
    endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    endCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    endCol.setMinWidth(80);

    var actionCol = new TableColumn<DemoSlot, Void>("Action");
    actionCol.setMinWidth(120);
    actionCol.setCellFactory(col -> new TableCell<>() {
      private final Button btn = new Button();

      {
        btn.getStyleClass().add("btn");
        btn.setOnAction(e -> {
          var slot = getTableView().getItems().get(getIndex());
          if (slot.getStudentEmail() == null && !hasClash(slot)) {
            onBookSlot(slot);
          } else if (email.equals(slot.getStudentEmail())) {
            onUnbookSlot(slot);
          }
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) { setGraphic(null); return; }

        var slot = getTableView().getItems().get(getIndex());
        btn.getStyleClass().removeAll("btn-primary", "btn-danger", "btn-ghost", "btn-warning");

        if (email.equals(slot.getStudentEmail())) {
          btn.setText("Unbook");
          btn.getStyleClass().add("btn-danger");
          btn.setDisable(false);
        } else if (slot.getStudentEmail() != null) {
          btn.setText("Taken");
          btn.getStyleClass().add("btn-ghost");
          btn.setDisable(true);
        } else if (hasClash(slot)) {
          btn.setText("Clash");
          btn.getStyleClass().add("btn-ghost");
          btn.setDisable(true);
        } else {
          btn.setText("Book");
          btn.getStyleClass().add("btn-primary");
          btn.setDisable(false);
        }
        setGraphic(btn);
      }
    });

    slotTable.getColumns().setAll(List.of(dateCol, startCol, endCol, actionCol));
  }

  // ── Clash detection ───────────────────────────────────────────────────────

  /**
   * Returns true if the slot's day and time overlaps with any class period
   * the student has in their timetable.
   */
  private boolean hasClash(DemoSlot slot) {
    if (slot.getDate() == null || slot.getStartTime() == null) return false;
    DayOfWeek dow = slot.getDate().getDayOfWeek();
    return timetableEntries.stream().anyMatch(entry -> {
      if (!dow.name().equals(entry.dayOfWeek())) return false;
      int p = entry.periodIndex();
      if (p < 0 || p >= PERIODS.length) return false;
      LocalTime pStart = PERIODS[p][0];
      LocalTime pEnd   = PERIODS[p][1];
      // Overlap: slot starts before period ends AND slot ends after period starts
      return slot.getStartTime().isBefore(pEnd) && slot.getEndTime().isAfter(pStart);
    });
  }

  // ── Actions ───────────────────────────────────────────────────────────────

  private void onCourseSelected(Course course) {
    selectedCourse = course;
    courseHeaderLabel.setText(course.displayName());
    slotCountLabel.setText("Loading…");
    slots.clear();

    var task = new Task<List<DemoSlot>>() {
      @Override
      protected List<DemoSlot> call() throws Exception {
        return api.listSlots(course.getId()).stream()
            .map(s -> new DemoSlot(s.id(), s.date(), s.startTime(), s.endTime(), s.note(), s.studentEmail()))
            .toList();
      }
    };
    task.setOnSucceeded(e -> { slots.setAll(task.getValue()); refreshSlotCount(); });
    task.setOnFailed(e -> FxAsync.showError("Load slots failed", task.getException()));
    FxAsync.run(task);
  }

  private void onBookSlot(DemoSlot slot) {
    var task = new Task<DemoSlot>() {
      @Override
      protected DemoSlot call() throws Exception {
        var dto = api.bookSlot(selectedCourse.getId(), slot.getId(), email);
        return new DemoSlot(dto.id(), dto.date(), dto.startTime(), dto.endTime(), dto.note(), dto.studentEmail());
      }
    };
    task.setOnSucceeded(e -> { replaceSlot(slot, task.getValue()); refreshSlotCount(); });
    task.setOnFailed(e -> FxAsync.showError("Book slot failed", task.getException()));
    FxAsync.run(task);
  }

  private void onUnbookSlot(DemoSlot slot) {
    var task = new Task<DemoSlot>() {
      @Override
      protected DemoSlot call() throws Exception {
        var dto = api.unbookSlot(selectedCourse.getId(), slot.getId(), email);
        return new DemoSlot(dto.id(), dto.date(), dto.startTime(), dto.endTime(), dto.note(), dto.studentEmail());
      }
    };
    task.setOnSucceeded(e -> { replaceSlot(slot, task.getValue()); refreshSlotCount(); });
    task.setOnFailed(e -> FxAsync.showError("Unbook slot failed", task.getException()));
    FxAsync.run(task);
  }

  private void replaceSlot(DemoSlot old, DemoSlot updated) {
    int idx = slots.indexOf(old);
    if (idx >= 0) slots.set(idx, updated);
  }

  private void refreshSlotCount() {
    if (slots.isEmpty()) { slotCountLabel.setText("No slots available"); return; }
    long available = slots.stream()
        .filter(s -> s.getStudentEmail() == null && !hasClash(s))
        .count();
    slotCountLabel.setText(slots.size() + " slots  •  " + available + " available");
  }

  private void loadTimetable() {
    var task = new Task<List<TimetableEntryDto>>() {
      @Override
      protected List<TimetableEntryDto> call() throws Exception {
        return api.listTimetable(email);
      }
    };
    task.setOnSucceeded(e -> {
      timetableEntries = task.getValue();
      slotTable.refresh();
    });
    FxAsync.run(task);
  }

  private void loadCourses() {
    var task = new Task<List<Course>>() {
      @Override
      protected List<Course> call() throws Exception {
        return api.listCourses().stream()
            .map(c -> new Course(c.id(), c.code(), c.name(), c.taEmail()))
            .toList();
      }
    };
    task.setOnSucceeded(e -> courses.setAll(task.getValue()));
    FxAsync.run(task);
  }
}
