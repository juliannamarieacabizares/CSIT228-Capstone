package com.group8.csit228capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegisterApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Register view
        Parent root = FXMLLoader.load(getClass().getResource("register-view.fxml"));

        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Register - CSIT228 Capstone");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


}