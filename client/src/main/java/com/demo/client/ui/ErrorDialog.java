package com.demo.client.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public final class ErrorDialog {
  private ErrorDialog() {}

  public static void show(String title, Throwable ex) {
    var alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText("Something went wrong");
    alert.setContentText(ex == null ? "Unknown error." : safeMessage(ex));

    if (ex != null) {
      var sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));

      var area = new TextArea(sw.toString());
      area.setEditable(false);
      area.setWrapText(false);
      area.setMaxWidth(Double.MAX_VALUE);
      area.setMaxHeight(Double.MAX_VALUE);
      GridPane.setVgrow(area, Priority.ALWAYS);
      GridPane.setHgrow(area, Priority.ALWAYS);

      var content = new GridPane();
      content.setMaxWidth(Double.MAX_VALUE);
      content.add(new Label("Details:"), 0, 0);
      content.add(area, 0, 1);
      alert.getDialogPane().setExpandableContent(content);
    }

    alert.showAndWait();
  }

  private static String safeMessage(Throwable ex) {
    var msg = ex.getMessage();
    if (msg != null && !msg.isBlank()) return msg;
    return ex.getClass().getSimpleName();
  }
}

