package com.example.ballerevents;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/** Firestore-backed notification log sent by organizers to entrants. */
public class NotificationLog {
    public String id;            // Firestore doc id (filled after fetch)
    public String type;          // e.g., WAITLIST_ADDED, INVITE_SENT, EVENT_UPDATED
    public String message;       // human-readable summary

    public String eventId;
    public String eventTitle;

    public String senderId;      // organizer uid
    public String senderName;

    public String recipientId;   // entrant uid
    public String recipientName;

    public boolean read;         // simple global read flag for admin list

    @ServerTimestamp public Date timestamp; // server time

    public NotificationLog() {}  // required for Firestore
}
