package com.demo.client.ui;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NewCourseDialog extends Dialog<NewCourseDialog.CourseDraft> {
  public record CourseDraft(String code, String name) {
    public String codeTrimmed() {
      return code == null ? "" : code.trim();
    }

    public String nameTrimmed() {
      return name == null ? "" : name.trim();
    }
  }

  public NewCourseDialog() {
    setTitle("New course tab");
    setHeaderText("Create a course tab");

    var create = new ButtonType("Create", ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(create, ButtonType.CANCEL);

    var code = new TextField();
    code.setPromptText("e.g., CS101");
    var name = new TextField();
    name.setPromptText("e.g., Introduction to Programming");

    var grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(18, 18, 8, 18));

    grid.add(new Label("Course code"), 0, 0);
    grid.add(code, 1, 0);
    grid.add(new Label("Course name"), 0, 1);
    grid.add(name, 1, 1);

    getDialogPane().setContent(grid);

    var createBtn = getDialogPane().lookupButton(create);
    createBtn.disableProperty().bind(name.textProperty().isEmpty().and(code.textProperty().isEmpty()));

    setResultConverter(btn -> {
      if (btn != create) return null;
      var c = code.getText() == null ? "" : code.getText().trim();
      var n = name.getText() == null ? "" : name.getText().trim();
      if (c.isBlank() && n.isBlank()) return null;
      return new CourseDraft(c, n);
    });
  }
}

