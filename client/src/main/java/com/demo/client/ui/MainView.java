package com.demo.client.ui;

import com.demo.client.api.BackendApiClient;
import com.demo.client.api.dto.CreateCourseRequest;
import com.demo.client.model.Course;
import java.net.URI;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MainView {
  private final BorderPane root = new BorderPane();
  private final TabPane tabPane = new TabPane();
  private final BackendApiClient api = new BackendApiClient(URI.create("http://localhost:8080"));
  private final String email;
  private final Runnable onLogout;

  public MainView(String email, Runnable onLogout) {
    this.email = email;
    this.onLogout = onLogout;
    root.getStyleClass().add("app-root");
    root.setTop(buildTopBar());
    root.setCenter(tabPane);

    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    tabPane.setMaxWidth(Double.MAX_VALUE);
    tabPane.setMaxHeight(Double.MAX_VALUE);

    tabPane.getTabs().add(buildEmptyStateTab());

    loadCourses();
  }

  public Parent getRoot() {
    return root;
  }

  private Parent buildTopBar() {
    var bar = new HBox(12);
    bar.getStyleClass().add("topbar");
    bar.setPadding(new Insets(14, 16, 14, 16));
    bar.setAlignment(Pos.CENTER_LEFT);

    var title = new Label("Demo Management System");
    title.getStyleClass().add("app-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    var addCourse = new Button("New course tab");
    addCourse.getStyleClass().addAll("btn", "btn-primary");
    addCourse.setTooltip(new Tooltip("Create a new course tab"));
    addCourse.setOnAction(evt -> onAddCourse());

    var emailLabel = new Label(email);
    emailLabel.getStyleClass().add("topbar-email");

    var logoutBtn = new Button("Log out");
    logoutBtn.getStyleClass().addAll("btn", "btn-ghost");
    logoutBtn.setOnAction(evt -> onLogout.run());

    bar.getChildren().addAll(title, spacer, emailLabel, addCourse, logoutBtn);
    return bar;
  }

  private Tab buildEmptyStateTab() {
    var tab = new Tab("Welcome");
    tab.setClosable(false);
    tab.setContent(EmptyStatePane.forCourses(() -> onAddCourse()));
    return tab;
  }

  private void onAddCourse() {
    var dialog = new NewCourseDialog();
    var result = dialog.showAndWait();
    if (result.isEmpty()) return;

    var draft = result.get();

    var task =
        new Task<Course>() {
          @Override
          protected Course call() throws Exception {
            var dto = api.createCourse(new CreateCourseRequest(draft.codeTrimmed(), draft.nameTrimmed(), email));
            return new Course(dto.id(), dto.code(), dto.name(), dto.taEmail());
          }
        };

    task.setOnSucceeded(e -> addCourseTab(task.getValue()));
    task.setOnFailed(e -> FxAsync.showError("Create course failed", task.getException()));
    FxAsync.run(task);
  }

  private void loadCourses() {
    var task =
        new Task<java.util.List<Course>>() {
          @Override
          protected java.util.List<Course> call() throws Exception {
            return api.listCourses().stream().map(c -> new Course(c.id(), c.code(), c.name(), c.taEmail())).toList();
          }
        };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      var list = task.getValue();
      if (list == null || list.isEmpty()) return;
      tabPane.getTabs().clear();
      for (var c : list) addCourseTab(c);
    }));
    task.setOnFailed(e -> {
      // Backend might not be running yet; keep UI usable.
    });
    FxAsync.run(task);
  }

  private void addCourseTab(Course course) {
    if (tabPane.getTabs().size() == 1 && "Welcome".equals(tabPane.getTabs().getFirst().getText())) {
      tabPane.getTabs().clear();
    }

    var courseTab = new Tab(course.displayName());
    courseTab.setClosable(true);
    courseTab.setContent(new CourseTabView(api, course, email, updatedName -> courseTab.setText(updatedName)));
    tabPane.getTabs().add(courseTab);
    tabPane.getSelectionModel().select(courseTab);
  }
}

