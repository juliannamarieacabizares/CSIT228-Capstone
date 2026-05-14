package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainController {

    @FXML
    private TableView<Event> eventTable;

    @FXML
    private TableColumn<Event, Integer> colEventId;

    @FXML
    private TableColumn<Event, String> colTitle;

    @FXML
    private TableColumn<Event, String> colDate;

    @FXML
    private TableColumn<Event, String> colLocation;

    @FXML
    private TableColumn<Event, Integer> colAvailableSeats;

    @FXML
    private Button btnEvents;

    @FXML
    private Button btnBook;

    @FXML
    private Button btnView;

    @FXML
    private Button btnManageEvents;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblStatus;

    private String currentUserName;
    private int currentUserId;
    private String currentUserRole;

    private ObservableList<Event> eventList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        loadEvents();
    }

    // Made PUBLIC so BookingHistoryController can call it
    public void loadEvents() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = """
                SELECT e.eventId, e.title, e.description, e.date, e.location, e.totalSeats,
                       (e.totalSeats - (SELECT COUNT(*) FROM seats s WHERE s.eventId = e.eventId AND s.status = 'reserved')) as availableSeats
                FROM events e
                ORDER BY e.date
                """;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            eventList.clear();

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("eventId"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("location"),
                        rs.getInt("availableSeats")
                );
                eventList.add(event);
            }

            eventTable.setItems(eventList);
            lblStatus.setText("Loaded " + eventList.size() + " events");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error loading events");
        }
    }

    @FXML
    private void handleBookTicket() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            lblStatus.setText("Please select an event first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("seat-view.fxml"));
            Scene scene = new Scene(loader.load());

            SeatSelectionController controller = loader.getController();
            controller.setEvent(selectedEvent, currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Select Seats - " + selectedEvent.getTitle());
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEvents();

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening seat selection");
        }
    }

    @FXML
    private void handleEventsNavigation() {
        loadEvents();
    }

    @FXML
    private void handleViewBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookingHistoryView.fxml"));
            Scene scene = new Scene(loader.load());

            BookingHistoryController controller = loader.getController();
            controller.setUserId(currentUserId);
            controller.setMainController(this);  // Pass reference for auto-refresh

            Stage stage = new Stage();
            stage.setTitle("My Booking History");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening booking history");
        }
    }

    @FXML
    private void handleManageEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Admin Event Management");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening admin management");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserInfo(String userName, int userId, String role) {
        this.currentUserName = userName;
        this.currentUserId = userId;
        this.currentUserRole = role;
        lblStatus.setText("Welcome, " + userName + "!");
        System.out.println("User logged in: " + userName + " (Role: " + role + ")");
    }

    public void enableAdminMode() {
        System.out.println("Admin mode enabled");
        if (btnManageEvents != null) {
            btnManageEvents.setVisible(true);
            btnManageEvents.setManaged(true);
        }
    }
}