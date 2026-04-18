package com.demo.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class EmptyStatePane {
  private EmptyStatePane() {}

  public static Node forCourses(Runnable onCreateCourse) {
    var wrap = new VBox(10);
    wrap.getStyleClass().add("empty-state");
    wrap.setAlignment(Pos.CENTER);
    wrap.setPadding(new Insets(28));

    var title = new Label("Create a course tab to get started");
    title.getStyleClass().add("empty-title");

    var subtitle = new Label("Each course tab keeps demos and generated slots in one place.");
    subtitle.getStyleClass().add("empty-subtitle");
    subtitle.setWrapText(true);
    subtitle.setMaxWidth(520);

    var btn = new Button("New course tab");
    btn.getStyleClass().addAll("btn", "btn-primary");
    btn.setOnAction(e -> onCreateCourse.run());

    wrap.getChildren().addAll(title, subtitle, btn);
    return wrap;
  }
}

