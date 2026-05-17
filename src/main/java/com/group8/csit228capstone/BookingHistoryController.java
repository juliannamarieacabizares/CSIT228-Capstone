package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookingHistoryController {

    @FXML
    private TableView<Booking> tblBookings;

    @FXML
    private TableColumn<Booking, Integer> colBookingId;

    @FXML
    private TableColumn<Booking, String> colEventTitle;

    @FXML
    private TableColumn<Booking, String> colEventDate;

    @FXML
    private TableColumn<Booking, String> colSeatNumber;

    @FXML
    private TableColumn<Booking, String> colBookingDate;

    @FXML
    private Label lblStatus;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnBackToDashboard;

    private int currentUserId;
    private String currentUserName;
    private String currentUserRole;
    private MainController mainController;
    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredList;

    @FXML
    public void initialize() {
        colBookingId.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty().asObject());
        colEventTitle.setCellValueFactory(cellData -> cellData.getValue().eventTitleProperty());
        colEventDate.setCellValueFactory(cellData -> cellData.getValue().eventDateProperty());
        colSeatNumber.setCellValueFactory(cellData -> cellData.getValue().seatNumberProperty());
        colBookingDate.setCellValueFactory(cellData -> cellData.getValue().bookingDateProperty());

        txtSearch.textProperty().addListener((obs, old, newVal) -> filterBookings(newVal));
    }

    public void setUserId(int userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserRole = role;
        loadBookings();
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private void loadBookings() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = """
                SELECT b.bookingId, e.title, e.date, t.seatNumber, b.bookingDate
                FROM bookings b
                JOIN events e ON b.eventId = e.eventId
                JOIN tickets t ON b.bookingId = t.bookingId
                WHERE b.userId = ?
                ORDER BY b.bookingDate DESC, t.seatNumber
                """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            bookingList.clear();

            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("bookingId"),
                        rs.getString("title"),
                        rs.getString("date"),
                        rs.getString("seatNumber"),
                        rs.getString("bookingDate")
                );
                bookingList.add(booking);
            }

            filteredList = new FilteredList<>(bookingList, p -> true);
            tblBookings.setItems(filteredList);
            lblStatus.setText("Found " + bookingList.size() + " ticket(s)");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error loading bookings");
        }
    }

    private void filterBookings(String keyword) {
        if (filteredList == null) return;

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.setPredicate(p -> true);
            lblStatus.setText("Showing " + filteredList.size() + " of " + bookingList.size() + " ticket(s)");
        } else {
            String lowerKeyword = keyword.toLowerCase().trim();
            filteredList.setPredicate(booking ->
                    booking.getEventTitle().toLowerCase().contains(lowerKeyword) ||
                            booking.getSeatNumber().toLowerCase().contains(lowerKeyword)
            );
            lblStatus.setText("Found " + filteredList.size() + " matching ticket(s)");
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selected = tblBookings.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Please select a ticket to cancel.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Ticket for Seat " + selected.getSeatNumber());
        confirmAlert.setContentText("Are you sure you want to cancel this ticket?\n\n" +
                "Event: " + selected.getEventTitle() + "\n" +
                "Seat: " + selected.getSeatNumber() + "\n" +
                "Date: " + selected.getEventDate());

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            cancelBooking(selected);
        }
    }

    private void cancelBooking(Booking booking) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            String getTicketSql = "SELECT t.ticketId, t.seatNumber, b.eventId FROM tickets t JOIN bookings b ON t.bookingId = b.bookingId WHERE t.bookingId = ? AND t.seatNumber = ?";
            PreparedStatement getStmt = conn.prepareStatement(getTicketSql);
            getStmt.setInt(1, booking.getBookingId());
            getStmt.setString(2, booking.getSeatNumber());
            ResultSet rs = getStmt.executeQuery();

            int ticketId = -1;
            String seatNumber = null;
            int eventId = -1;
            if (rs.next()) {
                ticketId = rs.getInt("ticketId");
                seatNumber = rs.getString("seatNumber");
                eventId = rs.getInt("eventId");
            }

            String deleteTicketSql = "DELETE FROM tickets WHERE ticketId = ?";
            PreparedStatement ticketStmt = conn.prepareStatement(deleteTicketSql);
            ticketStmt.setInt(1, ticketId);
            ticketStmt.executeUpdate();

            String checkTicketsSql = "SELECT COUNT(*) FROM tickets WHERE bookingId = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkTicketsSql);
            checkStmt.setInt(1, booking.getBookingId());
            ResultSet checkRs = checkStmt.executeQuery();

            int remainingTickets = 0;
            if (checkRs.next()) {
                remainingTickets = checkRs.getInt(1);
            }

            if (remainingTickets == 0) {
                String deleteBookingSql = "DELETE FROM bookings WHERE bookingId = ?";
                PreparedStatement bookingStmt = conn.prepareStatement(deleteBookingSql);
                bookingStmt.setInt(1, booking.getBookingId());
                bookingStmt.executeUpdate();
                System.out.println("Booking #" + booking.getBookingId() + " deleted (no tickets left)");
            }

            if (seatNumber != null && eventId != -1) {
                String updateSql = "UPDATE seats SET status = 'available' WHERE eventId = ? AND seatNumber = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, eventId);
                updateStmt.setString(2, seatNumber);
                updateStmt.executeUpdate();
            }

            if (mainController != null) {
                mainController.loadEvents();
            }

            showAlert("Success", "Ticket for seat " + booking.getSeatNumber() + " has been cancelled.");
            loadBookings();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel ticket: " + e.getMessage());
        }
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
    private void handleClose() {
        Stage stage = (Stage) tblBookings.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}