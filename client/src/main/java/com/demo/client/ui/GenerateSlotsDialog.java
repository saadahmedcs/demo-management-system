package com.demo.client.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class GenerateSlotsDialog extends Dialog<GenerateSlotsDialog.GenerationRequest> {

  public record GenerationRequest(
      String assignmentName,
      Boolean groupAssignment,
      Integer groupMemberCount,
      LocalDate startDate,
      LocalDate endDate,
      Set<DayOfWeek> daysOfWeek,
      LocalTime dayStart,
      LocalTime dayEnd,
      int slotMinutes,
      int breakMinutes) {}

  public GenerateSlotsDialog() {
    setTitle("Generate slots");
    setHeaderText("Automated slot generation");

    var generate = new ButtonType("Generate", ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(generate, ButtonType.CANCEL);

    var startDate = new DatePicker(LocalDate.now().plusDays(1));
    var endDate = new DatePicker(LocalDate.now().plusDays(14));

    var dayStart = new TextField("09:00");
    var dayEnd = new TextField("17:00");

    var slotMinutes = new Spinner<>(new IntegerSpinnerValueFactory(5, 240, 20, 5));
    slotMinutes.setEditable(true);

    var breakMinutes = new Spinner<>(new IntegerSpinnerValueFactory(0, 120, 0, 5));
    breakMinutes.setEditable(true);
    var assignmentName = new TextField("Assignment 1");
    var mode = new ComboBox<String>();
    mode.getItems().addAll("Individual", "Group");
    mode.setValue("Individual");
    var groupMembers = new Spinner<>(new IntegerSpinnerValueFactory(2, 20, 2, 1));
    groupMembers.setEditable(true);
    groupMembers.setDisable(true);
    mode.valueProperty().addListener((obs, oldV, newV) -> groupMembers.setDisable(!"Group".equals(newV)));

    var days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    var daysRow = buildDaysRow(days);

    var grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(18, 18, 8, 18));

    int r = 0;
    grid.add(new Label("Assignment"), 0, r);
    grid.add(assignmentName, 1, r++);
    grid.add(new Label("Mode"), 0, r);
    grid.add(mode, 1, r++);
    grid.add(new Label("Group members"), 0, r);
    grid.add(groupMembers, 1, r++);

    grid.add(new Label("Start date"), 0, r);
    grid.add(startDate, 1, r++);
    grid.add(new Label("End date"), 0, r);
    grid.add(endDate, 1, r++);

    grid.add(new Label("Days"), 0, r);
    grid.add(daysRow, 1, r++);

    grid.add(new Label("Day start (HH:mm)"), 0, r);
    grid.add(dayStart, 1, r++);
    grid.add(new Label("Day end (HH:mm)"), 0, r);
    grid.add(dayEnd, 1, r++);

    grid.add(new Label("Slot duration (min)"), 0, r);
    grid.add(slotMinutes, 1, r++);
    grid.add(new Label("Break between slots (min)"), 0, r);
    grid.add(breakMinutes, 1, r++);

    getDialogPane().setContent(grid);

    setResultConverter(btn -> {
      if (btn != generate) return null;
      return new GenerationRequest(
          assignmentName.getText(),
          "Group".equals(mode.getValue()),
          "Group".equals(mode.getValue()) ? groupMembers.getValue() : 1,
          startDate.getValue(),
          endDate.getValue(),
          EnumSet.copyOf(days),
          parseTime(dayStart.getText()),
          parseTime(dayEnd.getText()),
          slotMinutes.getValue(),
          breakMinutes.getValue());
    });
  }

  private static HBox buildDaysRow(Set<DayOfWeek> selected) {
    var wrap = new HBox(10);
    wrap.getStyleClass().add("days-row");

    for (var d : DayOfWeek.values()) {
      var cb = new CheckBox(shortName(d));
      cb.setSelected(selected.contains(d));
      cb.selectedProperty().addListener((obs, oldV, newV) -> {
        if (newV) selected.add(d);
        else selected.remove(d);
      });
      wrap.getChildren().add(cb);
    }
    return wrap;
  }

  private static String shortName(DayOfWeek d) {
    return switch (d) {
      case MONDAY -> "Mon";
      case TUESDAY -> "Tue";
      case WEDNESDAY -> "Wed";
      case THURSDAY -> "Thu";
      case FRIDAY -> "Fri";
      case SATURDAY -> "Sat";
      case SUNDAY -> "Sun";
    };
  }

  private static LocalTime parseTime(String text) {
    if (text == null) return null;
    var t = text.trim();
    if (t.isBlank()) return null;
    return LocalTime.parse(t);
  }
}

