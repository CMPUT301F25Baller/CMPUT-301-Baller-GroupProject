package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single event document from the Firestore {@code "events"} collection.
 */
public class Event implements Parcelable {

    @DocumentId
    private String id;

    // -------------------------------------------------------------------------
    // Public fields used directly by Firestore during serialization.
    // -------------------------------------------------------------------------

    /** The main title of the event. */
    public String title;

    /** The date of the event (e.g., "November 15 2023"). */
    public String date;

    /** The scheduled time range of the event (e.g., "8:00PM - 11:00PM"). */
    public String time;

    /** The display name of the event venue. */
    public String locationName;

    /** The full street address of the venue. */
    public String locationAddress;

    /** The ticket price or entry cost for the event. */
    public String price;

    /** A list of classification tags for filtering and search. */
    public List<String> tags;

    /** The display name of the organizer hosting the event. */
    public String organizer;

    /** Firestore document ID of the organizer (from the {@code users} collection). */
    public String organizerId;

    /** A descriptive summary or detailed information about the event. */
    public String description;

    /** URL pointing to the event’s main poster image. */
    public String eventPosterUrl;

    /** URL for the organizer's profile image or icon. */
    public String organizerIconUrl;

    /** Whether the event should be shown as a trending event. */
    public boolean isTrending;

    /** IDs of users who are enrolled / final entrants (optional, other story). */
    public List<String> enrolledUserIds;

    /** IDs of users who have been chosen in the lottery (invited to apply). */
    public List<String> chosenUserIds;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Empty constructor required for Firestore automatic deserialization. */
    public Event() {
        tags = new ArrayList<>();
        enrolledUserIds = new ArrayList<>();
        chosenUserIds = new ArrayList<>();
    }

    /** Reconstructs an Event from a Parcel. */
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

        enrolledUserIds = in.createStringArrayList();
        chosenUserIds = in.createStringArrayList();

        if (tags == null) tags = new ArrayList<>();
        if (enrolledUserIds == null) enrolledUserIds = new ArrayList<>();
        if (chosenUserIds == null) chosenUserIds = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Parcelable
    // -------------------------------------------------------------------------

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(price);
        dest.writeStringList(tags != null ? tags : new ArrayList<>());
        dest.writeString(organizer);
        dest.writeString(organizerId);
        dest.writeString(description);
        dest.writeString(eventPosterUrl);
        dest.writeString(organizerIconUrl);
        dest.writeByte((byte) (isTrending ? 1 : 0));

        dest.writeStringList(enrolledUserIds != null ? enrolledUserIds : new ArrayList<>());
        dest.writeStringList(chosenUserIds != null ? chosenUserIds : new ArrayList<>());
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

    // -------------------------------------------------------------------------
    // Getters (these are what all your adapters & activities are calling)
    // -------------------------------------------------------------------------

    /** @return the Firestore document ID of this event. */
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    /** @return the event title. */
    public String getTitle() { return title; }

    /** @return the event date. */
    public String getDate() { return date; }

    /** @return the event time or time range. */
    public String getTime() { return time; }

    /** @return the display name of the event venue. */
    public String getLocationName() { return locationName; }

    /** @return the full venue address. */
    public String getLocationAddress() { return locationAddress; }

    /** @return the event price text. */
    public String getPrice() { return price; }

    /** @return list of tag labels associated with the event. */
    public List<String> getTags() { return tags; }

    /** @return display name of the event organizer. */
    public String getOrganizer() { return organizer; }

    /** @return Firestore ID referencing the organizer in the users collection. */
    public String getOrganizerId() { return organizerId; }

    /** @return full event description. */
    public String getDescription() { return description; }

    /** @return URL for the event poster image. */
    public String getEventPosterUrl() { return eventPosterUrl; }

    /** @return URL for the organizer avatar/icon. */
    public String getOrganizerIconUrl() { return organizerIconUrl; }

    /** @return true if the event is marked as trending. */
    public boolean isTrending() { return isTrending; }

    /** @return enrolled user IDs list (never null). */
    public List<String> getEnrolledUserIds() {
        return enrolledUserIds == null ? new ArrayList<>() : enrolledUserIds;
    }

    public void setEnrolledUserIds(List<String> enrolledUserIds) {
        this.enrolledUserIds = enrolledUserIds;
    }

    /** @return chosen user IDs list (never null). */
    public List<String> getChosenUserIds() {
        return chosenUserIds == null ? new ArrayList<>() : chosenUserIds;
    }

    public void setChosenUserIds(List<String> chosenUserIds) {
        this.chosenUserIds = chosenUserIds;
    }
}
