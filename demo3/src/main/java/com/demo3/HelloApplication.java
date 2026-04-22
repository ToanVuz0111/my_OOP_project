package com.demo3;

import com.demo3.controller.MainMenuController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application entry point — opens the Main Menu.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Tree Visualization — OOP Project");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        MainMenuController menuController = new MainMenuController(stage);
        Scene scene = menuController.createScene();
        stage.setScene(scene);
        stage.show();
    }
}
