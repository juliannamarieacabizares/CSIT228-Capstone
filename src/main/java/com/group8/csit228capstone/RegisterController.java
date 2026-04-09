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

    }

    @FXML
    private void handleBack(ActionEvent event) {

    }

    private void showAlert(Alert.AlertType type, String title, String content) {

    }
}