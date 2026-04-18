package com.demo.client.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;

public final class FxAsync {
  private FxAsync() {}

  public static <T> void run(Task<T> task) {
    var t = new Thread(task);
    t.setDaemon(true);
    t.start();
  }

  public static void showError(String title, Throwable ex) {
    Platform.runLater(() -> ErrorDialog.show(title, ex));
  }
}

