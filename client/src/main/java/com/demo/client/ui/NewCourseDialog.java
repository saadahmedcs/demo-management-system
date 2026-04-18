package com.demo.client.ui;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class NewCourseDialog extends Dialog<NewCourseDialog.CourseDraft> {
  public record CourseDraft(String code, String name, String enrollmentCode) {
    public String codeTrimmed() {
      return code == null ? "" : code.trim();
    }

    public String nameTrimmed() {
      return name == null ? "" : name.trim();
    }

    public String enrollmentCodeTrimmed() {
      return enrollmentCode == null ? "" : enrollmentCode.trim();
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

    var enrollmentCode = new TextField();
    enrollmentCode.setPromptText("e.g., CS101-SP26");
    enrollmentCode.setTooltip(new Tooltip("Share this code with students so they can join the course"));

    var grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(18, 18, 8, 18));

    grid.add(new Label("Course code"), 0, 0);
    grid.add(code, 1, 0);
    grid.add(new Label("Course name"), 0, 1);
    grid.add(name, 1, 1);
    grid.add(new Label("Enrollment code"), 0, 2);
    grid.add(enrollmentCode, 1, 2);

    getDialogPane().setContent(grid);

    var createBtn = getDialogPane().lookupButton(create);
    createBtn.disableProperty().bind(
        name.textProperty().isEmpty()
            .and(code.textProperty().isEmpty())
            .or(enrollmentCode.textProperty().isEmpty()));

    setResultConverter(btn -> {
      if (btn != create) return null;
      var c = code.getText() == null ? "" : code.getText().trim();
      var n = name.getText() == null ? "" : name.getText().trim();
      var e = enrollmentCode.getText() == null ? "" : enrollmentCode.getText().trim();
      if ((c.isBlank() && n.isBlank()) || e.isBlank()) return null;
      return new CourseDraft(c, n, e);
    });
  }
}
