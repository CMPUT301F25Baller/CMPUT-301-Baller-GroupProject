package com.example.ballerevents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory store holding a fixed list of {@link NotificationLog} entries.
 * <p>
 * This class is used for prototype and testing purposes to simulate backend-
 * provided notification logs. All data is stored statically, and methods return
 * copies to prevent external mutation.
 * </p>
 *
 * <p>No persistence or Firestore integration is used here. This store
 * is intended purely for sample UI behavior in the notification log screens.</p>
 */
public final class NotificationLogsStore {

    /** Internal static storage for all mock notification logs. */
    private static final List<NotificationLog> STORE = new ArrayList<>();

    // Seed with mock data removed. The list starts empty.
    static {
        // STORE.add(new NotificationLog(...));
    }

    /** Private constructor to enforce non-instantiability. */
    private NotificationLogsStore() {}

    /**
     * Returns a defensive copy of all notification logs.
     * <p>
     * This prevents callers from mutating the internal static list, enabling
     * safe UI diffing and stable state management.
     * </p>
     *
     * @return A new {@link List} containing all {@link NotificationLog} entries.
     */
    public static List<NotificationLog> getAll() {
        return new ArrayList<>(STORE);
    }

    /**
     * Marks a single notification as read by matching its unique identifier.
     *
     * @param id The ID of the notification to update.
     */
    public static void markRead(String id) {
        for (NotificationLog n : STORE) {
            if (n.id.equals(id)) {
                n.isRead = true;
                break;
            }
        }
    }

    /**
     * Marks all notifications in the store as read.
     */
    public static void markAllRead() {
        for (NotificationLog n : STORE) {
            n.isRead = true;
        }
    }
}