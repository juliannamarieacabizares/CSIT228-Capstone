package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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
    private TableView<Event> eventTable;

    @FXML
    private TableColumn<Event, String> colTitle;

    @FXML
    private TableColumn<Event, String> colDate;

    @FXML
    private TableColumn<Event, String> colLocation;

    @FXML
    private TableColumn<Event, Integer> colAvailableSeats;

    @FXML
    private Label lblStatus;

    // Session fields
    private String currentUserName;
    private int currentUserId;
    private String currentUserRole;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        loadEvents();
    }

    @FXML
    public void handleEventsNavigation(ActionEvent actionEvent) {
        System.out.println("Events button clicked");
    }

    private void loadEvents() {
        String query = "SELECT e.eventId, e.title, e.date, e.location, " +
                "e.totalSeats - COALESCE(SUM(CASE WHEN t.ticketId IS NOT NULL THEN 1 ELSE 0 END), 0) AS availableSeats " +
                "FROM events e " +
                "LEFT JOIN bookings b ON e.eventId = b.eventId " +
                "LEFT JOIN tickets t ON b.bookingId = t.bookingId " +
                "GROUP BY e.eventId, e.title, e.date, e.location, e.totalSeats";

        ObservableList<Event> events = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("eventId"),
                        rs.getString("title"),
                        rs.getString("date"),
                        rs.getString("location"),
                        rs.getInt("availableSeats")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Unable to load events.");
        }

        eventTable.setItems(events);
        lblStatus.setText(events.size() + " events loaded.");
    }

    // Called by LoginController after successful login
    public void setUserInfo(String userName, int userId, String role) {
        this.currentUserName = userName;
        this.currentUserId = userId;
        this.currentUserRole = role;

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