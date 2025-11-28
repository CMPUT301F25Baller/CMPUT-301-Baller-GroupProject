package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single event document from the Firestore {@code "events"} collection.
 * <p>
 * This class is a standard POJO used by Firestore for serialization and
 * deserialization. It also implements {@link Parcelable} so that event objects
 * can be passed between Android activities when needed.
 * </p>
 */
public class Event implements Parcelable {

    /**
     * Unique Firestore document ID for this event.
     * <p>
     * This value is automatically populated by Firestore when the object is
     * deserialized through {@code @DocumentId}.
     * </p>
     */
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
    /** URL for the organizer's profile image or icon. */
    public String organizerIconUrl;

    /** URL pointing to the event’s main poster image. */
    public String eventPosterUrl;
    /** Whether the event should be shown as a trending event. */
    public boolean isTrending;
    /** A descriptive summary or detailed information about the event. */
    public String description;

    // --- Lottery & Attendance System ---
    /** Maximum number of attendees allowed (Organizer + Lottery Winners). */
    public int maxAttendees;

    /** IDs of users who have signed up for the waitlist. */
    public List<String> waitingListIds;

    /** IDs of users explicitly invited by the organizer. */
    public List<String> invitedUserIds;

    /** IDs of users selected via the lottery draw. */
    public List<String> drawnUserIds;

    /** IDs of users who have ACCEPTED the invitation or lottery win. */
    public List<String> confirmedUserIds;

    /** IDs of users who DECLINED or cancelled. */
    public List<String> cancelledUserIds;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Empty constructor required for Firestore automatic deserialization.
     * <p>Do not remove or modify.</p>
     */
    public Event() {
        // Firestore requires a no-arg constructor
        this.tags = new ArrayList<>();
        this.waitingListIds = new ArrayList<>();
        this.invitedUserIds = new ArrayList<>();
        this.drawnUserIds = new ArrayList<>();
        this.confirmedUserIds = new ArrayList<>();
        this.cancelledUserIds = new ArrayList<>();
        this.maxAttendees = 10; // Default
    }



    /**
     * Reconstructs an Event from a Parcel.
     *
     * @param in Parcel containing serialized event data
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
        maxAttendees = in.readInt();
        waitingListIds = in.createStringArrayList();
        invitedUserIds = in.createStringArrayList();
        drawnUserIds = in.createStringArrayList();
        confirmedUserIds = in.createStringArrayList();
        cancelledUserIds = in.createStringArrayList();
    }

    // -------------------------------------------------------------------------
    // Parcelable Implementation
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
        dest.writeStringList(tags);
        dest.writeString(organizer);
        dest.writeString(organizerId);
        dest.writeString(description);
        dest.writeString(eventPosterUrl);
        dest.writeString(organizerIconUrl);
        dest.writeByte((byte) (isTrending ? 1 : 0));
        dest.writeInt(maxAttendees);
        dest.writeStringList(waitingListIds);
        dest.writeStringList(invitedUserIds);
        dest.writeStringList(drawnUserIds);
        dest.writeStringList(confirmedUserIds);
        dest.writeStringList(cancelledUserIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /** Parcelable creator for generating Event instances from a Parcel. */
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
    // Getters
    // -------------------------------------------------------------------------

    /** @return the Firestore document ID of this event. */
    public String getId() { return id; }

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

    /**
     * Sets the Firestore document ID for this event.
     *
     * @param id new Firestore ID value
     */
    public void setId(String id) { this.id = id; }

    /** @return true if the event is marked as trending. */
    public boolean isTrending() { return isTrending; }

    public int getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(int maxAttendees) { this.maxAttendees = maxAttendees; }

    public List<String> getWaitingListIds() { return waitingListIds; }
    public List<String> getInvitedUserIds() { return invitedUserIds; }
    public List<String> getDrawnUserIds() { return drawnUserIds; }
    public List<String> getConfirmedUserIds() { return confirmedUserIds; }
    public List<String> getCancelledUserIds() { return cancelledUserIds; }
}
