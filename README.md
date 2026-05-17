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

## Proposed Features
- User login and authentication (Customer and Admin)
- Browse and search events
- Select seats from interactive seat map
- Purchase and cancel tickets
- View booking history
- Admin: Manage events (add, edit, delete)
- Admin: View all customer bookings

## Planned Technologies
- Java (OOP)
- JavaFX (GUI with FXML)
- JDBC (Database connectivity)
- SQLite (Lightweight embedded database)

## Evaluation Criteria Mapping

### Object-Oriented Programming (OOP)
Planned use of classes such as:
- `User` - Handles authentication and user data
- `Event` - Manages event information
- `Booking` - Handles ticket reservations
- `Ticket` - Individual ticket details
- `Seat` - Seat availability and selection
- `DatabaseConnection` - Singleton pattern for DB access

### JavaFX GUI
- FXML files for all views (`loginView`, `mainView`, `adminView`, `registerView`, `seatView`)
- TableView for displaying events
- Forms for data entry
- Scene Builder for layout design

### UML Diagrams
- **Use Case Diagram** - Shows actors (Customer, Admin) and system actions
- **Class Diagram** - Shows 6+ core classes with attributes, methods, and relationships
- Both diagrams included in `/diagrams` folder

### Design Pattern (Tentative)
- **Singleton Pattern** - `DatabaseConnection` class will use `getInstance()` method to ensure single database connection instance

### Multithreading (Planned)
- **JavaFX Task** - Loading events and seat availability in background threads to prevent UI freezing

### GitHub Repository
- Proper project structure with `src/`, `diagrams/`, `README.md`, `.gitignore`
- All group members added as collaborators

## Project Structure
CSIT228-Capstone/
├── src/
│   └── main/
│       ├── java/com/group8/csit228capstone/
│       │   ├── MainApplication.java
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── MainController.java
│       │   ├── SeatSelectionController.java
│       │   ├── BookingHistoryController.java
│       │   ├── AdminController.java
│       │   ├── User.java
│       │   ├── Event.java
│       │   ├── Booking.java
│       │   ├── AdminBooking.java
│       │   └── database/DatabaseConnection.java
│       └── resources/com/group8/csit228capstone/
│           ├── login-view.fxml
│           ├── register-view.fxml
│           ├── main-view.fxml
│           ├── seat-view.fxml
│           ├── BookingHistoryView.fxml
│           ├── admin-view.fxml
│           └── LoadingView.fxml
├── diagrams/
│   ├── use-case-diagram.png
│   └── class-diagram.png
├── eventticketing.db
├── README.md
└── .gitignore
