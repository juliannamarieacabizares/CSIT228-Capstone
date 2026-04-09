package com.group8.csit228capstone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields are required!");
            return;
        }

        // TODO: Add your actual registration logic here (e.g., save to database)
        System.out.println("Registering user: " + username + " | " + email);

        showAlert(Alert.AlertType.INFORMATION, "Success",
                "Account created successfully for " + username + "!\n(This is a mockup)");

        // Optional: Go back to login after successful "registration"
        // handleBack(event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // TODO: Add logic to switch back to Login screen
        System.out.println("Going back to Login screen...");

        // For now, just show a message (replace with actual scene switch later)
        showAlert(Alert.AlertType.INFORMATION, "Back", "Returning to Login screen (not implemented yet).");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}