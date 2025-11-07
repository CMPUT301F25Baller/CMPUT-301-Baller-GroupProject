package com.example.ballerevents;

public class NotificationLog {
    public final String id;
    public final String title;
    public final String timestamp; // e.g., "Just now"
    public final int avatarRes;
    public boolean isRead;

    public NotificationLog(String id, String title, String timestamp, int avatarRes, boolean isRead) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.avatarRes = avatarRes;
        this.isRead = isRead;
    }
}
