package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Firestore model for a user notification.
 * <p>
 * This class maps directly to documents stored in:
 * <code>users/{userId}/notifications/{notificationId}</code>
 * </p>
 */
public class Notification {

    @DocumentId
    private String id;
    private String title;
    private String message;
    private String eventId;
    private String type;
    private String senderId;
    private boolean isRead;

    @ServerTimestamp
    private Date timestamp;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Notification() {}

    /**
     * Constructs a standard notification object.
     *
     * @param title   The notification title.
     * @param message The body text of the notification.
     * @param eventId The ID of the related event (optional).
     * @param type    The type of notification (e.g., "invitation", "general").
     */
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

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public boolean isRead() { return isRead; }
    public Date getTimestamp() { return timestamp; }

    public void setRead(boolean read) { isRead = read; }
}