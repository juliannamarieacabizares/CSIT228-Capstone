package com.group8.csit228capstone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class MainController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnEvents;

    @FXML
    private Button btnBook;

    @FXML
    private Button btnView;

    @FXML
    private Button btnManageEvents;

    @FXML
    private TableView<?> eventTable;

    @FXML
    private Label lblStatus;  // Make sure this exists in FXML

    // Session fields
    private String currentUserName;
    private int currentUserId;
    private String currentUserRole;

    @FXML
    public void handleEventsNavigation(ActionEvent actionEvent) {
        System.out.println("Events button clicked");
    }

    // Called by LoginController after successful login
    public void setUserInfo(String userName, int userId, String role) {
        this.currentUserName = userName;
        this.currentUserId = userId;
        this.currentUserRole = role;

        // Display welcome message
        if (lblStatus != null) {
            lblStatus.setText("Welcome, " + userName + "!");
        }

        System.out.println("User logged in: " + userName + " (Role: " + role + ")");
    }

    // Called for admin users to show admin features
    public void enableAdminMode() {
        System.out.println("Admin mode enabled");
        if (btnManageEvents != null) {
            btnManageEvents.setVisible(true);
            btnManageEvents.setManaged(true);
        }
    }
}