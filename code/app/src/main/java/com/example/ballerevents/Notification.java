package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Firestore model for a user notification.
 * Stored in: users/{userId}/notifications/{notificationId}
 */
public class Notification {

    @DocumentId
    private String id;
    private String title;
    private String message;
    private String eventId; // Required to link back to the event
    private String type;    // e.g., "invitation", "general"
    private boolean isRead;

    @ServerTimestamp
    private Date timestamp;

    public Notification() {} // Required for Firestore

    public Notification(String title, String message, String eventId, String type) {
        this.title = title;
        this.message = message;
        this.eventId = eventId;
        this.type = type;
        this.isRead = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getEventId() { return eventId; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public Date getTimestamp() { return timestamp; }

    public void setRead(boolean read) { isRead = read; }
}