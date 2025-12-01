package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single event document from the Firestore "events" collection.
 * Updated to support Lottery System fields.
 */
public class Event implements Parcelable {

    @DocumentId
    private String id;
    public String title;
    public String date;
    public String time;
    public String locationName;
    public String description;
    public String price;
    public String organizer;     // Organizer Name
    public String organizerId;   // Organizer UID
    public String eventPosterUrl;
    public String eventBannerUrl;
    public boolean geolocationRequired;
    public List<String> tags = new ArrayList<>();

    // --- Lottery & Registration Fields ---

    /** Maximum number of people allowed to attend (Selected + Confirmed). */
    public int maxAttendees;

    /** * Users who have signed up for the lottery.
     * These users are waiting to be drawn.
     */
    public List<String> waitlistUserIds = new ArrayList<>();

    /** * Users who have been selected (won the lottery) or manually invited.
     * They are now pending acceptance.
     */
    public List<String> selectedUserIds = new ArrayList<>();

    /**
     * Users who were selected but explicitly declined the invitation.
     * Kept for record-keeping so we don't re-draw them.
     */
    public List<String> cancelledUserIds = new ArrayList<>();

    /**
     * Map of userId -> Status ("pending", "accepted", "declined").
     * Used to track the state of selected users.
     */
    public Map<String, String> invitationStatus = new HashMap<>();

    // Registration Window
    public long registrationOpenAtMillis = 0;
    public long registrationCloseAtMillis = 0;
    public boolean isTrending = false;

    // Required empty constructor for Firestore
    public Event() {}

    // Getters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocationName() { return locationName; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getOrganizer() { return organizer; }
    public String getOrganizerId() { return organizerId; }

    public boolean isGeolocationRequired() {return geolocationRequired;}

    public String getEventPosterUrl() { return eventPosterUrl; }
    public String getEventBannerUrl() { return eventBannerUrl; }
    public List<String> getTags() { return tags; }

    public boolean isTrending() { return isTrending; }
    public void setTrending(boolean trending) { isTrending = trending; }

    // Lottery Getters/Setters
    public int getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(int maxAttendees) { this.maxAttendees = maxAttendees; }

    public List<String> getWaitlistUserIds() { return waitlistUserIds; }
    public List<String> getSelectedUserIds() { return selectedUserIds; }
    public List<String> getCancelledUserIds() { return cancelledUserIds; }
    public Map<String, String> getInvitationStatus() { return invitationStatus; }

    /**
     * Helper to check if a specific user has accepted their invite.
     */
    public boolean isUserConfirmed(String userId) {
        return invitationStatus != null &&
                "accepted".equals(invitationStatus.get(userId));
    }

    /**
     * Helper to check if registration is currently open.
     */
    public boolean isRegistrationOpenAt(long nowMillis) {
        boolean startOk = (registrationOpenAtMillis == 0L) || (nowMillis >= registrationOpenAtMillis);
        boolean endOk   = (registrationCloseAtMillis == 0L) || (nowMillis <= registrationCloseAtMillis);
        return startOk && endOk;
    }

    // --- Parcelable Implementation ---
    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        date = in.readString();
        time = in.readString();
        locationName = in.readString();
        description = in.readString();
        price = in.readString();
        organizer = in.readString();
        organizerId = in.readString();
        geolocationRequired = in.readBoolean();
        eventPosterUrl = in.readString();
        eventBannerUrl = in.readString();
        tags = in.createStringArrayList();
        maxAttendees = in.readInt();
        waitlistUserIds = in.createStringArrayList();
        selectedUserIds = in.createStringArrayList();
        cancelledUserIds = in.createStringArrayList();
        // Simple serialization for map (size + keys/values)
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            String val = in.readString();
            invitationStatus.put(key, val);
        }
        registrationOpenAtMillis = in.readLong();
        registrationCloseAtMillis = in.readLong();
        isTrending = in.readByte() != 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) { return new Event(in); }
        @Override
        public Event[] newArray(int size) { return new Event[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(locationName);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(organizer);
        dest.writeString(organizerId);
        dest.writeBoolean(geolocationRequired);
        dest.writeString(eventPosterUrl);
        dest.writeString(eventBannerUrl);
        dest.writeStringList(tags);
        dest.writeInt(maxAttendees);
        dest.writeStringList(waitlistUserIds);
        dest.writeStringList(selectedUserIds);
        dest.writeStringList(cancelledUserIds);
        dest.writeInt(invitationStatus.size());
        for(Map.Entry<String, String> entry : invitationStatus.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeLong(registrationOpenAtMillis);
        dest.writeLong(registrationCloseAtMillis);
        dest.writeByte((byte) (isTrending ? 1 : 0));
    }
}