package com.example.ballerevents;

public class NotificationItem {
    public final String id;
    public final int avatarRes;
    public final String sender;
    public final String message;
    public final String timeLabel;
    public final boolean hasActions; // show "Mark as read" + "Open"
    public boolean isRead;

    public NotificationItem(String id, int avatarRes, String sender, String message,
                            String timeLabel, boolean hasActions, boolean isRead) {
        this.id = id;
        this.avatarRes = avatarRes;
        this.sender = sender;
        this.message = message;
        this.timeLabel = timeLabel;
        this.hasActions = hasActions;
        this.isRead = isRead;
    }
}
