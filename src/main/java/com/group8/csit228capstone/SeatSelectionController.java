package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatSelectionController {

    @FXML
    private Label lblEventName;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnBack;

    @FXML
    private GridPane seatGrid;

    @FXML
    private Label textTotal;

    private Event currentEvent;
    private int currentUserId;
    private List<String> selectedSeats = new ArrayList<>();
    private Map<Button, String> seatButtonMap = new HashMap<>();

    public void setEvent(Event event, int userId) {
        this.currentEvent = event;
        this.currentUserId = userId;
        lblEventName.setText("Event: " + event.getTitle());
        ensureSeatsExist();
        loadSeatStatusFromDatabase();
        updateConfirmButton();
        updateTotalDisplay();
    }

    private void ensureSeatsExist() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String checkSql = "SELECT COUNT(*) FROM seats WHERE eventId = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, currentEvent.getEventId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                generateSeatsForEvent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSeatsForEvent() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            int totalSeats = currentEvent.getAvailableSeats();
            int seatsPerRow = 10;
            int numRows = (int) Math.ceil((double) totalSeats / seatsPerRow);

            int seatCounter = 1;
            for (int row = 0; row < numRows; row++) {
                char rowChar = (char) ('A' + row);
                for (int seatNum = 1; seatNum <= seatsPerRow && seatCounter <= totalSeats; seatNum++) {
                    String seatNumber = rowChar + String.valueOf(seatNum);
                    String sql = "INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, currentEvent.getEventId());
                    pstmt.setString(2, seatNumber);
                    pstmt.executeUpdate();
                    seatCounter++;
                }
            }
            System.out.println("Generated " + totalSeats + " seats for event: " + currentEvent.getTitle());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSeatStatusFromDatabase() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT seatNumber, status FROM seats WHERE eventId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentEvent.getEventId());
            ResultSet rs = pstmt.executeQuery();

            Map<String, String> seatStatusMap = new HashMap<>();
            while (rs.next()) {
                seatStatusMap.put(rs.getString("seatNumber"), rs.getString("status"));
            }

            seatGrid.getChildren().clear();
            seatButtonMap.clear();

            int maxSeats = seatStatusMap.size();
            int seatsPerRow = 10;
            int numRows = (int) Math.ceil((double) maxSeats / seatsPerRow);

            seatGrid.getColumnConstraints().clear();
            for (int i = 0; i < seatsPerRow; i++) {
                javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
                col.setPrefWidth(70);
                col.setHalignment(javafx.geometry.HPos.CENTER);
                seatGrid.getColumnConstraints().add(col);
            }

            seatGrid.getRowConstraints().clear();
            for (int i = 0; i < numRows; i++) {
                javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
                row.setPrefHeight(50);
                seatGrid.getRowConstraints().add(row);
            }

            int rowIndex = 0;
            int colIndex = 0;
            for (Map.Entry<String, String> entry : seatStatusMap.entrySet()) {
                String seatNumber = entry.getKey();
                String status = entry.getValue();

                Button seatBtn = new Button(seatNumber);
                seatBtn.setPrefSize(70, 50);
                seatBtn.setStyle("-fx-font-weight: bold; -fx-border-radius: 5;");

                if ("reserved".equals(status)) {
                    seatBtn.setStyle(seatBtn.getStyle() + "-fx-background-color: #f44336; -fx-text-fill: white;");
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5;");
                    seatBtn.setOnAction(e -> handleSeatSelection(seatBtn, seatNumber));
                }

                seatGrid.add(seatBtn, colIndex, rowIndex);
                seatButtonMap.put(seatBtn, seatNumber);

                colIndex++;
                if (colIndex >= seatsPerRow) {
                    colIndex = 0;
                    rowIndex++;
                }
            }

            updateTotalDisplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSeatSelection(Button clickedButton, String seatNumber) {
        if (selectedSeats.contains(seatNumber)) {
            clickedButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5;");
            selectedSeats.remove(seatNumber);
            System.out.println("Seat deselected: " + seatNumber);
        } else {
            clickedButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5;");
            selectedSeats.add(seatNumber);
            System.out.println("Seat selected: " + seatNumber);
        }

        updateConfirmButton();
        updateTotalDisplay();
    }

    private void updateConfirmButton() {
        btnConfirm.setDisable(selectedSeats.isEmpty());
    }

    private void updateTotalDisplay() {
        if (textTotal != null) {
            textTotal.setText("Selected: " + selectedSeats.size() + " seat(s)");
        }
    }

    @FXML
    private void handleSeatSelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String seatNumber = seatButtonMap.get(clickedButton);
        if (seatNumber != null) {
            handleSeatSelection(clickedButton, seatNumber);
        }
    }

    @FXML
    private void handleConfirm() {
        if (selectedSeats.isEmpty()) {
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            int bookingId = -1;

            String bookingSql = "INSERT INTO bookings (userId, eventId, bookingDate) VALUES (?, ?, ?)";
            PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
            bookingStmt.setInt(1, currentUserId);
            bookingStmt.setInt(2, currentEvent.getEventId());
            bookingStmt.setString(3, LocalDate.now().toString());
            bookingStmt.executeUpdate();

            String lastIdSql = "SELECT last_insert_rowid()";
            PreparedStatement lastIdStmt = conn.prepareStatement(lastIdSql);
            ResultSet rs = lastIdStmt.executeQuery();
            if (rs.next()) {
                bookingId = rs.getInt(1);
            }

            for (String seatNumber : selectedSeats) {
                String ticketSql = "INSERT INTO tickets (bookingId, seatNumber) VALUES (?, ?)";
                PreparedStatement ticketStmt = conn.prepareStatement(ticketSql);
                ticketStmt.setInt(1, bookingId);
                ticketStmt.setString(2, seatNumber);
                ticketStmt.executeUpdate();

                String updateSql = "UPDATE seats SET status = 'reserved' WHERE eventId = ? AND seatNumber = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, currentEvent.getEventId());
                updateStmt.setString(2, seatNumber);
                updateStmt.executeUpdate();
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Booking Successful");
            alert.setHeaderText(null);
            alert.setContentText("Successfully booked " + selectedSeats.size() + " seat(s):\n" + String.join(", ", selectedSeats));
            alert.showAndWait();

            Stage stage = (Stage) btnConfirm.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Booking Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to book seats: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}