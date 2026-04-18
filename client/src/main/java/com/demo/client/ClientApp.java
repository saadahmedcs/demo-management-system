package com.demo.client;

import com.demo.client.ui.LoginView;
import com.demo.client.ui.MainView;
import com.demo.client.ui.StudentView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

  private Scene scene;

  @Override
  public void start(Stage stage) {
    scene = new Scene(loginRoot(), 1200, 760);
    scene.getStylesheets().add(MainView.class.getResource("/styles/app.css").toExternalForm());

    stage.setTitle("Demo Management System");
    stage.setMinWidth(900);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }

  private javafx.scene.Parent loginRoot() {
    return new LoginView((role, email) -> {
      if ("TA".equals(role)) {
        scene.setRoot(new MainView(email, () -> scene.setRoot(loginRoot())).getRoot());
      } else {
        scene.setRoot(new StudentView(email, () -> scene.setRoot(loginRoot())).getRoot());
      }
    }).getRoot();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

