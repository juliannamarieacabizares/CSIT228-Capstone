package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;

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

    @FXML
    private StackPane loadingPane;

    @FXML
    private ScrollPane scrollPane;

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

            String sql = "INSERT INTO seats (eventId, seatNumber, status) VALUES (?, ?, 'available')";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int batchSize = 0;
            for (int i = 1; i <= totalSeats; i++) {
                String seatNumber = "A" + i;
                pstmt.setInt(1, currentEvent.getEventId());
                pstmt.setString(2, seatNumber);
                pstmt.addBatch();
                batchSize++;

                if (batchSize >= 500) {
                    pstmt.executeBatch();
                    batchSize = 0;
                }
            }

            if (batchSize > 0) {
                pstmt.executeBatch();
            }

            System.out.println("Generated " + totalSeats + " seats for event: " + currentEvent.getTitle());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSeatStatusFromDatabase() {
        // Show loading spinner
        if (loadingPane != null) {
            loadingPane.setVisible(true);
        }
        if (scrollPane != null) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
        }
        if (textTotal != null) {
            textTotal.setText("Loading seats...");
        }

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                String sql = "SELECT seatNumber, status FROM seats WHERE eventId = ? ORDER BY CAST(SUBSTR(seatNumber, 2) AS INTEGER)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, currentEvent.getEventId());
                ResultSet rs = pstmt.executeQuery();

                List<Map.Entry<String, String>> seatList = new ArrayList<>();
                while (rs.next()) {
                    seatList.add(Map.entry(rs.getString("seatNumber"), rs.getString("status")));
                }
                rs.close();
                pstmt.close();

                // Simulate network delay
                Thread.sleep(200);

                javafx.application.Platform.runLater(() -> {
                    try {
                        seatGrid.getChildren().clear();
                        seatButtonMap.clear();

                        int totalSeats = seatList.size();
                        int btnWidth = 100;
                        int btnHeight = 35;
                        int fontSize = 12;

                        seatGrid.getColumnConstraints().clear();
                        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
                        col.setPrefWidth(btnWidth);
                        col.setHalignment(javafx.geometry.HPos.CENTER);
                        seatGrid.getColumnConstraints().add(col);

                        seatGrid.getRowConstraints().clear();
                        for (int i = 0; i < totalSeats; i++) {
                            javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
                            row.setPrefHeight(btnHeight);
                            seatGrid.getRowConstraints().add(row);
                        }

                        int rowIndex = 0;
                        for (Map.Entry<String, String> entry : seatList) {
                            final String seatNumber = entry.getKey();
                            final String status = entry.getValue();

                            String displayNumber = seatNumber;
                            if (displayNumber.startsWith("A0")) {
                                displayNumber = "A" + Integer.parseInt(displayNumber.substring(1));
                            }
                            final String displayText = displayNumber;

                            Button seatBtn = new Button(displayText);
                            seatBtn.setPrefSize(btnWidth, btnHeight);
                            seatBtn.setStyle("-fx-font-weight: bold; -fx-border-radius: 3; -fx-font-size: " + fontSize + ";");

                            if ("reserved".equals(status)) {
                                seatBtn.setStyle(seatBtn.getStyle() + "-fx-background-color: #f44336; -fx-text-fill: white;");
                                seatBtn.setDisable(true);
                            } else {
                                seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
                                seatBtn.setOnAction(e -> handleSeatSelection(seatBtn, displayText));
                            }

                            seatGrid.add(seatBtn, 0, rowIndex);
                            seatButtonMap.put(seatBtn, displayText);
                            rowIndex++;
                        }

                        updateTotalDisplay();

                        // Hide loading spinner
                        if (loadingPane != null) {
                            loadingPane.setVisible(false);
                        }
                        if (scrollPane != null) {
                            scrollPane.setVisible(true);
                            scrollPane.setManaged(true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                return null;
            }
        };

        loadTask.setOnFailed(event -> {
            System.err.println("Error loading seats: " + loadTask.getException().getMessage());
            if (textTotal != null) {
                textTotal.setText("Error loading seats");
            }
            if (loadingPane != null) {
                loadingPane.setVisible(false);
            }
        });

        new Thread(loadTask).start();
    }

    private void handleSeatSelection(Button clickedButton, String seatNumber) {
        if (selectedSeats.contains(seatNumber)) {
            clickedButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
            selectedSeats.remove(seatNumber);
            System.out.println("Seat deselected: " + seatNumber);
        } else {
            clickedButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
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
            showAlert(AlertType.WARNING, "No Selection", "Please select at least one seat.");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String bookingSql = "INSERT INTO bookings (userId, eventId, bookingDate) VALUES (?, ?, ?)";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {
                bookingStmt.setInt(1, currentUserId);
                bookingStmt.setInt(2, currentEvent.getEventId());
                bookingStmt.setString(3, LocalDate.now().toString());
                bookingStmt.executeUpdate();
            }

            int bookingId = -1;
            try (PreparedStatement lastIdStmt = conn.prepareStatement("SELECT last_insert_rowid()")) {
                ResultSet rs = lastIdStmt.executeQuery();
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
            }
            if (bookingId == -1) throw new SQLException("Failed to retrieve booking ID.");

            String ticketSql = "INSERT INTO tickets (bookingId, seatNumber) VALUES (?, ?)";
            String updateSeatSql = "UPDATE seats SET status = 'reserved' WHERE eventId = ? AND seatNumber = ?";

            try (PreparedStatement ticketStmt = conn.prepareStatement(ticketSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSeatSql)) {
                for (String seatNumber : selectedSeats) {
                    ticketStmt.setInt(1, bookingId);
                    ticketStmt.setString(2, seatNumber);
                    ticketStmt.addBatch();

                    updateStmt.setInt(1, currentEvent.getEventId());
                    updateStmt.setString(2, seatNumber);
                    updateStmt.addBatch();
                }
                ticketStmt.executeBatch();
                updateStmt.executeBatch();
            }

            conn.commit();

            showAlert(AlertType.INFORMATION, "Success", "Booking confirmed for: " + String.join(", ", selectedSeats));

            // Clear selected seats
            selectedSeats.clear();
            updateConfirmButton();
            updateTotalDisplay();

            // Reload seats to show reserved (RED)
            loadSeatStatusFromDatabase();

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Booking Failed", "Error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}