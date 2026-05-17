package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    @FXML
    public void handleRegister(ActionEvent event) {
        String name = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Invalid email format!");
            return;
        }

        if (password.length() < 8) {
            showAlert("Error", "Password must be at least 8 characters long!");
            return;
        }

        if (isEmailDuplicate(email)) {
            showAlert("Error", "Email is already registered!");
        } else {
            saveUser(name, email, password);
        }
    }

    private boolean isEmailDuplicate(String email) {
        String query = "SELECT count(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveUser(String name, String email, String password) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'customer')";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn == null || conn.isClosed()) {
                System.out.println("Connection was closed, attempting to reconnect...");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                pstmt.executeUpdate();

                showAlert("Success", "Account created successfully!");
                handleBack(new ActionEvent(txtUsername, null));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            showAlert("Database Error", "Reason: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Event Ticketing System - Login");
            stage.setWidth(480);
            stage.setHeight(600);
            stage.setMinWidth(420);
            stage.setMinHeight(500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}