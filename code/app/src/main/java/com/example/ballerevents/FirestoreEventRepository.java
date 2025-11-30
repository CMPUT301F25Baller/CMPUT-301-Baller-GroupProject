package com.example.ballerevents;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class that provides Firestore-based data operations for {@link Event}
 * objects. This class handles common queries such as trending events, organizer-filtered
 * events, real-time listeners, and fetching individual event documents.
 *
 * <p>All methods use callbacks for asynchronous operations and return
 * {@link ListenerRegistration} where applicable for real-time updates.</p>
 */
public class FirestoreEventRepository {

    /** Reference to the Firestore database instance. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Generic callback for returning lists of objects asynchronously.
     *
     * @param <T> The data type returned in the list.
     */
    public interface ListCallback<T> {
        /** Called when the operation succeeds. */
        void onSuccess(List<T> data);

        /** Called when an error occurs. */
        void onError(Exception e);
    }

    /**
     * Callback for returning a single object asynchronously.
     *
     * @param <T> The object type returned.
     */
    public interface ItemCallback<T> {
        /** Called when the operation succeeds. Null indicates item not found. */
        void onSuccess(@Nullable T item);

        /** Called when an error occurs. */
        void onError(Exception e);
    }
    /**
     * Simple callback for operations that don't return data.
     */
    public interface VoidCallback {
        void onSuccess();
        void onError(Exception e);
    }


    /**
     * Converts a {@link QuerySnapshot} to a list of {@link Event} objects and ensures
     * each event has its Firestore document ID set.
     *
     * @param snap The QuerySnapshot returned by Firestore.
     * @return List of {@link Event} models, or an empty list if {@code snap} is null.
     */
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
     * Fetches a limited list of "trending" events.
     * <p>
     * Prototype: currently orders events alphabetically. Later this may use
     * {@code isTrending == true} or other ranking logic.
     * </p>
     *
     * @param cb Callback returning a list of {@link Event} items.
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
     * Fetches events considered "nearby."
     * <p>
     * Prototype: currently the same as trending. Location-based filtering may be added later.
     * </p>
     *
     * @param cb Callback returning a list of {@link Event} items.
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
     * Registers a real-time listener that triggers whenever the "events" collection changes.
     *
     * @param cb Callback invoked whenever Firestore updates the snapshot.
     * @return A {@link ListenerRegistration} that can be used to remove the listener.
     */
    public ListenerRegistration listenAll(ListCallback<Event> cb) {
        return db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { cb.onError(e); return; }
                    cb.onSuccess(mapToEvents(snap));
                });
    }

    /**
     * Fetches all events created by a specific organizer.
     *
     * @param organizerId Firestore ID of the organizer.
     * @param cb          Callback returning a filtered list of events.
     */
    public void fetchByOrganizer(String organizerId, ListCallback<Event> cb) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Fetches a single event by its Firestore document ID.
     *
     * @param id Firestore event document ID.
     * @param cb Callback returning the event, or null if not found.
     */
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

    /**
     * Creates a new event document in Firestore.
     *
     * @param e  The event model to create.
     * @param cb Callback returning the newly created document ID.
     */
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

