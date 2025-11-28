package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

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

    /** A descriptive summary or detailed information about the event. */
    public String description;

    /** URL pointing to the event’s main poster image. */
    public String eventPosterUrl;

    /** URL for the organizer's profile image or icon. */
    public String organizerIconUrl;

    /** Whether the event should be shown as a trending event. */
    public boolean isTrending;

    /**
     * Event start time in milliseconds since Unix epoch.
     * <p>
     * A value of {@code 0} indicates that no explicit start time was stored.
     * </p>
     */
    public long eventStartAtMillis;

    /**
     * Event end time in milliseconds since Unix epoch.
     * <p>
     * A value of {@code 0} indicates that no explicit end time was stored.
     * </p>
     */
    public long eventEndAtMillis;

    /**
     * Registration opening time in milliseconds since Unix epoch.
     * <p>
     * A value of {@code 0} indicates that no explicit opening time
     * was configured for this event.
     * </p>
     */
    public long registrationOpenAtMillis;

    /**
     * Registration closing time in milliseconds since Unix epoch.
     * <p>
     * A value of {@code 0} indicates that no explicit closing time
     * was configured for this event.
     * </p>
     */
    public long registrationCloseAtMillis;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Empty constructor required for Firestore automatic deserialization.
     * <p>Do not remove or modify.</p>
     */
    public Event() {
        // Default no-arg constructor
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
        eventStartAtMillis = in.readLong();
        eventEndAtMillis = in.readLong();
        registrationOpenAtMillis = in.readLong();
        registrationCloseAtMillis = in.readLong();
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
        dest.writeLong(eventStartAtMillis);
        dest.writeLong(eventEndAtMillis);
        dest.writeLong(registrationOpenAtMillis);
        dest.writeLong(registrationCloseAtMillis);
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

    /** @return true if the event is marked as trending. */
    public boolean isTrending() { return isTrending; }

    /**
     * @return event start time in milliseconds since epoch;
     *         {@code 0} means no explicit start time was stored.
     */
    public long getEventStartAtMillis() { return eventStartAtMillis; }

    /**
     * @return event end time in milliseconds since epoch;
     *         {@code 0} means no explicit end time was stored.
     */
    public long getEventEndAtMillis() { return eventEndAtMillis; }

    /**
     * @return registration opening time in milliseconds since epoch;
     *         {@code 0} means no explicit opening time.
     */
    public long getRegistrationOpenAtMillis() { return registrationOpenAtMillis; }

    /**
     * @return registration closing time in milliseconds since epoch;
     *         {@code 0} means no explicit closing time.
     */
    public long getRegistrationCloseAtMillis() { return registrationCloseAtMillis; }

    // -------------------------------------------------------------------------
    // Setters / helpers
    // -------------------------------------------------------------------------

    /**
     * Sets the Firestore document ID for this event.
     *
     * @param id new Firestore ID value
     */
    public void setId(String id) { this.id = id; }

    /**
     * Sets the event start time.
     *
     * @param millis time in milliseconds since epoch; {@code 0} to unset
     */
    public void setEventStartAtMillis(long millis) {
        this.eventStartAtMillis = millis;
    }

    /**
     * Sets the event end time.
     *
     * @param millis time in milliseconds since epoch; {@code 0} to unset
     */
    public void setEventEndAtMillis(long millis) {
        this.eventEndAtMillis = millis;
    }

    /**
     * Sets the registration opening time.
     *
     * @param millis time in milliseconds since epoch; {@code 0} to unset
     */
    public void setRegistrationOpenAtMillis(long millis) {
        this.registrationOpenAtMillis = millis;
    }

    /**
     * Sets the registration closing time.
     *
     * @param millis time in milliseconds since epoch; {@code 0} to unset
     */
    public void setRegistrationCloseAtMillis(long millis) {
        this.registrationCloseAtMillis = millis;
    }

    /**
     * Returns whether registration is open at the specified moment.
     * <ul>
     *     <li>If {@link #registrationOpenAtMillis} is {@code 0}, the start is unbounded.</li>
     *     <li>If {@link #registrationCloseAtMillis} is {@code 0}, the end is unbounded.</li>
     * </ul>
     *
     * @param nowMillis current time in milliseconds since epoch
     * @return {@code true} if {@code nowMillis} lies within the registration window
     */
    public boolean isRegistrationOpenAt(long nowMillis) {
        boolean startOk = (registrationOpenAtMillis == 0L) || (nowMillis >= registrationOpenAtMillis);
        boolean endOk   = (registrationCloseAtMillis == 0L) || (nowMillis <= registrationCloseAtMillis);
        return startOk && endOk;
    }
}
