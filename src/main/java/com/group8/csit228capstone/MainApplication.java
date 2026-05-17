package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Test database connection
        DatabaseConnection db = DatabaseConnection.getInstance();
        System.out.println("Database test passed!");

        // Load Login screen
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set window properties
        stage.setTitle("Event Ticketing System - Login");
        stage.setScene(scene);
        stage.setWidth(480);
        stage.setHeight(600);
        stage.setMinWidth(420);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}