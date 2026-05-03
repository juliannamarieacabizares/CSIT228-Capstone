package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField textUsername;

    @FXML
    private PasswordField textPassword;

    @FXML
    private Button btnSignIn;

    @FXML
    private Label lblError;

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String email = textUsername.getText().trim();
        String password = textPassword.getText().trim();

        // Check if fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter email and password");
            return;
        }

        // Validate login and get user
        User loggedInUser = validateLogin(email, password);

        if (loggedInUser != null) {
            try {
                // Load the main dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
                Parent root = loader.load();

                // Get MainController and pass user info
                MainController mainController = loader.getController();
                mainController.setUserInfo(loggedInUser.getName(), loggedInUser.getUserId(), loggedInUser.getRole());

                // Enable admin mode if user is admin
                if ("admin".equals(loggedInUser.getRole())) {
                    mainController.enableAdminMode();
                }

                // Close login window and open dashboard
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Event Ticketing System - Home");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                lblError.setText("Error loading dashboard");
            }
        } else {
            lblError.setText("Invalid email or password");
            textPassword.clear();
        }
    }

    private User validateLogin(String email, String password) {
        String query = "SELECT userId, name, email, role FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}