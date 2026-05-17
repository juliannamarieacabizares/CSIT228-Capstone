package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    // Event Form Fields
    @FXML
    private TextField textTitle;
    @FXML
    private TextField textDescription;
    @FXML
    private DatePicker textDate;
    @FXML
    private TextField textLocation;
    @FXML
    private TextField textTotalSeats;

    // Buttons
    @FXML
    private Button btnSave;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnBackToDashboard;
    @FXML
    private Button btnRefreshBookings;

    // Events Table
    @FXML
    private TableView<Event> tblEvents;
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

    // All Bookings Table
    @FXML
    private TableView<AdminBooking> tblAllBookings;
    @FXML
    private TableColumn<AdminBooking, Integer> colAllBookingId;
    @FXML
    private TableColumn<AdminBooking, String> colAllCustomerName;
    @FXML
    private TableColumn<AdminBooking, String> colAllEventTitle;
    @FXML
    private TableColumn<AdminBooking, String> colAllSeatNumber;
    @FXML
    private TableColumn<AdminBooking, String> colAllBookingDate;
    @FXML
    private TableColumn<AdminBooking, String> colAllPaymentStatus;

    // Status Labels
    @FXML
    private Label lblEventStatus;
    @FXML
    private Label lblBookingStatus;

    // TabPane
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEvents;
    @FXML
    private Tab tabBookings;

    private final ObservableList<Event> eventList = FXCollections.observableArrayList();
    private final ObservableList<AdminBooking> allBookingsList = FXCollections.observableArrayList();
    private Event selectedEvent;

    // User info fields
    private String currentUserName;
    private int currentUserId;
    private String currentUserRole;

    public void setUserInfo(String userName, int userId, String role) {
        this.currentUserName = userName;
        this.currentUserId = userId;
        this.currentUserRole = role;
    }

    @FXML
    public void initialize() {
        // Setup Events Table columns
        colEventId.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        // Setup All Bookings Table columns
        colAllBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colAllCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAllEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        colAllSeatNumber.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        colAllBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colAllPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        tblEvents.setItems(eventList);
        tblAllBookings.setItems(allBookingsList);

        loadEventsInBackground();

        // Selection listener for Events table
        tblEvents.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(loader.load());

            MainController mainController = loader.getController();
            mainController.setUserInfo(currentUserName, currentUserId, currentUserRole);
            if ("admin".equals(currentUserRole)) {
                mainController.enableAdminMode();
            }

            Stage stage = (Stage) btnBackToDashboard.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Event Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard");
        }
    }

    @FXML
    private void handleRefreshBookings() {
        loadAllBookings();
    }

    private void loadAllBookings() {
        allBookingsList.clear();
        if (lblBookingStatus != null) lblBookingStatus.setText("Loading bookings...");

        String sql = """
            SELECT b.bookingId, u.name AS customerName, e.title AS eventTitle, 
                   t.seatNumber, b.bookingDate, COALESCE(b.paymentStatus, 'Pending') AS paymentStatus
            FROM bookings b
            JOIN users u ON b.userId = u.userId
            JOIN events e ON b.eventId = e.eventId
            JOIN tickets t ON b.bookingId = t.bookingId
            ORDER BY b.bookingDate DESC
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                allBookingsList.add(new AdminBooking(
                        rs.getString("customerName"),
                        rs.getString("eventTitle"),
                        rs.getString("seatNumber"),
                        rs.getString("bookingDate"),
                        rs.getString("paymentStatus")
                ));
            }
            if (lblBookingStatus != null) lblBookingStatus.setText("Loaded " + allBookingsList.size() + " bookings");
        } catch (Exception e) {
            e.printStackTrace();
            if (lblBookingStatus != null) lblBookingStatus.setText("Error loading bookings");
        }
    }

    public void loadEventsInBackground() {
        if (lblEventStatus != null) lblEventStatus.setText("Loading events...");

        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() {
                return fetchEventsFromDatabase();
            }
        };

        loadTask.setOnSucceeded(event -> {
            eventList.setAll(loadTask.getValue());
            if (lblEventStatus != null) lblEventStatus.setText("Loaded " + eventList.size() + " events");
        });

        loadTask.setOnFailed(event -> {
            loadTask.getException().printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error loading events");
        });

        Thread thread = new Thread(loadTask, "AdminEventLoader");
        thread.setDaemon(true);
        thread.start();
    }

    private List<Event> fetchEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
        String sql = """
                SELECT e.eventId, e.title, e.description, e.date, e.location, e.totalSeats,
                       (e.totalSeats - (SELECT COUNT(*) FROM seats s WHERE s.eventId = e.eventId AND s.status = 'reserved')) AS availableSeats
                FROM events e
                ORDER BY e.date
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("eventId"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("location"),
                        rs.getInt("availableSeats")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return events;
    }

    private void populateForm(Event event) {
        selectedEvent = event;
        if (event == null) {
            clearFormFields();
            return;
        }
        textTitle.setText(event.getTitle());
        textDescription.setText(event.getDescription());
        try {
            textDate.setValue(LocalDate.parse(event.getDate()));
        } catch (Exception e) {
            textDate.setValue(null);
        }
        textLocation.setText(event.getLocation());
        textTotalSeats.setText(String.valueOf(fetchEventTotalSeats(event.getEventId())));
    }

    private int fetchEventTotalSeats(int eventId) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT totalSeats FROM events WHERE eventId = ?")) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("totalSeats");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    public void handleSave() {
        if (!validateEventForm()) {
            return;
        }

        String title = textTitle.getText().trim();
        String desc = textDescription.getText().trim();
        String date = textDate.getValue() != null ? textDate.getValue().toString() : "";
        String location = textLocation.getText().trim();
        int totalSeats = Integer.parseInt(textTotalSeats.getText().trim());

        if (date.isEmpty()) {
            showAlert("Error", "Please select a date");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String insertSql = "INSERT INTO events (title, description, date, location, totalSeats) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, title);
            pstmt.setString(2, desc);
            pstmt.setString(3, date);
            pstmt.setString(4, location);
            pstmt.setInt(5, totalSeats);
            pstmt.executeUpdate();
            pstmt.close();

            String idSql = "SELECT last_insert_rowid()";
            pstmt = conn.prepareStatement(idSql);
            ResultSet rs = pstmt.executeQuery();
            int eventId = rs.next() ? rs.getInt(1) : -1;
            pstmt.close();

            if (eventId != -1) {
                generateSeatsForEvent(conn, eventId, totalSeats);
                if (lblEventStatus != null) lblEventStatus.setText("Event added successfully");
                clearFormFields();
                loadEventsInBackground();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error saving event: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleUpdate() {
        if (selectedEvent == null) {
            showAlert("No event selected", "Please select an event to update.");
            return;
        }
        if (!validateEventForm()) {
            return;
        }

        String title = textTitle.getText().trim();
        String desc = textDescription.getText().trim();
        String date = textDate.getValue() != null ? textDate.getValue().toString() : "";
        String location = textLocation.getText().trim();
        int requestedTotalSeats = Integer.parseInt(textTotalSeats.getText().trim());

        if (date.isEmpty()) {
            showAlert("Error", "Please select a date");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            int eventId = selectedEvent.getEventId();

            int reservedSeats = 0;
            String countSql = "SELECT COUNT(*) FROM seats WHERE eventId = ? AND status = 'reserved'";
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                reservedSeats = rs.getInt(1);
            }
            rs.close();
            pstmt.close();

            if (requestedTotalSeats < reservedSeats) {
                showAlert("Cannot shrink seating", "There are " + reservedSeats + " reserved seats. Cannot reduce total seats below that.");
                return;
            }

            int currentTotalSeats = fetchEventTotalSeats(eventId);

            String updateSql = "UPDATE events SET title = ?, description = ?, date = ?, location = ?, totalSeats = ? WHERE eventId = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, title);
            pstmt.setString(2, desc);
            pstmt.setString(3, date);
            pstmt.setString(4, location);
            pstmt.setInt(5, requestedTotalSeats);
            pstmt.setInt(6, eventId);
            pstmt.executeUpdate();
            pstmt.close();

            if (requestedTotalSeats > currentTotalSeats) {
                addSeatsForEvent(conn, eventId, requestedTotalSeats, currentTotalSeats + 1);
            } else if (requestedTotalSeats < currentTotalSeats) {
                removeAvailableSeats(conn, eventId, currentTotalSeats - requestedTotalSeats);
            }

            if (lblEventStatus != null) lblEventStatus.setText("Event updated successfully");
            clearFormFields();
            loadEventsInBackground();

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error updating event: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedEvent == null) {
            showAlert("No event selected", "Please select an event to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Event");
        confirmation.setHeaderText("Delete event: " + selectedEvent.getTitle());
        confirmation.setContentText("This will remove the event and all related seats, bookings, and tickets.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "DELETE FROM events WHERE eventId = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedEvent.getEventId());
            pstmt.executeUpdate();
            pstmt.close();

            if (lblEventStatus != null) lblEventStatus.setText("Event deleted successfully");
            clearFormFields();
            loadEventsInBackground();

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error deleting event: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleClear() {
        clearFormFields();
        tblEvents.getSelectionModel().clearSelection();
    }

    private boolean validateEventForm() {
        if (textTitle.getText().trim().isEmpty()
                || textDescription.getText().trim().isEmpty()
                || textLocation.getText().trim().isEmpty()
                || textTotalSeats.getText().trim().isEmpty()) {
            showAlert("Validation Error", "All event fields must be filled.");
            return false;
        }
        if (textDate.getValue() == null) {
            showAlert("Validation Error", "Please select a date.");
            return false;
        }
        try {
            int seats = Integer.parseInt(textTotalSeats.getText().trim());
            if (seats < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Total seats must be a positive whole number.");
            return false;
        }
        return true;
    }

    private void clearFormFields() {
        textTitle.clear();
        textDescription.clear();
        textDate.setValue(null);
        textLocation.clear();
        textTotalSeats.clear();
        if (lblEventStatus != null) lblEventStatus.setText("Ready");
        selectedEvent = null;
    }

    private void generateSeatsForEvent(Connection conn, int eventId, int totalSeats) throws SQLException {
        addSeatsForEvent(conn, eventId, totalSeats, 1);
    }

    private void addSeatsForEvent(Connection conn, int eventId, int totalSeats, int startNumber) throws SQLException {
        String sql = "INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')";
        PreparedStatement pstmt = null;
        for (int i = startNumber; i <= totalSeats; i++) {
            String seatNumber = "A" + i;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, eventId);
            pstmt.setString(2, seatNumber);
            pstmt.executeUpdate();
            pstmt.close();
        }
    }

    private void removeAvailableSeats(Connection conn, int eventId, int count) throws SQLException {
        String sql = "DELETE FROM seats WHERE seatId IN (SELECT seatId FROM seats WHERE eventId = ? AND status = 'available' ORDER BY seatId DESC LIMIT ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, eventId);
        pstmt.setInt(2, count);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}