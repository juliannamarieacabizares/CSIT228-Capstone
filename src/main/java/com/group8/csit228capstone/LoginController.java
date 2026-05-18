package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField textUsername;

    @FXML
    private PasswordField textPassword;

    @FXML
    private Label lblError;

    @FXML
    private Button btnSignIn;

    @FXML
    private Button btnRegister;

    @FXML
    private CheckBox showPasswordCheckbox;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    public void initialize() {
        // Create a visible TextField and hide it initially
        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter your password");
        visiblePasswordField.setStyle(textPassword.getStyle());
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        // Get the parent container and add the visible field
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) textPassword.getParent();
        int passwordIndex = parent.getChildren().indexOf(textPassword);
        parent.getChildren().add(passwordIndex + 1, visiblePasswordField);

        // Bind the text between both fields
        visiblePasswordField.textProperty().bindBidirectional(textPassword.textProperty());

        // Toggle password visibility
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Show password
                visiblePasswordField.setText(textPassword.getText());
                textPassword.setVisible(false);
                textPassword.setManaged(false);
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                visiblePasswordField.requestFocus();
            } else {
                // Hide password
                textPassword.setText(visiblePasswordField.getText());
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
                textPassword.setVisible(true);
                textPassword.setManaged(true);
                textPassword.requestFocus();
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = textUsername.getText();
        String password;

        // Get password from the visible field if it's showing
        if (showPasswordCheckbox.isSelected()) {
            password = visiblePasswordField.getText();
        } else {
            password = textPassword.getText();
        }

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter email and password");
            return;
        }

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("userId");
                String userName = rs.getString("name");
                String role = rs.getString("role");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
                Parent root = loader.load();

                MainController mainController = loader.getController();
                mainController.setUserInfo(userName, userId, role);

                if ("admin".equals(role)) {
                    mainController.enableAdminMode();
                }

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                boolean wasMaximized = stage.isMaximized();

                Scene scene = new Scene(root);
                stage.setScene(scene);

                if (wasMaximized) {
                    stage.setMaximized(true);
                }

                stage.setTitle("Event Dashboard");
            } else {
                lblError.setText("Invalid email or password");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterNavigation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            if (wasMaximized) {
                stage.setMaximized(true);
            }

            stage.setTitle("Create Account");

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error loading registration page");
        }
    }
}