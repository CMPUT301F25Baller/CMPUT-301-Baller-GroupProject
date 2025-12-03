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
 * not integrate directly with Firestore real-time updates in the same way the
 * core {@link Notification} model does.
 * </p>
 */
@Keep
public class NotificationLog {

    // ---------------------------------------------------------
    // Firestore-backed fields
    // ---------------------------------------------------------
    @DocumentId
    private String id;

    private String organizerId;
    private String organizerName;

    private String recipientId;
    private String recipientName;

    private String eventId;
    private String eventTitle;

    private String title;
    private String message;
    private String type;
    private boolean read;

    private Timestamp timestamp;

    private Boolean adminReviewed;

    // ---------------------------------------------------------
    // UI-only fields
    // ---------------------------------------------------------

    /** Human-friendly timestamp label such as "1h ago". */
    private String timestampLabel;

    /** Avatar icon resource ID for list display. */
    private int avatarRes = 0;

    /** Whether this notification represents an event invitation. */
    private boolean isInvitation = false;


    // ---------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public NotificationLog() {
    }

    /**
     * Simple constructor for basic Firestore data.
     *
     * @param id        The unique document ID.
     * @param title     The notification title.
     * @param message   The notification body text.
     * @param timestamp The time the notification was created.
     */
    public NotificationLog(String id, String title, String message, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Constructor for UI display without specific invitation logic.
     *
     * @param id             The unique document ID.
     * @param title          The notification title.
     * @param timestampLabel Formatted time string (e.g., "2h ago").
     * @param avatarRes      Resource ID for the avatar icon.
     * @param isRead         Whether the notification has been read.
     */
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

    /**
     * Full constructor for UI display, including invitation details.
     *
     * @param id             The unique document ID.
     * @param title          The notification title.
     * @param timestampLabel Formatted time string.
     * @param avatarRes      Resource ID for the avatar icon.
     * @param isRead         Whether the notification has been read.
     * @param isInvitation   True if this is an event invitation.
     * @param eventId        The ID of the associated event.
     */
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
    // Getters
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
    // Helper fallback methods
    // ---------------------------------------------------------

    /**
     * Returns a safe title string to display.
     * Falls back to the notification type or a default string if the title is missing.
     *
     * @return A displayable title string.
     */
    public String safeTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (type != null && !type.isEmpty()) return type;
        return "Notification";
    }

    /**
     * Returns a safe message string to display.
     * Falls back to the event title if the message body is missing.
     *
     * @return A displayable message string.
     */
    public String safeMessage() {
        if (message != null && !message.isEmpty()) return message;
        if (eventTitle != null && !eventTitle.isEmpty()) return "Related to " + eventTitle;
        return "";
    }
}