package com.example.ballerevents;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FirestoreEventRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ListCallback<T> {
        void onSuccess(List<T> data);

        void onError(Exception e);
    }

    public interface ItemCallback<T> {
        void onSuccess(@Nullable T item);

        void onError(Exception e);
    }
    /**
     * Simple callback for operations that don't return data.
     */
    public interface VoidCallback {
        void onSuccess();
        void onError(Exception e);
    }


    private static List<Event> mapToEvents(QuerySnapshot snap) {
        List<Event> out = new ArrayList<>();
        if (snap == null) return out;
        for (DocumentSnapshot d : snap.getDocuments()) {
            Event e = d.toObject(Event.class);
            if (e != null) {
                // Ensure the Firestore document id is set on the model
                e.setId(d.getId());
                out.add(e);
            }
        }
        return out;
    }

    /**
     * "Trending" events (prototype): either filter by flag or just order by title
     */
    public void fetchTrending(ListCallback<Event> cb) {
        db.collection("events")
                // Uncomment next line if you set isTrending on create:
                //.whereEqualTo("isTrending", true)
                .orderBy("title", Query.Direction.ASCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Prototype "nearby" – same as trending for now (later: add geo/city)
     */
    public void fetchNearYou(ListCallback<Event> cb) {
        db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Real-time feed (optional; useful for home to auto-refresh)
     */
    public ListenerRegistration listenAll(ListCallback<Event> cb) {
        return db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        cb.onError(e);
                        return;
                    }
                    cb.onSuccess(mapToEvents(snap));
                });
    }

    /**
     * Fetch all events created by a specific organizer (optional)
     */
    public void fetchByOrganizer(String organizerId, ListCallback<Event> cb) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    public void fetchById(String id, ItemCallback<Event> cb) {
        db.collection("events").document(id)
                .get()
                .addOnSuccessListener(d -> {
                    Event e = d.toObject(Event.class);
                    if (e != null) e.setId(d.getId());
                    cb.onSuccess(e);
                })
                .addOnFailureListener(cb::onError);
    }

    public void create(Event e, ItemCallback<String> cb) {
        db.collection("events").add(e)
                .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                .addOnFailureListener(cb::onError);
    }

    public void create(Event e, ItemCallback<String> cb) {
        db.collection("events").add(e)
                .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Sends notifications to all "chosen" entrants of an event who have not yet been notified.
     *
     * events/{eventId}/entrants/{entrantId}
     *   - status          : "chosen" | "invited" | ...
     *   - winnerNotified  : boolean
     *   - userId          : String (id of the user profile / auth user)
     *
     * users/{userId}/notifications/{notificationId}
     *   - eventId, title, message, timestamp, read
     */
    public void sendWinnerNotifications(String eventId, VoidCallback cb) {
        CollectionReference entrantsRef = db.collection("events")
                .document(eventId)
                .collection("entrants");

        // Find all chosen entrants who haven't been notified yet
        entrantsRef
                .whereEqualTo("status", "chosen")          // change if your field/value is different
                .whereEqualTo("winnerNotified", false)     // make sure this field exists on entrants
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        if (cb != null) cb.onSuccess();
                        return;
                    }

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot snap : querySnapshot.getDocuments()) {
                        String userId = snap.getString("userId"); // field linking entrant -> user
                        if (userId == null) continue;

                        // 1) Create a notification for this user
                        DocumentReference notifRef = db.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .document(); // auto ID

                        java.util.Map<String, Object> notif = new java.util.HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("title", "You won the lottery!");
                        notif.put("message",
                                "You have been selected for this event. Open the app to confirm your spot.");
                        notif.put("timestamp", FieldValue.serverTimestamp());
                        notif.put("read", false);

                        batch.set(notifRef, notif);

                        // 2) Mark entrant as notified (and invited)
                        DocumentReference entrantRef = snap.getReference();
                        batch.update(entrantRef,
                                "winnerNotified", true,
                                "status", "invited");   // change if you use another status label
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                if (cb != null) cb.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                if (cb != null) cb.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (cb != null) cb.onError(e);
                });
    }
}
