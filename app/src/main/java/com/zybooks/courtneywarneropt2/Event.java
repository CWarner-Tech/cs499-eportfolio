package com.zybooks.courtneywarneropt2;

// Represents an event with ID, user association, name, date, and time
public class Event {
    private final int id;
    private int userId;
    private String name;
    private String date;
    private String time;

    // Constructor to initialize event details
    public Event(int id, int userId, String name, String date, String time) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    // Getter for event ID
    public int getId() {
        return id;
    }

    // Getter for associated user ID
    public int getUserId() {
        return userId;
    }

    // Getter for event name
    public String getName() {
        return name;
    }

    // Getter for event date
    public String getDate() {
        return date;
    }

    // Getter for event time
    public String getTime() {
        return time;
    }
}
