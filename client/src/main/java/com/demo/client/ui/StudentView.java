package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.TimetableEntryDto;
import com.demo.client.model.Course;
import com.demo.client.model.DemoSlot;
import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

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
  private final Label venueLabel = new Label();
  private final Button rubricBtn = new Button("View Rubric");
  private final TableView<DemoSlot> slotTable = new TableView<>();

  /** Current timetable entries — updated after each timetable save. */
  private List<TimetableEntryDto> timetableEntries = new ArrayList<>();

  /** Polls the selected course's venue every 10 s. */
  private Timeline venuePoll;

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

    var joinBtn = new Button("Join Course");
    joinBtn.getStyleClass().addAll("btn", "btn-primary");
    joinBtn.setOnAction(e -> onJoinCourse());

    var timetableBtn = new Button("My Timetable");
    timetableBtn.getStyleClass().addAll("btn", "btn-ghost");
    timetableBtn.setOnAction(e ->
        new TimetableView(api, email, saved -> {
          timetableEntries = saved;
          slotTable.refresh();
        }).show()
    );

    var logoutBtn = new Button("Log out");
    logoutBtn.getStyleClass().addAll("btn", "btn-ghost");
    logoutBtn.setOnAction(e -> onLogout.run());

    bar.getChildren().addAll(title, spacer, emailLabel, joinBtn, timetableBtn, logoutBtn);
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

    venueLabel.getStyleClass().add("course-subtitle");
    venueLabel.setVisible(false);
    venueLabel.setManaged(false);

    rubricBtn.getStyleClass().addAll("btn", "btn-ghost");
    rubricBtn.setVisible(false);
    rubricBtn.setManaged(false);
    rubricBtn.setOnAction(e -> onViewRubric());

    var messagesBtn = new Button("Messages");
    messagesBtn.getStyleClass().addAll("btn", "btn-ghost");
    messagesBtn.setOnAction(e -> {
      if (selectedCourse != null) new ChatDialog(api, selectedCourse, email).show();
    });

    headerRow.getChildren().addAll(titleWrap, venueLabel, rubricBtn, messagesBtn);

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

        boolean alreadyBooked = slots.stream().anyMatch(s -> email.equals(s.getStudentEmail()));

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
        } else if (alreadyBooked) {
          btn.setText("Book");
          btn.getStyleClass().add("btn-primary");
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
    slotTable.setRowFactory(
        tv ->
            new javafx.scene.control.TableRow<>() {
              @Override
              protected void updateItem(DemoSlot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getIndex() <= 0) {
                  setStyle("");
                  return;
                }
                var current = item.getDate();
                var previous = getTableView().getItems().get(getIndex() - 1).getDate();
                if (current != null && previous != null && !current.equals(previous)) {
                  setStyle(
                      "-fx-border-color: rgba(148,163,184,0.65) transparent transparent transparent; "
                          + "-fx-border-width: 2 0 0 0;");
                } else {
                  setStyle("");
                }
              }
            });
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

    // Venue label
    updateVenueLabel(course.getVenue());

    // Rubric button — show only if a rubric has been uploaded
    rubricBtn.setVisible(course.getRubricFilename() != null);
    rubricBtn.setManaged(course.getRubricFilename() != null);

    // Start venue polling (cancel any previous poll first)
    if (venuePoll != null) venuePoll.stop();
    venuePoll = new Timeline(new KeyFrame(Duration.seconds(10), ev -> refreshVenue()));
    venuePoll.setCycleCount(Timeline.INDEFINITE);
    venuePoll.play();

    var task = new Task<List<DemoSlot>>() {
      @Override
      protected List<DemoSlot> call() throws Exception {
        return api.listSlots(course.getId()).stream()
            .map(
                s ->
                    new DemoSlot(
                        s.id(),
                        s.date(),
                        s.startTime(),
                        s.endTime(),
                        s.note(),
                        s.assignmentName(),
                        s.studentEmail(),
                        s.studentRollNumber(),
                        s.studentSection(),
                        s.groupMemberCount(),
                        s.groupMemberRollNumbers()))
            .toList();
      }
    };
    task.setOnSucceeded(e -> { slots.setAll(task.getValue()); refreshSlotCount(); });
    task.setOnFailed(e -> FxAsync.showError("Load slots failed", task.getException()));
    FxAsync.run(task);
  }

  private void onBookSlot(DemoSlot slot) {
    var rollNumbers = collectGroupRollNumbers(slot);
    if (rollNumbers == null) return;
    var task = new Task<DemoSlot>() {
      @Override
      protected DemoSlot call() throws Exception {
        var dto =
            api.bookSlot(
                selectedCourse.getId(),
                slot.getId(),
                email,
                slot.getGroupMemberCount(),
                rollNumbers);
        return new DemoSlot(
            dto.id(),
            dto.date(),
            dto.startTime(),
            dto.endTime(),
            dto.note(),
            dto.assignmentName(),
            dto.studentEmail(),
            dto.studentRollNumber(),
            dto.studentSection(),
            dto.groupMemberCount(),
            dto.groupMemberRollNumbers());
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
        return new DemoSlot(
            dto.id(),
            dto.date(),
            dto.startTime(),
            dto.endTime(),
            dto.note(),
            dto.assignmentName(),
            dto.studentEmail(),
            dto.studentRollNumber(),
            dto.studentSection(),
            dto.groupMemberCount(),
            dto.groupMemberRollNumbers());
      }
    };
    task.setOnSucceeded(e -> { replaceSlot(slot, task.getValue()); refreshSlotCount(); });
    task.setOnFailed(e -> FxAsync.showError("Unbook slot failed", task.getException()));
    FxAsync.run(task);
  }

  private List<String> collectGroupRollNumbers(DemoSlot slot) {
    int expected = slot.getGroupMemberCount() == null || slot.getGroupMemberCount() < 1 ? 1 : slot.getGroupMemberCount();
    if (expected <= 1) return List.of();
    var dialog = new javafx.scene.control.TextInputDialog();
    dialog.setTitle("Group Roll Numbers");
    dialog.setHeaderText("This slot is group-based (" + expected + " members).");
    dialog.setContentText("Enter all " + expected + " roll numbers (comma-separated):");
    var res = dialog.showAndWait();
    if (res.isEmpty()) return null;
    var values =
        Arrays.stream(res.get().split(","))
            .map(String::trim)
            .filter(v -> !v.isBlank())
            .distinct()
            .toList();
    if (values.size() != expected) {
      FxAsync.showError(
          "Group members required",
          new IllegalArgumentException("Please enter exactly " + expected + " roll numbers."));
      return null;
    }
    return values;
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

  private void onJoinCourse() {
    var dialog = new javafx.scene.control.TextInputDialog();
    dialog.setTitle("Join Course");
    dialog.setHeaderText("Enter the enrollment code shared by your TA.");
    dialog.setContentText("Enrollment code:");
    dialog.showAndWait().ifPresent(code -> {
      var trimmed = code.trim();
      if (trimmed.isBlank()) return;
      var task = new Task<Course>() {
        @Override protected Course call() throws Exception {
          var dto = api.joinCourse(email, trimmed);
          return new Course(dto.id(), dto.code(), dto.name(), dto.taEmail(), dto.venue(), dto.rubricFilename(), dto.enrollmentCode());
        }
      };
      task.setOnSucceeded(e -> {
        var joined = task.getValue();
        // Avoid duplicate if already in list
        boolean already = courses.stream().anyMatch(c -> c.getId().equals(joined.getId()));
        if (!already) courses.add(joined);
      });
      task.setOnFailed(e -> FxAsync.showError("Could not join course", task.getException()));
      FxAsync.run(task);
    });
  }

  private void updateVenueLabel(String venue) {
    if (venue != null && !venue.isBlank()) {
      venueLabel.setText("Venue: " + venue);
      venueLabel.setVisible(true);
      venueLabel.setManaged(true);
    } else {
      venueLabel.setVisible(false);
      venueLabel.setManaged(false);
    }
  }

  private void refreshVenue() {
    if (selectedCourse == null) return;
    var task = new Task<com.demo.client.api.dto.CourseDto>() {
      @Override protected com.demo.client.api.dto.CourseDto call() throws Exception {
        return api.getCourse(selectedCourse.getId());
      }
    };
    task.setOnSucceeded(e -> {
      var dto = task.getValue();
      selectedCourse.setVenue(dto.venue());
      updateVenueLabel(dto.venue());
      // Show rubric button if rubric became available
      var hasRubric = dto.rubricFilename() != null;
      rubricBtn.setVisible(hasRubric);
      rubricBtn.setManaged(hasRubric);
      if (hasRubric) selectedCourse.setRubricFilename(dto.rubricFilename());
    });
    FxAsync.run(task);
  }

  private void onViewRubric() {
    if (selectedCourse == null || selectedCourse.getRubricFilename() == null) return;
    var task = new Task<byte[]>() {
      @Override protected byte[] call() throws Exception {
        return api.downloadRubric(selectedCourse.getId());
      }
    };
    task.setOnSucceeded(e -> {
      try {
        var bytes = task.getValue();
        var tmp = Files.createTempFile("rubric_", "_" + selectedCourse.getRubricFilename());
        Files.write(tmp, bytes);
        Desktop.getDesktop().open(tmp.toFile());
      } catch (Exception ex) {
        FxAsync.showError("Could not open rubric", ex);
      }
    });
    task.setOnFailed(ev -> FxAsync.showError("Download rubric failed", task.getException()));
    FxAsync.run(task);
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
        return api.listEnrolledCourses(email).stream()
            .map(c -> new Course(c.id(), c.code(), c.name(), c.taEmail(), c.venue(), c.rubricFilename(), c.enrollmentCode()))
            .toList();
      }
    };
    task.setOnSucceeded(e -> courses.setAll(task.getValue()));
    FxAsync.run(task);
  }
}
