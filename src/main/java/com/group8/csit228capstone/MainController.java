package com.group8.csit228capstone;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import java.time.LocalDate;

public class MainController {

    @FXML
    private Button btnRefreshEvents;

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblTotalEvents;

    @FXML
    private Label lblAvailableTickets;

    @FXML
    private Label lblUpcomingEvents;

    @FXML
    private HBox eventLoadingSpinner;

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
    private TableColumn<Event, Void> colAction;

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
    private Button activeButton;

    private ObservableList<Event> eventList = FXCollections.observableArrayList();

    @FXML
    private void handleRefreshEvents() {
        loadEvents();
        lblStatus.setText("Refreshing events...");
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        // Add Book Now button to each row
        colAction.setCellFactory(col -> new TableCell<Event, Void>() {
            private final Button bookBtn = new Button("🎫 Book Now");
            {
                bookBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                bookBtn.setOnAction(click -> {
                    Event selectedEvent = getTableView().getItems().get(getIndex());
                    handleBookTicketDirect(selectedEvent);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookBtn);
                }
            }
        });

        // Style past events with light red background
        eventTable.setRowFactory(tv -> new TableRow<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (event == null || empty) {
                    setStyle("");
                } else {
                    try {
                        LocalDate eventDate = LocalDate.parse(event.getDate());
                        LocalDate today = LocalDate.now();
                        if (eventDate.isBefore(today)) {
                            setStyle("-fx-background-color: #ffebee;"); // Light red for past events
                        } else {
                            setStyle("");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        if (eventLoadingSpinner != null) {
            eventLoadingSpinner.setVisible(false);
        }

        loadEvents();
        setActiveMenu(btnEvents);
    }

    private void setActiveMenu(Button button) {
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #b0b0cc; -fx-background-radius: 8;");
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #2d2d44; -fx-text-fill: white; -fx-background-radius: 8;");
        }
    }

    private boolean isEventPassed(Event event) {
        try {
            LocalDate eventDate = LocalDate.parse(event.getDate());
            LocalDate today = LocalDate.now();
            return eventDate.isBefore(today);
        } catch (Exception e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void loadEvents() {
        if (eventLoadingSpinner != null) {
            eventLoadingSpinner.setVisible(true);
        }
        lblStatus.setText("Loading events...");

        Task<ObservableList<Event>> loadTask = new Task<>() {
            @Override
            protected ObservableList<Event> call() throws Exception {
                ObservableList<Event> events = FXCollections.observableArrayList();
                int totalAvailable = 0;
                int upcomingCount = 0;
                String today = LocalDate.now().toString();

                Connection conn = DatabaseConnection.getInstance().getConnection();
                String sql = """
                SELECT e.eventId, e.title, e.description, e.date, e.location, e.totalSeats,
                       (e.totalSeats - (SELECT COUNT(*) FROM seats s WHERE s.eventId = e.eventId AND s.status = 'reserved')) as availableSeats
                FROM events e
                ORDER BY e.date
                """;
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int available = rs.getInt("availableSeats");
                    totalAvailable += available;

                    String eventDate = rs.getString("date");
                    if (eventDate.compareTo(today) >= 0) {
                        upcomingCount++;
                    }

                    Event event = new Event(
                            rs.getInt("eventId"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("date"),
                            rs.getString("location"),
                            available
                    );
                    events.add(event);
                }

                final int finalTotalAvailable = totalAvailable;
                final int finalUpcomingCount = upcomingCount;

                javafx.application.Platform.runLater(() -> {
                    if (lblTotalEvents != null) lblTotalEvents.setText(String.valueOf(events.size()));
                    if (lblAvailableTickets != null) lblAvailableTickets.setText(String.valueOf(finalTotalAvailable));
                    if (lblUpcomingEvents != null) lblUpcomingEvents.setText(String.valueOf(finalUpcomingCount));
                });

                // FOR DEMO - Make loading visible (remove or reduce for production)
                Thread.sleep(2000);  // ← CHANGE THIS to make spinner visible longer

                return events;
            }
        };

        loadTask.setOnSucceeded(result -> {
            eventList.setAll(loadTask.getValue());
            eventTable.setItems(eventList);
            lblStatus.setText("Loaded " + eventList.size() + " events");
            if (eventLoadingSpinner != null) {
                eventLoadingSpinner.setVisible(false);
            }
        });

        loadTask.setOnFailed(result -> {
            lblStatus.setText("Error loading events");
            if (eventLoadingSpinner != null) {
                eventLoadingSpinner.setVisible(false);
            }
        });

        new Thread(loadTask).start();
    }

    private void handleBookTicketDirect(Event selectedEvent) {
        // Check if event has already passed
        if (isEventPassed(selectedEvent)) {
            lblStatus.setText("❌ This event has already passed. Cannot book tickets.");
            showAlert("Event Passed", "This event occurred on " + selectedEvent.getDate() + ". You cannot book tickets for past events.");
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
            stage.setWidth(800);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEvents();

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening seat selection");
        }
    }

    @FXML
    private void handleBookTicket() {
        setActiveMenu(btnBook);

        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            lblStatus.setText("Please select an event first");
            return;
        }

        // Check if event has already passed
        if (isEventPassed(selectedEvent)) {
            lblStatus.setText("❌ This event has already passed. Cannot book tickets.");
            showAlert("Event Passed", "This event occurred on " + selectedEvent.getDate() + ". You cannot book tickets for past events.");
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
            stage.setWidth(800);
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
        setActiveMenu(btnEvents);
        loadEvents();
    }

    @FXML
    private void handleViewBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookingHistoryView.fxml"));
            Scene scene = new Scene(loader.load());

            BookingHistoryController controller = loader.getController();
            controller.setUserId(currentUserId, currentUserName, currentUserRole);
            controller.setMainController(this);

            Stage stage = (Stage) btnView.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("My Bookings");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening bookings");
        }
    }

    @FXML
    private void handleManageEvents() {
        setActiveMenu(btnManageEvents);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Scene scene = new Scene(loader.load());

            AdminController adminController = loader.getController();
            adminController.setUserInfo(currentUserName, currentUserId, currentUserRole);

            Stage stage = (Stage) btnManageEvents.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Panel - Event Management");
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening admin panel");
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
        if (lblStatus != null) {
            lblStatus.setText("Welcome back, " + userName + "! 👋");
        }
        if (lblWelcome != null) {
            lblWelcome.setText("👤 " + userName);
        }
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