package com.group8.csit228capstone;

public class Event {
    private final int eventId;
    private final String title;
    private final String description;
    private final String date;
    private final String location;
    private final int availableSeats;

    public Event(int eventId, String title, String description, String date, String location, int availableSeats) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.availableSeats = availableSeats;
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public int getAvailableSeats() { return availableSeats; }
}