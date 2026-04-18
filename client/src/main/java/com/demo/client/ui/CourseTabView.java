package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.GenerateSlotsRequest;
import com.demo.client.model.Course;
import com.demo.client.model.DemoSlot;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class CourseTabView extends BorderPane {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  private final BackendApiClient api;
  private final Course course;
  private final String email;
  private final ObservableList<DemoSlot> allSlots = FXCollections.observableArrayList();
  private final FilteredList<DemoSlot> visibleSlots = new FilteredList<>(allSlots, s -> true);
  private final TableView<DemoSlot> table = buildSlotTable(visibleSlots);
  private final TabPane assignmentTabs = new TabPane();
  private final Label subtitle = new Label();
  private final java.util.function.Consumer<String> onTabNameChanged;

  public CourseTabView(BackendApiClient api, Course course, String email,
      java.util.function.Consumer<String> onTabNameChanged) {
    this.api = api;
    this.course = course;
    this.email = email;
    this.onTabNameChanged = onTabNameChanged;

    getStyleClass().add("course-tab");
    setPadding(new Insets(16));

    assignmentTabs.setMaxWidth(Double.MAX_VALUE);
    assignmentTabs.setMaxHeight(Double.MAX_VALUE);
    assignmentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    assignmentTabs
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, oldTab, newTab) -> applyTabFilter(newTab));

    setTop(buildHeader());

    var tableCard = new BorderPane(table);
    tableCard.getStyleClass().add("table-card");
    tableCard.setPadding(new Insets(8));

    var center = new BorderPane();
    center.setTop(assignmentTabs);
    BorderPane.setMargin(assignmentTabs, new Insets(0, 0, 8, 0));
    center.setCenter(tableCard);
    setCenter(center);

    allSlots.setAll(course.getSlots());
    rebuildAssignmentTabsPreserveSelection();
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

    var codeHint = new Label(course.getEnrollmentCode() != null
        ? "Enrollment code: " + course.getEnrollmentCode()
        : "No enrollment code set");
    codeHint.getStyleClass().add("course-subtitle");

    var titleWrap = new VBox2(2, title, subtitle, codeHint);
    HBox.setHgrow(titleWrap, Priority.ALWAYS);

    var rename = new Button("Rename");
    rename.getStyleClass().addAll("btn", "btn-ghost");
    rename.setTooltip(new Tooltip("Update course tab name"));
    rename.setOnAction(e -> onRename(title));

    var setVenueBtn = new Button("Set Venue");
    setVenueBtn.getStyleClass().addAll("btn", "btn-ghost");
    setVenueBtn.setTooltip(new Tooltip("Set the room/venue for demos in this course"));
    setVenueBtn.setOnAction(e -> onSetVenue());

    var uploadRubricBtn = new Button("Upload Rubric");
    uploadRubricBtn.getStyleClass().addAll("btn", "btn-ghost");
    uploadRubricBtn.setTooltip(new Tooltip("Upload a rubric file students can view"));
    uploadRubricBtn.setOnAction(e -> onUploadRubric());

    var marksBtn = new Button("Marks");
    marksBtn.getStyleClass().addAll("btn", "btn-ghost");
    marksBtn.setTooltip(new Tooltip("Enter and export student demo marks"));
    marksBtn.setOnAction(
        e ->
            new MarksDialog(
                    api,
                    course,
                    allSlots.stream().filter(s -> s.getStudentEmail() != null).toList())
                .show());

    var messages = new Button("Messages");
    messages.getStyleClass().addAll("btn", "btn-ghost");
    messages.setTooltip(new Tooltip("Open course messages"));
    messages.setOnAction(e -> new ChatDialog(api, course, email).show());

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

    wrap.getChildren()
        .addAll(
            titleWrap,
            spacer,
            setVenueBtn,
            uploadRubricBtn,
            marksBtn,
            rename,
            messages,
            generate,
            clear);
    return wrap;
  }

  private TableView<DemoSlot> buildSlotTable(ObservableList<DemoSlot> items) {
    var tbl = new TableView<>(items);
    tbl.getStyleClass().add("slots-table");
    tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    var dateCol = new TableColumn<DemoSlot, java.time.LocalDate>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    dateCol.setCellFactory(col -> new PrettyCell<>(d -> d == null ? "" : DATE_FMT.format(d)));
    dateCol.setMinWidth(200);

    var startCol = new TableColumn<DemoSlot, java.time.LocalTime>("Start");
    startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
    startCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    startCol.setMinWidth(100);

    var endCol = new TableColumn<DemoSlot, java.time.LocalTime>("End");
    endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    endCol.setCellFactory(col -> new PrettyCell<>(t -> t == null ? "" : TIME_FMT.format(t)));
    endCol.setMinWidth(100);

    var noteCol = new TableColumn<DemoSlot, String>("Note");
    noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
    noteCol.setMinWidth(200);

    var assignmentCol = new TableColumn<DemoSlot, String>("Assignment");
    assignmentCol.setCellValueFactory(new PropertyValueFactory<>("assignmentName"));
    assignmentCol.setMinWidth(160);

    var modeCol = new TableColumn<DemoSlot, Integer>("Mode");
    modeCol.setCellValueFactory(new PropertyValueFactory<>("groupMemberCount"));
    modeCol.setCellFactory(col -> new PrettyCell<>(n -> n != null && n > 1 ? "Group" : "Individual"));
    modeCol.setMinWidth(110);

    var studentCol = new TableColumn<DemoSlot, String>("Student");
    studentCol.setCellValueFactory(new PropertyValueFactory<>("studentEmail"));
    studentCol.setCellFactory(col -> new PrettyCell<>(s -> s == null ? "Available" : s));
    studentCol.setMinWidth(200);

    var rollCol = new TableColumn<DemoSlot, String>("Roll");
    rollCol.setCellValueFactory(new PropertyValueFactory<>("studentRollNumber"));
    rollCol.setCellFactory(col -> new PrettyCell<>(s -> s == null || s.isBlank() ? "-" : s));
    rollCol.setMinWidth(100);

    var sectionCol = new TableColumn<DemoSlot, String>("Section");
    sectionCol.setCellValueFactory(new PropertyValueFactory<>("studentSection"));
    sectionCol.setCellFactory(col -> new PrettyCell<>(s -> s == null || s.isBlank() ? "-" : s));
    sectionCol.setMinWidth(100);

    var groupCol = new TableColumn<DemoSlot, Integer>("Group Members");
    groupCol.setCellValueFactory(new PropertyValueFactory<>("groupMemberCount"));
    groupCol.setCellFactory(col -> new PrettyCell<>(n -> n == null ? "-" : String.valueOf(n)));
    groupCol.setMinWidth(120);

    var groupRollsCol = new TableColumn<DemoSlot, String>("Group Roll Numbers");
    groupRollsCol.setCellValueFactory(new PropertyValueFactory<>("groupMemberRollNumbers"));
    groupRollsCol.setCellFactory(col -> new PrettyCell<>(s -> s == null || s.isBlank() ? "-" : s));
    groupRollsCol.setMinWidth(220);

    var evalCol = new TableColumn<DemoSlot, Void>("Evaluate");
    evalCol.setMinWidth(110);
    evalCol.setCellFactory(
        col ->
            new javafx.scene.control.TableCell<>() {
              private final Button btn = new Button("Evaluate");
              {
                btn.getStyleClass().addAll("btn", "btn-ghost");
                btn.setOnAction(
                    e -> {
                      var row = getTableView().getItems().get(getIndex());
                      if (row.getStudentEmail() == null) return;
                      new MarksDialog(api, course, java.util.List.of(row)).show();
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setGraphic(null);
                  return;
                }
                var row = getTableView().getItems().get(getIndex());
                btn.setDisable(row.getStudentEmail() == null);
                setGraphic(btn);
              }
            });

    tbl.getColumns()
        .setAll(
            assignmentCol,
            modeCol,
            dateCol,
            startCol,
            endCol,
            noteCol,
            studentCol,
            rollCol,
            sectionCol,
            groupCol,
            groupRollsCol,
            evalCol);
    tbl.setRowFactory(
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
    return tbl;
  }

  private void applyTabFilter(Tab tab) {
    if (tab == null) {
      visibleSlots.setPredicate(s -> true);
      return;
    }
    if ("All assignments".equals(tab.getText())) {
      visibleSlots.setPredicate(s -> true);
      return;
    }
    String name = tab.getText();
    visibleSlots.setPredicate(
        s ->
            name.equals(
                s.getAssignmentName() == null || s.getAssignmentName().isBlank()
                    ? "General"
                    : s.getAssignmentName()));
  }

  private void rebuildAssignmentTabsPreserveSelection() {
    Tab selected = assignmentTabs.getSelectionModel().getSelectedItem();
    String selectedTitle = selected != null ? selected.getText() : null;

    assignmentTabs.getTabs().clear();
    var allTab = new Tab("All assignments");
    allTab.setClosable(false);
    assignmentTabs.getTabs().add(allTab);

    List<String> names =
        allSlots.stream()
            .map(s -> s.getAssignmentName() == null || s.getAssignmentName().isBlank() ? "General" : s.getAssignmentName())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();
    for (var n : names) {
      var t = new Tab(n);
      t.setClosable(false);
      assignmentTabs.getTabs().add(t);
    }

    if (selectedTitle != null) {
      for (var t : assignmentTabs.getTabs()) {
        if (selectedTitle.equals(t.getText())) {
          assignmentTabs.getSelectionModel().select(t);
          applyTabFilter(t);
          return;
        }
      }
    }
    assignmentTabs.getSelectionModel().selectFirst();
    applyTabFilter(assignmentTabs.getSelectionModel().getSelectedItem());
  }

  private void onSetVenue() {
    var dialog = new TextInputDialog(course.getVenue() != null ? course.getVenue() : "");
    dialog.setTitle("Set Venue");
    dialog.setHeaderText("Venue for " + course.displayName());
    dialog.setContentText("Room / location:");
    dialog.showAndWait().ifPresent(venue -> {
      var task = new Task<com.demo.client.api.dto.CourseDto>() {
        @Override protected com.demo.client.api.dto.CourseDto call() throws Exception {
          return api.updateVenue(course.getId(), venue.trim());
        }
      };
      task.setOnSucceeded(ev -> course.setVenue(task.getValue().venue()));
      task.setOnFailed(ev -> FxAsync.showError("Set venue failed", task.getException()));
      FxAsync.run(task);
    });
  }

  private void onUploadRubric() {
    var chooser = new FileChooser();
    chooser.setTitle("Select Rubric File");
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt", "*.xlsx"),
        new FileChooser.ExtensionFilter("All Files", "*.*"));
    var file = chooser.showOpenDialog(getScene().getWindow());
    if (file == null) return;

    var task = new Task<com.demo.client.api.dto.CourseDto>() {
      @Override protected com.demo.client.api.dto.CourseDto call() throws Exception {
        var bytes = Files.readAllBytes(file.toPath());
        return api.uploadRubric(course.getId(), file.getName(), bytes);
      }
    };
    task.setOnSucceeded(ev -> course.setRubricFilename(task.getValue().rubricFilename()));
    task.setOnFailed(ev -> FxAsync.showError("Upload rubric failed", task.getException()));
    FxAsync.run(task);
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
                        req.assignmentName(),
                        req.groupAssignment(),
                        req.groupMemberCount(),
                        req.startDate(),
                        req.endDate(),
                        req.daysOfWeek(),
                        req.dayStart(),
                        req.dayEnd(),
                        req.slotMinutes(),
                        req.breakMinutes()));
            return dtos.stream()
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
    task.setOnSucceeded(e -> setSlots(task.getValue()));
    task.setOnFailed(e -> {
      // Backend may not be running; keep UI usable.
    });
    FxAsync.run(task);
  }

  private void setSlots(java.util.List<DemoSlot> newSlots) {
    course.getSlots().clear();
    if (newSlots != null) course.getSlots().addAll(newSlots);
    allSlots.setAll(course.getSlots());
    rebuildAssignmentTabsPreserveSelection();
    refreshSubtitle();
  }

  private void refreshSubtitle() {
    if (allSlots.isEmpty()) {
      subtitle.setText("No slots yet — generate slots to save time.");
      return;
    }
    long booked = allSlots.stream().filter(s -> s.getStudentEmail() != null).count();
    subtitle.setText(allSlots.size() + " slots  •  " + booked + " booked");
  }
}
