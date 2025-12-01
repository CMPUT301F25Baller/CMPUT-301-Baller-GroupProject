package com.example.ballerevents;

/**
 * Model representing a single notification entry displayed in the UI.
 * <p>
 * This class contains lightweight metadata for rendering notifications in
 * RecyclerView rows, including sender information, message content, timestamp,
 * avatar resource, and whether action buttons should be shown.
 * </p>
 *
 * <p>Fields are public and final where appropriate, as this is a simple model
 * without Firestore integration.</p>
 */
public class NotificationItem {

    /** Unique identifier for the notification. */
    public final String id;

    /** Drawable resource ID for the avatar icon. */
    public final int avatarRes;

    /** Name or label representing the sender. */
    public final String sender;

    /** Main notification message text. */
    public final String message;

    /** User-friendly time label (e.g., "Just now", "2h ago"). */
    public final String timeLabel;

    /** Whether this notification should display action buttons ("Mark read", "Open"). */
    public final boolean hasActions;

    /** Whether the notification has been marked as read by the user. */
    public boolean isRead;

    /** If true, this notification represents an event invitation (Accept/Reject). */
    public final boolean isInvitation;

    /** The ID of the event associated with this notification (if applicable). */
    public final String eventId;

    /**
     * Constructs a new {@link NotificationItem}.
     *
     * @param id         Unique notification identifier
     * @param avatarRes  Drawable resource ID for avatar
     * @param sender     Sender name or label
     * @param message    Notification body text
     * @param timeLabel  Display-formatted time label
     * @param hasActions Whether to display action buttons
     * @param isRead     Whether the notification is already marked as read
     */
    public NotificationItem(String id,
                            int avatarRes,
                            String sender,
                            String message,
                            String timeLabel,
                            boolean hasActions,
                            boolean isRead,
                            boolean isInvitation,
                            String eventId) {
        this.id = id;
        this.avatarRes = avatarRes;
        this.sender = sender;
        this.message = message;
        this.timeLabel = timeLabel;
        this.hasActions = hasActions;
        this.isRead = isRead;
        this.isInvitation = isInvitation;
        this.eventId = eventId;
    }
}
