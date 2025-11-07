package com.example.ballerevents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NotificationLogsStore {
    private static final List<NotificationLog> STORE = new ArrayList<>();

    static {
        // seed mock data
        STORE.add(new NotificationLog("n1",
                "David Silbia Invite to Jo Malone London’s Mother’s...",
                "Just now", R.drawable.placeholder_avatar1, false));
        STORE.add(new NotificationLog("n2",
                "Joan Baker\nInvite to A virtual Evening of Smooth Jazz",
                "20 min ago", R.drawable.placeholder_avatar2, false));
        STORE.add(new NotificationLog("n3",
                "Adnan Safi added you to waitlist",
                "5 min ago", R.drawable.placeholder_avatar3, true));
        STORE.add(new NotificationLog("n4",
                "Ronald C. Kinch added you to waitlist",
                "1 hr ago", R.drawable.placeholder_avatar2, false));
        STORE.add(new NotificationLog("n5",
                "Clara Tolson\ninvited you to Event Gala Music Festival",
                "9 hr ago", R.drawable.placeholder_avatar1, true));
        STORE.add(new NotificationLog("n6",
                "Eric G. Prickett\nsent an invitation",
                "Wed, 3:30 pm", R.drawable.placeholder_avatar2, false));
    }

    private NotificationLogsStore() {}

    /** Return a copy so UI can diff without mutating source */
    public static List<NotificationLog> getAll() {
        return new ArrayList<>(STORE);
    }

    public static void markRead(String id) {
        for (NotificationLog n : STORE) {
            if (n.id.equals(id)) { n.isRead = true; break; }
        }
    }

    public static void markAllRead() {
        for (NotificationLog n : STORE) n.isRead = true;
    }
}
