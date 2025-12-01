package com.example.ballerevents;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import androidx.annotation.Keep;

/**
 * Model representing a single notification that was sent to an Entrant by an Organizer.
 * <p>
 * This class is Firestore-friendly and maps common fields we expect from
 * a notification document living under users/{recipientId}/notifications/{id}.
 * We keep the fields permissive so older documents still bind safely.
 * </p>
 */
@Keep
public class NotificationLog {

    @DocumentId
    private String id;

    // Who sent the notification (organizer)
    private String organizerId;
    private String organizerName;

    // Who received the notification (entrant)
    private String recipientId;
    private String recipientName;

    // Optional event context
    private String eventId;
    private String eventTitle;

    // Content
    private String title;
    private String message;
    private String type; // e.g., INVITE, UPDATE, REMINDER

    // State
    private boolean read;

    // When it was created/sent
    private Timestamp timestamp;

    public NotificationLog() { /* required for Firestore */ }

    public NotificationLog(String id, String title, String message, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }
    private Boolean adminReviewed;   // <— NEW admin audit flag

    public Boolean getAdminReviewed() { return adminReviewed; }


    public String getId() { return id; }
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerName() { return organizerName; }
    public String getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return read; }

    @PropertyName("timestamp")
    public Timestamp getTimestamp() { return timestamp; }

    // Defensive defaults for missing legacy fields
    public String safeTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (type != null && !type.isEmpty()) return type;
        return "Notification";
    }

    public String safeMessage() {
        if (message != null && !message.isEmpty()) return message;
        if (eventTitle != null && !eventTitle.isEmpty()) return "Related to " + eventTitle;
        return "";
    }
}
