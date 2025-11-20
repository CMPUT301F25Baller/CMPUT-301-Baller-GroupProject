package com.example.ballerevents;

/**
 * Simple model representing a notification log entry, typically used for
 * displaying past or archived notifications in a list.
 * <p>
 * Each entry includes an identifier, a title or message label, timestamp,
 * avatar resource, and read/unread state. This model is UI-focused and does
 * not integrate directly with Firestore.
 * </p>
 */
public class NotificationLog {

    /** Unique identifier for the log entry. */
    public final String id;

    /** Short title or label describing the notification. */
    public final String title;

    /** Display-friendly timestamp string (e.g., "Just now", "1h ago"). */
    public final String timestamp;

    /** Drawable resource ID for the avatar icon associated with the notification. */
    public final int avatarRes;

    /** Whether this log entry has been marked as read. */
    public boolean isRead;

    /**
     * Constructs a new {@link NotificationLog} entry.
     *
     * @param id         Unique identifier for the log entry
     * @param title      Title or summary of the notification
     * @param timestamp  Human-readable timestamp label
     * @param avatarRes  Drawable resource ID for the avatar icon
     * @param isRead     Whether the entry is marked as read
     */
    public NotificationLog(String id,
                           String title,
                           String timestamp,
                           int avatarRes,
                           boolean isRead) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.avatarRes = avatarRes;
        this.isRead = isRead;
    }
}
