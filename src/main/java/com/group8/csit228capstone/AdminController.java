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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {
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
    @FXML
    private Label lblEventStatus;
    @FXML
    private Label lblBookingStatus;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEvents;
    @FXML
    private Tab tabBookings;
    @FXML
    private Button btnRefreshEvents;

    // ========== SEARCH & SORT FIELDS ==========
    @FXML private TextField searchEventField;
    @FXML private Button btnSearchEvent;
    @FXML private Button btnClearEventSearch;
    @FXML private Label lblEventSearchInfo;

    @FXML private RadioButton sortEventById;
    @FXML private RadioButton sortEventByTitle;
    @FXML private RadioButton sortEventByDate;
    @FXML private RadioButton sortEventByLocation;
    @FXML private Button btnApplyEventSort;

    @FXML private TextField searchBookingField;
    @FXML private Button btnSearchBooking;
    @FXML private Button btnClearBookingSearch;
    @FXML private Label lblBookingSearchInfo;

    @FXML private RadioButton sortBookingById;
    @FXML private RadioButton sortBookingByCustomer;
    @FXML private RadioButton sortBookingByEvent;
    @FXML private RadioButton sortBookingBySeat;
    @FXML private RadioButton sortBookingByDate;
    @FXML private Button btnApplyBookingSort;

    // ========== LOADING SPINNERS ==========
    @FXML private HBox eventLoadingSpinner;
    @FXML private HBox bookingLoadingSpinner;

    // ========== DATA LISTS ==========
    private final ObservableList<Event> allEventsList = FXCollections.observableArrayList();
    private final ObservableList<Event> displayedEventsList = FXCollections.observableArrayList();
    private final ObservableList<AdminBooking> allBookingsList = FXCollections.observableArrayList();
    private final ObservableList<AdminBooking> displayedBookingsList = FXCollections.observableArrayList();

    private Event selectedEvent;
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
        // Setup Event Table Columns
        colEventId.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        // Setup Booking Table Columns
        colAllBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colAllCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAllEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        colAllSeatNumber.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        colAllBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colAllPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Set up tables
        tblEvents.setItems(displayedEventsList);
        tblAllBookings.setItems(displayedBookingsList);

        // Load data
        loadEventsInBackground();

        // Event table selection listener
        tblEvents.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });

        // Set default sort selection
        sortEventById.setSelected(true);
        sortBookingById.setSelected(true);
    }

    // ========== EVENT SEARCH METHODS ==========
    @FXML
    private void handleSearchEvent() {
        String searchText = searchEventField != null ? searchEventField.getText().toLowerCase().trim() : "";

        if (searchText.isEmpty()) {
            displayedEventsList.setAll(allEventsList);
            lblEventSearchInfo.setText("");
        } else {
            List<Event> filtered = allEventsList.stream()
                    .filter(event ->
                            String.valueOf(event.getEventId()).toLowerCase().contains(searchText) ||
                                    event.getTitle().toLowerCase().contains(searchText) ||
                                    event.getLocation().toLowerCase().contains(searchText) ||
                                    event.getDate().toLowerCase().contains(searchText)
                    )
                    .collect(Collectors.toList());

            displayedEventsList.setAll(filtered);
            lblEventSearchInfo.setText("Found " + filtered.size() + " matching events");
        }

        if (lblEventStatus != null) {
            lblEventStatus.setText("Showing " + displayedEventsList.size() + " of " + allEventsList.size() + " events");
        }
    }

    @FXML
    private void handleClearEventSearch() {
        if (searchEventField != null) {
            searchEventField.clear();
        }
        displayedEventsList.setAll(allEventsList);
        lblEventSearchInfo.setText("");

        // Reset sort to default (by ID)
        sortEventById.setSelected(true);
        displayedEventsList.sort(Comparator.comparing(Event::getEventId));

        if (lblEventStatus != null) {
            lblEventStatus.setText("Loaded " + allEventsList.size() + " events (sorted by ID)");
        }
    }

    @FXML
    private void handleApplyEventSort() {
        Comparator<Event> comparator = null;

        if (sortEventById.isSelected()) {
            comparator = Comparator.comparing(Event::getEventId);
        } else if (sortEventByTitle.isSelected()) {
            comparator = Comparator.comparing(Event::getTitle);
        } else if (sortEventByDate.isSelected()) {
            comparator = Comparator.comparing(Event::getDate);
        } else if (sortEventByLocation.isSelected()) {
            comparator = Comparator.comparing(Event::getLocation);
        }

        if (comparator != null) {
            displayedEventsList.sort(comparator);
            if (lblEventStatus != null) lblEventStatus.setText("Sorted: " + displayedEventsList.size() + " events");
        }
    }

    // ========== BOOKING SEARCH METHODS ==========
    @FXML
    private void handleSearchBooking() {
        String searchText = searchBookingField != null ? searchBookingField.getText().toLowerCase().trim() : "";

        if (searchText.isEmpty()) {
            displayedBookingsList.setAll(allBookingsList);
            lblBookingSearchInfo.setText("");
        } else {
            List<AdminBooking> filtered = allBookingsList.stream()
                    .filter(booking ->
                            String.valueOf(booking.getBookingId()).toLowerCase().contains(searchText) ||
                                    booking.getCustomerName().toLowerCase().contains(searchText) ||
                                    booking.getEventTitle().toLowerCase().contains(searchText) ||
                                    booking.getSeatNumber().toLowerCase().contains(searchText)
                    )
                    .collect(Collectors.toList());

            displayedBookingsList.setAll(filtered);
            lblBookingSearchInfo.setText("Found " + filtered.size() + " matching bookings");
        }

        if (lblBookingStatus != null) {
            lblBookingStatus.setText("Showing " + displayedBookingsList.size() + " of " + allBookingsList.size() + " bookings");
        }
    }

    @FXML
    private void handleClearBookingSearch() {
        if (searchBookingField != null) {
            searchBookingField.clear();
        }
        displayedBookingsList.setAll(allBookingsList);
        lblBookingSearchInfo.setText("");

        // Reset sort to default (by ID)
        sortBookingById.setSelected(true);
        displayedBookingsList.sort(Comparator.comparing(AdminBooking::getBookingId));

        if (lblBookingStatus != null) {
            lblBookingStatus.setText("Loaded " + allBookingsList.size() + " bookings (sorted by ID)");
        }
    }

    @FXML
    private void handleApplyBookingSort() {
        Comparator<AdminBooking> comparator = null;

        if (sortBookingById.isSelected()) {
            comparator = Comparator.comparing(AdminBooking::getBookingId);
        } else if (sortBookingByCustomer.isSelected()) {
            comparator = Comparator.comparing(AdminBooking::getCustomerName);
        } else if (sortBookingByEvent.isSelected()) {
            comparator = Comparator.comparing(AdminBooking::getEventTitle);
        } else if (sortBookingBySeat.isSelected()) {
            comparator = Comparator.comparing(AdminBooking::getSeatNumber);
        } else if (sortBookingByDate.isSelected()) {
            comparator = Comparator.comparing(AdminBooking::getBookingDate);
        }

        if (comparator != null) {
            displayedBookingsList.sort(comparator);
            if (lblBookingStatus != null) lblBookingStatus.setText("Sorted: " + displayedBookingsList.size() + " bookings");
        }
    }

    @FXML
    private void handleRefreshEvents() {
        loadEventsInBackground();
    }

    @FXML
    private void handleRefreshBookings() {
        loadAllBookings();
    }

    private void loadAllBookings() {
        allBookingsList.clear();

        if (bookingLoadingSpinner != null) {
            bookingLoadingSpinner.setVisible(true);
        }
        if (tblAllBookings != null) {
            tblAllBookings.setVisible(false);
            tblAllBookings.setManaged(false);
        }
        if (lblBookingStatus != null) lblBookingStatus.setText("Loading bookings...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(300);

                String sql = """
                SELECT b.bookingId, u.name AS customerName, e.title AS eventTitle, 
                       t.seatNumber, b.bookingDate, COALESCE(b.paymentStatus, 'Confirmed') AS paymentStatus
                FROM bookings b
                JOIN users u ON b.userId = u.userId
                JOIN events e ON b.eventId = e.eventId
                JOIN tickets t ON b.bookingId = t.bookingId
                ORDER BY b.bookingDate DESC
                """;

                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;

                try {
                    conn = DatabaseConnection.getInstance().getConnection();
                    pstmt = conn.prepareStatement(sql);
                    rs = pstmt.executeQuery();

                    while (rs.next()) {
                        AdminBooking booking = new AdminBooking(
                                rs.getInt("bookingId"),
                                rs.getString("customerName"),
                                rs.getString("eventTitle"),
                                rs.getString("seatNumber"),
                                rs.getString("bookingDate"),
                                rs.getString("paymentStatus")
                        );
                        allBookingsList.add(booking);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
                    try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                    // DO NOT close connection
                }
                return null;
            }
        };

        loadTask.setOnSucceeded(result -> {
            displayedBookingsList.setAll(allBookingsList);
            if (bookingLoadingSpinner != null) bookingLoadingSpinner.setVisible(false);
            if (tblAllBookings != null) {
                tblAllBookings.setVisible(true);
                tblAllBookings.setManaged(true);
            }
            if (lblBookingStatus != null) lblBookingStatus.setText("Loaded " + allBookingsList.size() + " bookings");
        });

        loadTask.setOnFailed(result -> {
            if (bookingLoadingSpinner != null) bookingLoadingSpinner.setVisible(false);
            if (tblAllBookings != null) {
                tblAllBookings.setVisible(true);
                tblAllBookings.setManaged(true);
            }
            if (lblBookingStatus != null) lblBookingStatus.setText("Error loading bookings");
        });

        new Thread(loadTask).start();
    }

    public void loadEventsInBackground() {
        if (eventLoadingSpinner != null) {
            eventLoadingSpinner.setVisible(true);
        }
        if (tblEvents != null) {
            tblEvents.setVisible(false);
            tblEvents.setManaged(false);
        }
        if (lblEventStatus != null) lblEventStatus.setText("Loading events...");

        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() throws Exception {
                Thread.sleep(300);
                return fetchEventsFromDatabase();
            }
        };

        loadTask.setOnSucceeded(event -> {
            allEventsList.setAll(loadTask.getValue());
            displayedEventsList.setAll(allEventsList);

            if (eventLoadingSpinner != null) eventLoadingSpinner.setVisible(false);
            if (tblEvents != null) {
                tblEvents.setVisible(true);
                tblEvents.setManaged(true);
            }
            if (lblEventStatus != null) lblEventStatus.setText("Loaded " + allEventsList.size() + " events");
        });

        loadTask.setOnFailed(event -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();
            if (eventLoadingSpinner != null) eventLoadingSpinner.setVisible(false);
            if (tblEvents != null) {
                tblEvents.setVisible(true);
                tblEvents.setManaged(true);
            }
            if (lblEventStatus != null) lblEventStatus.setText("Error loading events: " + exception.getMessage());
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

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("eventId"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("location"),
                        rs.getInt("availableSeats")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch events: " + e.getMessage(), e);
        } finally {
            // Close resources in reverse order
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // DO NOT close the connection here - let the singleton manage it
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
        String sql = "SELECT totalSeats FROM events WHERE eventId = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, eventId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("totalSeats");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // DO NOT close connection
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String insertSql = "INSERT INTO events (title, description, date, location, totalSeats) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, desc);
                pstmt.setString(3, date);
                pstmt.setString(4, location);
                pstmt.setInt(5, totalSeats);
                pstmt.executeUpdate();
            }

            // Get the last inserted ID
            int eventId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    eventId = rs.getInt(1);
                }
            }

            if (eventId != -1) {
                // Generate seats
                String seatSql = "INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')";
                for (int i = 1; i <= totalSeats; i++) {
                    String seatNumber = "A" + i;
                    try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {
                        seatStmt.setInt(1, eventId);
                        seatStmt.setString(2, seatNumber);
                        seatStmt.executeUpdate();
                    }
                }

                if (lblEventStatus != null) lblEventStatus.setText("Event added successfully");
                clearFormFields();
                loadEventsInBackground();
                loadAllBookings();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error saving event: " + e.getMessage());
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int eventId = selectedEvent.getEventId();

            // Check reserved seats
            int reservedSeats = 0;
            String countSql = "SELECT COUNT(*) FROM seats WHERE eventId = ? AND status = 'reserved'";
            try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                pstmt.setInt(1, eventId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        reservedSeats = rs.getInt(1);
                    }
                }
            }

            if (requestedTotalSeats < reservedSeats) {
                showAlert("Cannot shrink seating", "There are " + reservedSeats + " reserved seats. Cannot reduce total seats below that.");
                return;
            }

            int currentTotalSeats = fetchEventTotalSeats(eventId);

            String updateSql = "UPDATE events SET title = ?, description = ?, date = ?, location = ?, totalSeats = ? WHERE eventId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, desc);
                pstmt.setString(3, date);
                pstmt.setString(4, location);
                pstmt.setInt(5, requestedTotalSeats);
                pstmt.setInt(6, eventId);
                pstmt.executeUpdate();
            }

            if (requestedTotalSeats > currentTotalSeats) {
                // Add new seats
                String seatSql = "INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')";
                for (int i = currentTotalSeats + 1; i <= requestedTotalSeats; i++) {
                    String seatNumber = "A" + i;
                    try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {
                        seatStmt.setInt(1, eventId);
                        seatStmt.setString(2, seatNumber);
                        seatStmt.executeUpdate();
                    }
                }
            } else if (requestedTotalSeats < currentTotalSeats) {
                // Remove available seats (only if not reserved)
                String deleteSql = "DELETE FROM seats WHERE seatId IN (SELECT seatId FROM seats WHERE eventId = ? AND status = 'available' ORDER BY seatId DESC LIMIT ?)";
                int seatsToRemove = currentTotalSeats - requestedTotalSeats;
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setInt(1, eventId);
                    pstmt.setInt(2, seatsToRemove);
                    pstmt.executeUpdate();
                }
            }

            if (lblEventStatus != null) lblEventStatus.setText("Event updated successfully");
            clearFormFields();
            loadEventsInBackground();
            loadAllBookings();

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error updating event: " + e.getMessage());
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
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                if (lblEventStatus != null) lblEventStatus.setText("Event deleted successfully");
                clearFormFields();
                loadEventsInBackground();
                loadAllBookings();
            } else {
                if (lblEventStatus != null) lblEventStatus.setText("Failed to delete event");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblEventStatus != null) lblEventStatus.setText("Error deleting event: " + e.getMessage());
            showAlert("Error", "Could not delete event: " + e.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        clearFormFields();
        tblEvents.getSelectionModel().clearSelection();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}