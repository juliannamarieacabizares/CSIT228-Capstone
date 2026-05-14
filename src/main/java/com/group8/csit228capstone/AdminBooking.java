package com.group8.csit228capstone;

import javafx.beans.property.SimpleStringProperty;

public class AdminBooking {
    private final SimpleStringProperty customerName;
    private final SimpleStringProperty eventTitle;
    private final SimpleStringProperty seatNumber;
    private final SimpleStringProperty bookingDate;
    private final SimpleStringProperty paymentStatus;

    public AdminBooking(String customerName, String eventTitle, String seatNumber, String bookingDate, String paymentStatus) {
        this.customerName = new SimpleStringProperty(customerName);
        this.eventTitle = new SimpleStringProperty(eventTitle);
        this.seatNumber = new SimpleStringProperty(seatNumber);
        this.bookingDate = new SimpleStringProperty(bookingDate);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
    }

    public String getCustomerName() {
        return customerName.get();
    }

    public String getEventTitle() {
        return eventTitle.get();
    }

    public String getSeatNumber() {
        return seatNumber.get();
    }

    public String getBookingDate() {
        return bookingDate.get();
    }

    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    public SimpleStringProperty customerNameProperty() {
        return customerName;
    }

    public SimpleStringProperty eventTitleProperty() {
        return eventTitle;
    }

    public SimpleStringProperty seatNumberProperty() {
        return seatNumber;
    }

    public SimpleStringProperty bookingDateProperty() {
        return bookingDate;
    }

    public SimpleStringProperty paymentStatusProperty() {
        return paymentStatus;
    }
}
