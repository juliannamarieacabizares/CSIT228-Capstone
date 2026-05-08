package com.group8.csit228capstone;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Booking {

    private final SimpleIntegerProperty bookingId;
    private final SimpleStringProperty eventTitle;
    private final SimpleStringProperty eventDate;
    private final SimpleStringProperty seatNumber;
    private final SimpleStringProperty bookingDate;

    public Booking(int bookingId, String eventTitle, String eventDate, String seatNumber, String bookingDate) {
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.eventTitle = new SimpleStringProperty(eventTitle);
        this.eventDate = new SimpleStringProperty(eventDate);
        this.seatNumber = new SimpleStringProperty(seatNumber);
        this.bookingDate = new SimpleStringProperty(bookingDate);
    }

    public int getBookingId() { return bookingId.get(); }
    public String getEventTitle() { return eventTitle.get(); }
    public String getEventDate() { return eventDate.get(); }
    public String getSeatNumber() { return seatNumber.get(); }
    public String getBookingDate() { return bookingDate.get(); }

    public SimpleIntegerProperty bookingIdProperty() { return bookingId; }
    public SimpleStringProperty eventTitleProperty() { return eventTitle; }
    public SimpleStringProperty eventDateProperty() { return eventDate; }
    public SimpleStringProperty seatNumberProperty() { return seatNumber; }
    public SimpleStringProperty bookingDateProperty() { return bookingDate; }
}