# Event Ticketing System

## Group Members
- Cabizares Julianna Marie A.
- Muaña Kenric
- Lazarte Matt Lister F.
- Cabrillos John Harvey C.
- Tesaluna France Loyd P.
- Salado Samuel O.

## Project Description
The Event Ticketing System is a desktop-based application that allows users to browse events, select seats, and book tickets. It helps manage event reservations efficiently and avoids manual booking errors such as overbooking and lost records.

## Implemented Features

### User Features
- ✅ User login and authentication (Customer and Admin roles)
- ✅ Browse and search events
- ✅ Select seats from interactive seat map (Green = Available, Red = Reserved, Blue = Selected)
- ✅ Purchase tickets with multiple seat selection
- ✅ Cancel tickets
- ✅ View booking history
- ✅ Show/Hide password toggle on login screen
- ✅ Full-screen state preservation when switching screens

### Admin Features
- ✅ Manage events (Add, Edit, Delete)
- ✅ View all customer bookings
- ✅ **Search events** by ID, title, location, or date (button-based)
- ✅ **Search bookings** by ID, customer name, event title, or seat number
- ✅ **Sort events** by ID, Title, Date, or Location
- ✅ **Sort bookings** by ID, Customer Name, Event Title, Seat Number, or Booking Date
- ✅ Loading spinners for all database operations

## Technologies Used
- Java (OOP)
- JavaFX (GUI with FXML)
- JDBC (Database connectivity)
- SQLite (Lightweight embedded database)
- Scene Builder for layout design

## Object-Oriented Programming (OOP) Implementation
- **User** - Handles authentication and user data
- **Event** - Manages event information
- **Booking** - Handles ticket reservations
- **AdminBooking** - Admin view of all bookings
- **Seat** - Seat availability and selection (seat grid generation)
- **DatabaseConnection** - Singleton pattern for DB access

## Design Pattern Used
- **Singleton Pattern** - `DatabaseConnection` class uses `getInstance()` method to ensure single database connection instance

## Multithreading Implementation
- **JavaFX Task** - Loading events, seats, and bookings in background threads to prevent UI freezing
- Loading spinners visible during all database operations

## JavaFX GUI Features
- FXML files for all views (`login-view.fxml`, `register-view.fxml`, `main-view.fxml`, `seat-view.fxml`, `BookingHistoryView.fxml`, `admin-view.fxml`)
- TableView for displaying events and bookings
- Forms for data entry with validation
- Interactive seat map with color-coded buttons
- Real-time search and sort functionality
- Modern UI with gradients, shadows, and rounded corners

## UML Diagrams
- Use Case Diagram - Shows actors (Customer, Admin) and system actions
- Class Diagram - Shows 6+ core classes with attributes, methods, and relationships
- Both diagrams included in `/diagrams` folder

## GitHub Repository
- Proper project structure with `src/`, `diagrams/`, `README.md`, `.gitignore`
- All group members added as collaborators

## Project Structure
CSIT228-Capstone/
├── src/
│ └── main/
│ ├── java/
│ │ ├── com/group8/csit228capstone/
│ │ │ ├── MainApplication.java
│ │ │ ├── LoginController.java
│ │ │ ├── RegisterController.java
│ │ │ ├── MainController.java
│ │ │ ├── SeatSelectionController.java
│ │ │ ├── BookingHistoryController.java
│ │ │ ├── AdminController.java
│ │ │ ├── User.java
│ │ │ ├── Event.java
│ │ │ ├── Booking.java
│ │ │ └── AdminBooking.java
│ │ └── database/
│ │ └── DatabaseConnection.java
│ └── resources/com/group8/csit228capstone/
│ ├── login-view.fxml
│ ├── register-view.fxml
│ ├── main-view.fxml
│ ├── seat-view.fxml
│ ├── BookingHistoryView.fxml
│ └── admin-view.fxml
├── diagrams/
│ ├── use-case-diagram.png
│ └── class-diagram.png
├── eventticketing.db
├── README.md
└── .gitignore
