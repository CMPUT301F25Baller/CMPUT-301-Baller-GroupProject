// FILE: Event.java
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
    private String title;
    private String date;
    private String time;
    private String locationName;
    private String locationAddress;  // MAIN branch
    private String description;
    private String price;
    private String organizer;
    private String organizerId;
    private String eventPosterUrl;
    private String organizerIconUrl; // MAIN branch
    private boolean isTrending;

    private List<String> tags = new ArrayList<>();

    // -----------------------------
    // MAIN Branch Attendance Fields
    // -----------------------------

    /** Enrolled / confirmed attendees */
    private List<String> enrolledUserIds = new ArrayList<>();

    /** Final chosen winner IDs (old main field) */
    private List<String> chosenUserIds = new ArrayList<>();

    // -----------------------------
    // Lottery System Fields
    // -----------------------------

    /** Maximum number of attendees allowed */
    private int maxAttendees = 0;

    /** Users waiting to be drawn in lottery */
    private List<String> waitlistUserIds = new ArrayList<>();

    /** Users selected in lottery or manually invited */
    private List<String> selectedUserIds = new ArrayList<>();

    /** Users who declined an invite */
    private List<String> cancelledUserIds = new ArrayList<>();

    /** Map userId → status ("pending", "accepted", "declined") */
    private Map<String, String> invitationStatus = new HashMap<>();

    /** Registration windows */
    private long registrationOpenAtMillis = 0;
    private long registrationCloseAtMillis = 0;

    // -----------------------------
    // Constructors
    // -----------------------------

    /** Required empty constructor for Firestore. */
    public Event() {
    }

    /**
     * Convenience constructor used from OrganizerEventCreationActivity.
     */
    public Event(String title,
                 String description,
                 String date,
                 String time,
                 String locationName,
                 int maxAttendees) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.locationName = locationName;
        this.maxAttendees = maxAttendees;

        // Reasonable defaults
        this.locationAddress = "";
        this.price = "";
        this.organizer = "";
        this.organizerId = "";
        this.eventPosterUrl = "";
        this.organizerIconUrl = "";
        this.isTrending = false;
    }

    // -----------------------------
    // Getters / Setters
    // -----------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // --- Basic metadata ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getEventPosterUrl() {
        return eventPosterUrl;
    }

    public void setEventPosterUrl(String eventPosterUrl) {
        this.eventPosterUrl = eventPosterUrl;
    }

    public String getOrganizerIconUrl() {
        return organizerIconUrl;
    }

    public void setOrganizerIconUrl(String organizerIconUrl) {
        this.organizerIconUrl = organizerIconUrl;
    }

    public boolean isTrending() {
        return isTrending;
    }

    public void setTrending(boolean trending) {
        isTrending = trending;
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // --- Attendance / winner lists ---

    public List<String> getEnrolledUserIds() {
        return enrolledUserIds == null ? new ArrayList<>() : enrolledUserIds;
    }

    public void setEnrolledUserIds(List<String> enrolledUserIds) {
        this.enrolledUserIds = enrolledUserIds;
    }

    public List<String> getChosenUserIds() {
        return chosenUserIds == null ? new ArrayList<>() : chosenUserIds;
    }

    public void setChosenUserIds(List<String> chosenUserIds) {
        this.chosenUserIds = chosenUserIds;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public List<String> getWaitlistUserIds() {
        return waitlistUserIds == null ? new ArrayList<>() : waitlistUserIds;
    }

    public void setWaitlistUserIds(List<String> waitlistUserIds) {
        this.waitlistUserIds = waitlistUserIds;
    }

    public List<String> getSelectedUserIds() {
        return selectedUserIds == null ? new ArrayList<>() : selectedUserIds;
    }

    public void setSelectedUserIds(List<String> selectedUserIds) {
        this.selectedUserIds = selectedUserIds;
    }

    public List<String> getCancelledUserIds() {
        return cancelledUserIds == null ? new ArrayList<>() : cancelledUserIds;
    }

    public void setCancelledUserIds(List<String> cancelledUserIds) {
        this.cancelledUserIds = cancelledUserIds;
    }

    public Map<String, String> getInvitationStatus() {
        return invitationStatus == null ? new HashMap<>() : invitationStatus;
    }

    public void setInvitationStatus(Map<String, String> invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public long getRegistrationOpenAtMillis() {
        return registrationOpenAtMillis;
    }

    public void setRegistrationOpenAtMillis(long registrationOpenAtMillis) {
        this.registrationOpenAtMillis = registrationOpenAtMillis;
    }

    public long getRegistrationCloseAtMillis() {
        return registrationCloseAtMillis;
    }

    public void setRegistrationCloseAtMillis(long registrationCloseAtMillis) {
        this.registrationCloseAtMillis = registrationCloseAtMillis;
    }

    // -----------------------------
    // Lottery Helper Methods
    // -----------------------------

    /** True if user accepted their invitation */
    public boolean isUserConfirmed(String userId) {
        return getInvitationStatus() != null &&
                "accepted".equals(getInvitationStatus().get(userId));
    }

    /** True if registration window is active */
    public boolean isRegistrationOpenAt(long nowMillis) {
        boolean startOk = registrationOpenAtMillis == 0 || nowMillis >= registrationOpenAtMillis;
        boolean endOk = registrationCloseAtMillis == 0 || nowMillis <= registrationCloseAtMillis;
        return startOk && endOk;
    }

    // -----------------------------
    // Parcelable constructor
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
        invitationStatus = new HashMap<>();
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
    // Parcelable writer
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
        Map<String, String> map = getInvitationStatus();
        dest.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }

        dest.writeLong(registrationOpenAtMillis);
        dest.writeLong(registrationCloseAtMillis);
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
}
