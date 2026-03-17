package com.demo.client;

import com.demo.client.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
  @Override
  public void start(Stage stage) {
    var view = new MainView();
    var scene = new Scene(view.getRoot(), 1200, 760);
    scene.getStylesheets().add(MainView.class.getResource("/styles/app.css").toExternalForm());

    stage.setTitle("Demo Management System");
    stage.setMinWidth(900);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

