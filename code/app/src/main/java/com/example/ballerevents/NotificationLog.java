package com.example.ballerevents;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Simple model representing a notification log entry, typically used for
 * displaying past or archived notifications in a list.
 * <p>
 * Each entry includes an identifier, a title or message label, timestamp,
 * avatar resource, and read/unread state. This model is UI-focused and does
 * not integrate directly with Firestore.
 * </p>
 */
@Keep
public class NotificationLog {

    // ---------------------------------------------------------
    // Firestore-backed fields (Organizer → Entrant notifications)
    // ---------------------------------------------------------
    @DocumentId
    private String id;

    private String organizerId;
    private String organizerName;

    private String recipientId;
    private String recipientName;

    private String eventId;
    private String eventTitle;

    private String title;      // shared with UI version
    private String message;
    private String type;       // e.g., "INVITE", "UPDATE", etc.
    private boolean read;

    private Timestamp timestamp;

    private Boolean adminReviewed;  // NEW audit flag

    // ---------------------------------------------------------
    // UI-only fields (from main + dashboard branches)
    // ---------------------------------------------------------

    /** Human-friendly timestamp label such as "1h ago" */
    private String timestampLabel;

    /** Avatar icon for list display */
    private int avatarRes = 0;

    /** Whether this notification represents an event invitation */
    private boolean isInvitation = false;


    // ---------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------

    public NotificationLog() {
        // Required empty constructor for Firestore
    }

    /** Simple Firestore constructor */
    public NotificationLog(String id, String title, String message, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    /** UI Constructor (main branch version) */
    public NotificationLog(String id,
                           String title,
                           String timestampLabel,
                           int avatarRes,
                           boolean isRead) {
        this.id = id;
        this.title = title;
        this.timestampLabel = timestampLabel;
        this.avatarRes = avatarRes;
        this.read = isRead;
    }

    /** Full UI + invitation constructor */
    public NotificationLog(String id,
                           String title,
                           String timestampLabel,
                           int avatarRes,
                           boolean isRead,
                           boolean isInvitation,
                           String eventId) {
        this(id, title, timestampLabel, avatarRes, isRead);
        this.isInvitation = isInvitation;
        this.eventId = eventId;
    }


    // ---------------------------------------------------------
    // Getters (FULL)
    // ---------------------------------------------------------

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

    public Boolean getAdminReviewed() { return adminReviewed; }

    // UI fields
    public String getTimestampLabel() { return timestampLabel; }
    public int getAvatarRes() { return avatarRes; }
    public boolean isInvitation() { return isInvitation; }


    // ---------------------------------------------------------
    // Helper fallback methods (for older documents)
    // ---------------------------------------------------------

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
