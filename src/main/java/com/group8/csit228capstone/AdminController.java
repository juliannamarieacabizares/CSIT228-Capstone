package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminController {
    @FXML
    private TextField textTitle;
    @FXML
    private TextField textDescription;
    @FXML
    private TextField textDate;
    @FXML
    private TextField textLocation;
    @FXML
    private TextField textTotalSeats;

    @FXML
    private Button btnSave;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnAllBookings;
    @FXML
    private Button btnRefreshBookings;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEvents;
    @FXML
    private Tab tabBookings;

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

    @FXML
    private TableView<AdminBooking> tblBookingsTable;
    @FXML
    private TableColumn<AdminBooking, String> colCustomerName;
    @FXML
    private TableColumn<AdminBooking, String> colBookingEventTitle;
    @FXML
    private TableColumn<AdminBooking, String> colBookingSeatNumber;
    @FXML
    private TableColumn<AdminBooking, String> colBookingDate;
    @FXML
    private TableColumn<AdminBooking, String> colPaymentStatus;

    @FXML
    private Label lblEventStatus;
    @FXML
    private Label lblBookingStatus;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private StackPane eventStack;

    private final ObservableList<Event> eventList = FXCollections.observableArrayList();
    private final ObservableList<AdminBooking> bookingList = FXCollections.observableArrayList();
    private Event selectedEvent;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colBookingEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        colBookingSeatNumber.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        tblEvents.setItems(eventList);
        tblBookingsTable.setItems(bookingList);

        tblEvents.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> populateForm(newSelection));
        progressIndicator.setVisible(false);
        loadEventsInBackground();
    }

    private void loadEventsInBackground() {
        progressIndicator.setVisible(true);
        lblEventStatus.setText("Loading events...");

        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() {
                return fetchEventsFromDatabase();
            }
        };

        loadTask.setOnSucceeded(event -> {
            eventList.setAll(loadTask.getValue());
            lblEventStatus.setText("Loaded " + eventList.size() + " events");
            progressIndicator.setVisible(false);
        });

        loadTask.setOnFailed(event -> {
            loadTask.getException().printStackTrace();
            lblEventStatus.setText("Error loading events");
            progressIndicator.setVisible(false);
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
        textDate.setText(event.getDate());
        textLocation.setText(event.getLocation());
        textTotalSeats.setText(String.valueOf(fetchEventTotalSeats(event.getEventId())));
    }

    private int fetchEventTotalSeats(int eventId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT totalSeats FROM events WHERE eventId = ?")) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalSeats");
                }
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String insertSql = "INSERT INTO events (title, description, date, location, totalSeats) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, textTitle.getText().trim());
                pstmt.setString(2, textDescription.getText().trim());
                pstmt.setString(3, textDate.getText().trim());
                pstmt.setString(4, textLocation.getText().trim());
                pstmt.setInt(5, Integer.parseInt(textTotalSeats.getText().trim()));
                pstmt.executeUpdate();
            }

            int eventId;
            try (PreparedStatement idStmt = conn.prepareStatement("SELECT last_insert_rowid()")) {
                ResultSet rs = idStmt.executeQuery();
                eventId = rs.next() ? rs.getInt(1) : -1;
            }

            if (eventId != -1) {
                generateSeatsForEvent(conn, eventId, Integer.parseInt(textTotalSeats.getText().trim()));
                lblEventStatus.setText("Event added successfully");
                clearFormFields();
                loadEventsInBackground();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblEventStatus.setText("Error saving event");
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int eventId = selectedEvent.getEventId();
            int currentTotalSeats = fetchEventTotalSeats(eventId);
            int requestedTotalSeats = Integer.parseInt(textTotalSeats.getText().trim());

            if (requestedTotalSeats < 1) {
                showAlert("Invalid seats", "Total seats must be at least 1.");
                return;
            }

            if (requestedTotalSeats < countReservedSeats(eventId)) {
                showAlert("Cannot shrink seating", "There are more reserved seats than the requested total.");
                return;
            }

            String updateSql = "UPDATE events SET title = ?, description = ?, date = ?, location = ?, totalSeats = ? WHERE eventId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, textTitle.getText().trim());
                pstmt.setString(2, textDescription.getText().trim());
                pstmt.setString(3, textDate.getText().trim());
                pstmt.setString(4, textLocation.getText().trim());
                pstmt.setInt(5, requestedTotalSeats);
                pstmt.setInt(6, eventId);
                pstmt.executeUpdate();
            }

            adjustSeatInventory(conn, eventId, currentTotalSeats, requestedTotalSeats);
            lblEventStatus.setText("Event updated successfully");
            loadEventsInBackground();
        } catch (SQLException e) {
            e.printStackTrace();
            lblEventStatus.setText("Error updating event");
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM events WHERE eventId = ?")) {
            pstmt.setInt(1, selectedEvent.getEventId());
            pstmt.executeUpdate();
            lblEventStatus.setText("Event deleted and related data removed");
            clearFormFields();
            loadEventsInBackground();
        } catch (SQLException e) {
            e.printStackTrace();
            lblEventStatus.setText("Error deleting event");
        }
    }

    @FXML
    public void handleClear() {
        clearFormFields();
        tblEvents.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleShowAllBookings() {
        tabPane.getSelectionModel().select(tabBookings);
        loadAllBookings();
    }

    @FXML
    public void handleRefreshBookings() {
        loadAllBookings();
    }

    private boolean validateEventForm() {
        if (textTitle.getText().trim().isEmpty()
                || textDescription.getText().trim().isEmpty()
                || textDate.getText().trim().isEmpty()
                || textLocation.getText().trim().isEmpty()
                || textTotalSeats.getText().trim().isEmpty()) {
            showAlert("Validation Error", "All event fields must be filled.");
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

    private int countReservedSeats(int eventId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM seats WHERE eventId = ? AND status = 'reserved'")) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void adjustSeatInventory(Connection conn, int eventId, int currentTotalSeats, int requestedTotalSeats) throws SQLException {
        if (requestedTotalSeats > currentTotalSeats) {
            generateSeatsForEvent(conn, eventId, requestedTotalSeats, currentTotalSeats + 1);
        } else if (requestedTotalSeats < currentTotalSeats) {
            int difference = currentTotalSeats - requestedTotalSeats;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM seats WHERE seatId IN (SELECT seatId FROM seats WHERE eventId = ? AND status = 'available' ORDER BY seatId DESC LIMIT ?)")) {
                pstmt.setInt(1, eventId);
                pstmt.setInt(2, difference);
                pstmt.executeUpdate();
            }
        }
    }

    private void generateSeatsForEvent(Connection conn, int eventId, int totalSeats) throws SQLException {
        generateSeatsForEvent(conn, eventId, totalSeats, 1);
    }

    private void generateSeatsForEvent(Connection conn, int eventId, int totalSeats, int startNumber) throws SQLException {
        int seatsPerRow = 10;
        int seatCounter = startNumber;
        int row = (startNumber - 1) / seatsPerRow;
        while (seatCounter <= totalSeats) {
            char rowChar = (char) ('A' + row);
            int seatNumber = ((seatCounter - 1) % seatsPerRow) + 1;
            String seatLabel = rowChar + String.valueOf(seatNumber);
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')")) {
                pstmt.setInt(1, eventId);
                pstmt.setString(2, seatLabel);
                pstmt.executeUpdate();
            }
            seatCounter++;
            if (seatNumber == seatsPerRow) {
                row++;
            }
        }
    }

    private void clearFormFields() {
        textTitle.clear();
        textDescription.clear();
        textDate.clear();
        textLocation.clear();
        textTotalSeats.clear();
        lblEventStatus.setText("Ready");
        selectedEvent = null;
    }

    private void loadAllBookings() {
        bookingList.clear();
        lblBookingStatus.setText("Loading bookings...");
        String sql = """
                SELECT u.name AS customerName, e.title AS eventTitle, t.seatNumber, b.bookingDate,
                       COALESCE(b.paymentStatus, 'Paid') AS paymentStatus
                FROM bookings b
                JOIN users u ON b.userId = u.userId
                JOIN events e ON b.eventId = e.eventId
                JOIN tickets t ON b.bookingId = t.bookingId
                ORDER BY b.bookingDate DESC, u.name, e.title
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                bookingList.add(new AdminBooking(
                        rs.getString("customerName"),
                        rs.getString("eventTitle"),
                        rs.getString("seatNumber"),
                        rs.getString("bookingDate"),
                        rs.getString("paymentStatus")
                ));
            }
            lblBookingStatus.setText("Loaded " + bookingList.size() + " bookings");
        } catch (SQLException e) {
            e.printStackTrace();
            lblBookingStatus.setText("Error loading bookings");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
