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

    /** Simple callback for operations that don't return data. */
    public interface VoidCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /** Helper: convert QuerySnapshot -> List<Event> and set document id. */
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

    /** "Trending" events (prototype). */
    public void fetchTrending(ListCallback<Event> cb) {
        db.collection("events")
                //.whereEqualTo("isTrending", true)
                .orderBy("title", Query.Direction.ASCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /** "Near you" prototype – same as trending for now. */
    public void fetchNearYou(ListCallback<Event> cb) {
        db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /** Real-time listener for all events. */
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

    /** Events for one organizer. */
    public void fetchByOrganizer(String organizerId, ListCallback<Event> cb) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(mapToEvents(snap)))
                .addOnFailureListener(cb::onError);
    }

    /** Single event by id. */
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

    /** Create a new event document. */
    public void create(Event e, ItemCallback<String> cb) {
        db.collection("events").add(e)
                .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                .addOnFailureListener(cb::onError);
    }

    /**
     * US29: Send notifications to all "chosen" entrants of an event who have not yet been notified.
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

                        // 1) Notification for this user
                        DocumentReference notifRef = db.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .document();

                        java.util.Map<String, Object> notif = new java.util.HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("title", "You won the lottery!");
                        notif.put("message",
                                "You have been selected for this event. Open the app to confirm your spot.");
                        notif.put("timestamp", FieldValue.serverTimestamp());
                        notif.put("read", false);

                        batch.set(notifRef, notif);

                        // 2) Mark entrant as invited + notified
                        DocumentReference entrantRef = snap.getReference();
                        batch.update(entrantRef,
                                "winnerNotified", true,
                                "status", "invited");
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

    /**
     * US30: Randomly sample a specified number of waiting entrants for an event
     * and mark them as "chosen".
     */
    public void sampleAttendees(String eventId, int sampleSize, VoidCallback cb) {
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

    /**
     * US31: Cancel entrants who were invited but did not sign up / confirm.
     */
    public void cancelUnresponsiveEntrants(String eventId, VoidCallback cb) {
        CollectionReference entrantsRef = db.collection("events")
                .document(eventId)
                .collection("entrants");

        entrantsRef
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    if (docs.isEmpty()) {
                        if (cb != null) cb.onSuccess();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot snap : docs) {
                        DocumentReference entrantRef = snap.getReference();
                        batch.update(entrantRef, "status", "cancelled");
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
