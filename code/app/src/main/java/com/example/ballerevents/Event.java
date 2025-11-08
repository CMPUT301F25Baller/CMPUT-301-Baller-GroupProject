package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;
import java.util.List;

/**
 * Represents a single event document from the Firestore "events" collection.
 * This class is a "POJO" (Plain Old Java Object) used by Firestore for
 * serializing and deserializing data. It also implements Parcelable to be
 * passed between Android activities via Intents, although only the ID is
 * typically passed.
 */
public class Event implements Parcelable {
    /**
     * The unique Firestore document ID.
     * This field is automatically populated by Firestore when
     * the object is deserialized.
     */
    @DocumentId
    private String id; // Changed to String and added @DocumentId

    // Fields are public for direct serialization by Firestore.
    /** The main title of the event. */
    public String title;
    /** The date of the event (e.g., "November 15 2023"). */
    public String date;
    /** The time of the event (e.g., "8:00PM - 11:00PM"). */
    public String time;
    /** The name of the venue (e.g., "Gala Convention Center"). */
    public String locationName;
    /** The full address of the venue. */
    public String locationAddress;
    /** The price of the event (e.g., "$25" or "Free"). */
    public String price;
    /** A list of tags for filtering (e.g., "Music", "Pop"). */
    public List<String> tags;
    /** The display name of the event organizer. */
    public String organizer;
    /** The Firestore document ID of the organizer (links to the "users" collection). */
    public String organizerId;
    /** A detailed description of the event. */
    public String description;
    /** A URL (e.g., in Firebase Storage) for the event's main poster. */
    public String eventPosterUrl;
    /** A URL for the organizer's profile picture. */
    public String organizerIconUrl;
    /** A boolean flag to determine if the event appears in the "Trending" list. */
    public boolean isTrending;

    /**
     * Empty constructor required for Firestore deserialization.
     * Do not use this directly.
     */
    // --- Empty constructor REQUIRED for Firestore ---
    public Event() {}

    // --- Parcelable Implementation ---
    // (This is only needed if you pass the *entire* object between activities)
    // (We pass the ID, but good to keep this)

    /**
     * Constructor for recreating an Event from a Parcel.
     * @param in The Parcel to read event data from.
     */
    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        date = in.readString();
        time = in.readString();
        locationName = in.readString();
        locationAddress = in.readString();
        price = in.readString();
        tags = in.createStringArrayList();
        organizer = in.readString();
        organizerId = in.readString();
        description = in.readString();
        eventPosterUrl = in.readString();
        organizerIconUrl = in.readString();
        isTrending = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(price);
        dest.writeStringList(tags);
        dest.writeString(organizer);
        dest.writeString(organizerId);
        dest.writeString(description);
        dest.writeString(eventPosterUrl);
        dest.writeString(organizerIconUrl);
        dest.writeByte((byte) (isTrending ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    // --- Getters ---
    // (Firestore uses public fields or getters)
    /** @return The event's unique Firestore document ID. */
    public String getId() { return id; }
    /** @return The main title of the event. */
    public String getTitle() { return title; }
    /** @return The date of the event (e.g., "November 15 2023"). */
    public String getDate() { return date; }
    /** @return The time of the event (e.g., "8:00PM - 11:00PM"). */
    public String getTime() { return time; }
    /** @return The name of the venue (e.g., "Gala Convention Center"). */
    public String getLocationName() { return locationName; }
    /** @return The full address of the venue. */
    public String getLocationAddress() { return locationAddress; }
    /** @return The price of the event (e.g., "$25" or "Free"). */
    public String getPrice() { return price; }
    /** @return A list of tags for filtering (e.g., "Music", "Pop"). */
    public List<String> getTags() { return tags; }
    /** @return The display name of the event organizer. */
    public String getOrganizer() { return organizer; }
    /** @return The Firestore document ID of the organizer (links to the "users" collection). */
    public String getOrganizerId() { return organizerId; }
    /** @return A detailed description of the event. */
    public String getDescription() { return description; }
    /** @return A URL for the event's main poster. */
    public String getEventPosterUrl() { return eventPosterUrl; }
    /** @return A URL for the organizer's profile picture. */
    public String getOrganizerIconUrl() { return organizerIconUrl; }
    public void setId(String id) { this.id = id; }
    /** @return True if the event is a "Trending" event, false otherwise. */
    public boolean isTrending() { return isTrending; }
}