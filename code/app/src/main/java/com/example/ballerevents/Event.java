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

    // -------------------------------------------------
    // BASIC EVENT METADATA (Common + Main + Your branch)
    // -------------------------------------------------
    private String title;
    private String date;
    private String time;
    private String locationName;
    private String description;
    private String price;
    private String organizer;
    private String organizerId;

    private String eventPosterUrl;
    private String eventBannerUrl;            // your branch
    private boolean geolocationRequired;      // your branch

    private boolean isTrending;
    private List<String> tags = new ArrayList<>();

    // -------------------------------------------------
    // LOTTERY FIELDS
    // -------------------------------------------------
    private int maxAttendees;

    private List<String> waitlistUserIds = new ArrayList<>();
    private List<String> selectedUserIds = new ArrayList<>();
    private List<String> cancelledUserIds = new ArrayList<>();

    /** userId → "pending" | "accepted" | "declined" */
    private Map<String, String> invitationStatus = new HashMap<>();

    private long registrationOpenAtMillis = 0;
    private long registrationCloseAtMillis = 0;

    // -------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------
    public Event() {}

    // Convenience constructor for OrganizerEventCreationActivity
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

        // Defaults so Firestore doesn't break
        this.price = "";
        this.organizer = "";
        this.organizerId = "";
        this.eventPosterUrl = "";
        this.eventBannerUrl = "";
        this.geolocationRequired = false;
        this.isTrending = false;
    }

    // -------------------------------------------------
    // GETTERS + SETTERS (FULL)
    // -------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getEventPosterUrl() { return eventPosterUrl; }
    public void setEventPosterUrl(String eventPosterUrl) { this.eventPosterUrl = eventPosterUrl; }

    public String getEventBannerUrl() { return eventBannerUrl; }
    public void setEventBannerUrl(String eventBannerUrl) { this.eventBannerUrl = eventBannerUrl; }

    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    public boolean isTrending() { return isTrending; }
    public void setTrending(boolean trending) { isTrending = trending; }

    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // LOTTERY
    public int getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(int maxAttendees) { this.maxAttendees = maxAttendees; }

    public List<String> getWaitlistUserIds() { return waitlistUserIds == null ? new ArrayList<>() : waitlistUserIds; }
    public void setWaitlistUserIds(List<String> waitlistUserIds) { this.waitlistUserIds = waitlistUserIds; }

    public List<String> getSelectedUserIds() { return selectedUserIds == null ? new ArrayList<>() : selectedUserIds; }
    public void setSelectedUserIds(List<String> selectedUserIds) { this.selectedUserIds = selectedUserIds; }

    public List<String> getCancelledUserIds() { return cancelledUserIds == null ? new ArrayList<>() : cancelledUserIds; }
    public void setCancelledUserIds(List<String> cancelledUserIds) { this.cancelledUserIds = cancelledUserIds; }

    public Map<String, String> getInvitationStatus() {
        return invitationStatus == null ? new HashMap<>() : invitationStatus;
    }

    public void setInvitationStatus(Map<String, String> invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public long getRegistrationOpenAtMillis() { return registrationOpenAtMillis; }
    public void setRegistrationOpenAtMillis(long registrationOpenAtMillis) { this.registrationOpenAtMillis = registrationOpenAtMillis; }

    public long getRegistrationCloseAtMillis() { return registrationCloseAtMillis; }
    public void setRegistrationCloseAtMillis(long registrationCloseAtMillis) { this.registrationCloseAtMillis = registrationCloseAtMillis; }

    // -------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------

    public boolean isUserConfirmed(String userId) {
        return invitationStatus != null &&
                "accepted".equals(invitationStatus.get(userId));
    }

    public boolean isRegistrationOpenAt(long nowMillis) {
        boolean startOk = registrationOpenAtMillis == 0 || nowMillis >= registrationOpenAtMillis;
        boolean endOk = registrationCloseAtMillis == 0 || nowMillis <= registrationCloseAtMillis;
        return startOk && endOk;
    }

    // -------------------------------------------------
    // PARCELABLE
    // -------------------------------------------------

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

        geolocationRequired = in.readByte() != 0;
        eventPosterUrl = in.readString();
        eventBannerUrl = in.readString();

        tags = in.createStringArrayList();

        maxAttendees = in.readInt();
        waitlistUserIds = in.createStringArrayList();
        selectedUserIds = in.createStringArrayList();
        cancelledUserIds = in.createStringArrayList();

        int mapSize = in.readInt();
        invitationStatus = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            invitationStatus.put(in.readString(), in.readString());
        }

        registrationOpenAtMillis = in.readLong();
        registrationCloseAtMillis = in.readLong();
        isTrending = in.readByte() != 0;
    }

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

        dest.writeByte((byte) (geolocationRequired ? 1 : 0));
        dest.writeString(eventPosterUrl);
        dest.writeString(eventBannerUrl);

        dest.writeStringList(tags);

        dest.writeInt(maxAttendees);
        dest.writeStringList(waitlistUserIds);
        dest.writeStringList(selectedUserIds);
        dest.writeStringList(cancelledUserIds);

        dest.writeInt(invitationStatus.size());
        for (Map.Entry<String, String> e : invitationStatus.entrySet()) {
            dest.writeString(e.getKey());
            dest.writeString(e.getValue());
        }

        dest.writeLong(registrationOpenAtMillis);
        dest.writeLong(registrationCloseAtMillis);
        dest.writeByte((byte) (isTrending ? 1 : 0));
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
