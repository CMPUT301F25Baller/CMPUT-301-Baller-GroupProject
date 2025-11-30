package com.example.ballerevents;

import androidx.annotation.Nullable;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public final class NotificationLogsStore {
    private static final String COL = "notification_logs";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LogsListener {
        void onChanged(List<NotificationLog> logs, @Nullable DocumentSnapshot nextCursor);
        void onError(Exception e);
    }

    /** Live listener for logs (All or only unread), ordered by timestamp desc, with optional pagination. */
    public ListenerRegistration watchAll(boolean onlyUnread,
                                         @Nullable DocumentSnapshot startAfter,
                                         int pageSize,
                                         LogsListener listener) {
        Query q = db.collection(COL);
        if (onlyUnread) q = q.whereEqualTo("read", false);
        q = q.orderBy("timestamp", Query.Direction.DESCENDING).limit(pageSize);
        if (startAfter != null) q = q.startAfter(startAfter);

        return q.addSnapshotListener((snap, err) -> {
            if (err != null) { listener.onError(err); return; }
            if (snap == null) { listener.onChanged(new ArrayList<>(), null); return; }

            List<NotificationLog> out = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                NotificationLog nl = d.toObject(NotificationLog.class);
                if (nl != null) nl.id = d.getId();
                out.add(nl);
            }
            DocumentSnapshot cursor = snap.getDocuments().isEmpty()
                    ? null : snap.getDocuments().get(snap.size() - 1);
            listener.onChanged(out, cursor);
        });
    }

    /** Marks ALL unread logs as read (single batch). */
    public Task<Void> markAllAsRead() {
        return db.collection(COL).whereEqualTo("read", false).get().continueWithTask(task -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot d : task.getResult().getDocuments()) {
                batch.update(d.getReference(), "read", true);
            }
            return batch.commit();
        });
    }

    /** Append a new log document. */
    public Task<DocumentReference> append(NotificationLog log) {
        return db.collection(COL).add(log);
    }
}
