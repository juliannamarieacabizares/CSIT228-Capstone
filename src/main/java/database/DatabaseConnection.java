package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;


    private DatabaseConnection() {
        try {
            connect();
            createTables();
            upgradeSchema();
            insertSampleData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void connect() throws SQLException {
        String url = "jdbc:sqlite:eventticketing.db";
        connection = DriverManager.getConnection(url);
        enableForeignKeys();
        System.out.println("Database connected successfully!");
    }

    private void enableForeignKeys() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            System.out.println("Unable to enable foreign keys: " + e.getMessage());
        }
    }

    private void upgradeSchema() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE bookings ADD COLUMN paymentStatus TEXT DEFAULT 'Paid'");
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) {
                System.out.println("Schema upgrade warning: " + e.getMessage());
            }
        }
    }

    private void createTables() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                userId INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL CHECK (role IN ('customer', 'admin'))
            )
        """;

        String createEvents = """
            CREATE TABLE IF NOT EXISTS events (
                eventId INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                date TEXT NOT NULL,
                location TEXT NOT NULL,
                totalSeats INTEGER NOT NULL
            )
        """;

        String createSeats = """
            CREATE TABLE IF NOT EXISTS seats (
                seatId INTEGER PRIMARY KEY AUTOINCREMENT,
                eventId INTEGER NOT NULL,
                seatNumber TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'available',
                FOREIGN KEY (eventId) REFERENCES events(eventId) ON DELETE CASCADE
            )
        """;

        String createBookings = """
            CREATE TABLE IF NOT EXISTS bookings (
                bookingId INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                eventId INTEGER NOT NULL,
                bookingDate TEXT NOT NULL,
                paymentStatus TEXT NOT NULL DEFAULT 'Paid',
                FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                FOREIGN KEY (eventId) REFERENCES events(eventId) ON DELETE CASCADE
            )
        """;

        String createTickets = """
            CREATE TABLE IF NOT EXISTS tickets (
                ticketId INTEGER PRIMARY KEY AUTOINCREMENT,
                bookingId INTEGER NOT NULL,
                seatNumber TEXT NOT NULL,
                FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createEvents);
            stmt.execute(createSeats);
            stmt.execute(createBookings);
            stmt.execute(createTickets);
            System.out.println("All 5 tables created successfully!");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    private void insertSampleData() {
        String insertAdmin = """
            INSERT OR IGNORE INTO users (name, email, password, role) 
            VALUES ('Admin User', 'admin@event.com', 'admin123', 'admin')
        """;

        String insertCustomer = """
            INSERT OR IGNORE INTO users (name, email, password, role) 
            VALUES ('John Doe', 'john@example.com', 'password123', 'customer')
        """;

        String insertEvents = """
            INSERT OR IGNORE INTO events (title, description, date, location, totalSeats) 
            VALUES 
            ('Summer Music Fest', 'Annual summer concert', '2025-06-15', 'City Park', 50),
            ('Tech Conference 2025', 'Latest in technology', '2025-07-20', 'Convention Center', 100),
            ('Food Festival', 'Taste from around the world', '2025-08-10', 'Downtown Plaza', 75)
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertAdmin);
            stmt.execute(insertCustomer);
            stmt.execute(insertEvents);
            System.out.println("Sample data inserted successfully!");
        } catch (SQLException e) {
            System.out.println("Error inserting sample data: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database disconnected!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
