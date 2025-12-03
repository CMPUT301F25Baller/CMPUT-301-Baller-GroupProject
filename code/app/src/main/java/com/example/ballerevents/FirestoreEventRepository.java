package com.example.ballerevents;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository class handling all Firestore operations related to Events.
 * This includes fetching events, creating events, and managing the lottery/sampling system.
 */
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

    public interface VoidCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Maps a Firestore QuerySnapshot to a list of Event objects.
     */
    private static List<Event> mapToEvents(QuerySnapshot snap) {
        List<Event> out = new ArrayList<>();
        if (snap == null) return out;
        for (DocumentSnapshot d : snap.getDocuments()) {
            Event e = d.toObject(Event.class);
            if (e != null) {
                e.setId(d.getId());
                out.add(e);
            }
        }
        return out;
    }

    /**
     * Fetches the top 3 events by Waitlist Size.
     * Note: Sorting is performed client-side as Firestore does not natively support
     * sorting by array length.
     *
     * @param cb Callback to receive the top 3 popular events.
     */
    public void fetchPopularEvents(ListCallback<Event> cb) {
        db.collection("events")
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Event> events = mapToEvents(snap);

                    Collections.sort(events, (e1, e2) ->
                            Integer.compare(e2.getWaitlistCount(), e1.getWaitlistCount())
                    );

                    List<Event> top3 = events.subList(0, Math.min(events.size(), 3));
                    cb.onSuccess(top3);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Fetches trending events. Currently redirects to {@link #fetchPopularEvents}.
     *
     * @param cb Callback to receive the list of events.
     */
    public void fetchTrending(ListCallback<Event> cb) {
        fetchPopularEvents(cb);
    }

    /**
     * Fetches events "Near You".
     *
     * @param cb Callback to receive the list of nearby events.
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
     * Registers a real-time listener for all events.
     *
     * @param cb Callback to receive real-time updates.
     * @return The ListenerRegistration object (call remove() to stop listening).
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
     * Fetches all events created by a specific organizer.
     *
     * @param organizerId The UID of the organizer.
     * @param cb Callback to receive the list of events.
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
     * Fetches a single event by its ID.
     *
     * @param id The event ID.
     * @param cb Callback to receive the Event object.
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
     * Creates a new event in Firestore.
     *
     * @param e  The Event object to create.
     * @param cb Callback to receive the new Event ID.
     */
    public void create(Event e, ItemCallback<String> cb) {
        db.collection("events").add(e)
                .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Performs the lottery sampling logic.
     * Selects a random sample of users from the waiting list and updates their status to "chosen".
     *
     * @param eventId    The ID of the event.
     * @param sampleSize The number of attendees to select.
     * @param cb         Callback for completion.
     */
    public void sampleAttendeesAndNotify(String eventId, int sampleSize, VoidCallback cb) {
        CollectionReference entrantsRef = db.collection("events")
                .document(eventId)
                .collection("entrants");

        entrantsRef
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    if (docs.isEmpty()) {
                        if (cb != null) cb.onSuccess();
                        return;
                    }

                    Collections.shuffle(docs);
                    int n = Math.min(sampleSize, docs.size());
                    List<DocumentSnapshot> chosen = docs.subList(0, n);

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot snap : chosen) {
                        DocumentReference entrantRef = snap.getReference();
                        batch.update(entrantRef,
                                "status", "chosen",
                                "winnerNotified", false);
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> sendWinnerNotifications(eventId, cb))
                            .addOnFailureListener(e -> {
                                if (cb != null) cb.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (cb != null) cb.onError(e);
                });
    }

    /**
     * Sends notifications to users who have been selected in the lottery.
     *
     * @param eventId The ID of the event.
     * @param cb      Callback for completion.
     */
    public void sendWinnerNotifications(String eventId, VoidCallback cb) {
        CollectionReference entrantsRef = db.collection("events")
                .document(eventId)
                .collection("entrants");

        entrantsRef
                .whereEqualTo("status", "chosen")
                .whereEqualTo("winnerNotified", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        if (cb != null) cb.onSuccess();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot snap : querySnapshot.getDocuments()) {
                        String userId = snap.getString("userId");
                        if (userId == null) continue;

                        DocumentReference notifRef = db.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .document();

                        java.util.Map<String, Object> notif = new java.util.HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("title", "You won the lottery!");
                        notif.put("message", "You have been selected for this event.");
                        notif.put("timestamp", FieldValue.serverTimestamp());
                        notif.put("read", false);
                        notif.put("type", "invitation");

                        batch.set(notifRef, notif);

                        DocumentReference entrantRef = snap.getReference();
                        batch.update(entrantRef, "winnerNotified", true, "status", "invited");
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> { if (cb != null) cb.onSuccess(); })
                            .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
                })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
    }
}