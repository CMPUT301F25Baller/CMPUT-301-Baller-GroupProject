package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Final merged Event model combining:
 * - Main branch fields
 * - Lottery system fields
 * - Full Parcelable support
 */
public class Event implements Parcelable {

    @DocumentId
    private String id;

    // -----------------------------
    // Standard Event Metadata
    // -----------------------------
    public String title;
    public String date;
    public String time;
    public String locationName;
    public String locationAddress;        // MAIN branch
    public String description;
    public String price;
    public String organizer;
    public String organizerId;
    public String eventPosterUrl;
    public String organizerIconUrl;       // MAIN branch
    public boolean isTrending;

    public List<String> tags = new ArrayList<>();

    // -----------------------------
    // MAIN Branch Attendance Fields
    // -----------------------------

    /** Enrolled / confirmed attendees */
    public List<String> enrolledUserIds = new ArrayList<>();

    /** Final chosen winner IDs (old main field) */
    public List<String> chosenUserIds = new ArrayList<>();

    // -----------------------------
    // Lottery System Fields (Your version)
    // -----------------------------

    /** Maximum number of attendees allowed */
    public int maxAttendees = 0;

    /** Users waiting to be drawn in lottery */
    public List<String> waitlistUserIds = new ArrayList<>();

    /** Users selected in lottery or manually invited */
    public List<String> selectedUserIds = new ArrayList<>();

    /** Users who declined an invite */
    public List<String> cancelledUserIds = new ArrayList<>();

    /** Map userId → status ("pending", "accepted", "declined") */
    public Map<String, String> invitationStatus = new HashMap<>();

    /** Registration windows */
    public long registrationOpenAtMillis = 0;
    public long registrationCloseAtMillis = 0;

    // -----------------------------
    // Constructor
    // -----------------------------
    public Event() {}

    // -----------------------------
    // Getters
    // -----------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocationName() { return locationName; }
    public String getLocationAddress() { return locationAddress; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getOrganizer() { return organizer; }
    public String getOrganizerId() { return organizerId; }
    public String getEventPosterUrl() { return eventPosterUrl; }
    public String getOrganizerIconUrl() { return organizerIconUrl; }
    public boolean isTrending() { return isTrending; }

    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }

    public List<String> getEnrolledUserIds() {
        return enrolledUserIds == null ? new ArrayList<>() : enrolledUserIds;
    }

    public List<String> getChosenUserIds() {
        return chosenUserIds == null ? new ArrayList<>() : chosenUserIds;
    }

    public List<String> getWaitlistUserIds() { return waitlistUserIds; }
    public List<String> getSelectedUserIds() { return selectedUserIds; }
    public List<String> getCancelledUserIds() { return cancelledUserIds; }
    public Map<String, String> getInvitationStatus() { return invitationStatus; }

    // -----------------------------
    // Lottery Helper Methods
    // -----------------------------

    /** True if user accepted their invitation */
    public boolean isUserConfirmed(String userId) {
        return invitationStatus != null &&
                "accepted".equals(invitationStatus.get(userId));
    }

    /** True if registration window is active */
    public boolean isRegistrationOpenAt(long nowMillis) {
        boolean startOk = registrationOpenAtMillis == 0 || nowMillis >= registrationOpenAtMillis;
        boolean endOk = registrationCloseAtMillis == 0 || nowMillis <= registrationCloseAtMillis;
        return startOk && endOk;
    }

    // -----------------------------
    // Parcelable Constructor
    // -----------------------------
    protected Event(Parcel in) {
        id = in.readString();

        // Standard fields
        title = in.readString();
        date = in.readString();
        time = in.readString();
        locationName = in.readString();
        locationAddress = in.readString();
        description = in.readString();
        price = in.readString();
        organizer = in.readString();
        organizerId = in.readString();
        eventPosterUrl = in.readString();
        organizerIconUrl = in.readString();
        isTrending = in.readByte() != 0;

        tags = in.createStringArrayList();
        enrolledUserIds = in.createStringArrayList();
        chosenUserIds = in.createStringArrayList();

        // Lottery
        maxAttendees = in.readInt();
        waitlistUserIds = in.createStringArrayList();
        selectedUserIds = in.createStringArrayList();
        cancelledUserIds = in.createStringArrayList();

        // Read invitationStatus map
        int mapSize = in.readInt();
        for (int i = 0; i < mapSize; i++) {
            String key = in.readString();
            String val = in.readString();
            invitationStatus.put(key, val);
        }

        registrationOpenAtMillis = in.readLong();
        registrationCloseAtMillis = in.readLong();
    }

    // -----------------------------
    // Parcelable Writer
    // -----------------------------
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);

        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(organizer);
        dest.writeString(organizerId);
        dest.writeString(eventPosterUrl);
        dest.writeString(organizerIconUrl);
        dest.writeByte((byte) (isTrending ? 1 : 0));

        dest.writeStringList(tags);
        dest.writeStringList(enrolledUserIds);
        dest.writeStringList(chosenUserIds);

        dest.writeInt(maxAttendees);
        dest.writeStringList(waitlistUserIds);
        dest.writeStringList(selectedUserIds);
        dest.writeStringList(cancelledUserIds);

        // Write map
        dest.writeInt(invitationStatus.size());
        for (Map.Entry<String, String> entry : invitationStatus.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }

        dest.writeLong(registrationOpenAtMillis);
        dest.writeLong(registrationCloseAtMillis);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) { return new Event(in); }

        @Override
        public Event[] newArray(int size) { return new Event[size]; }
    };
}
