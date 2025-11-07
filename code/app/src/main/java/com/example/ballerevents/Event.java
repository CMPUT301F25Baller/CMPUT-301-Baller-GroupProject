package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Event implements Parcelable {

    private final long id;
    private final String title;
    private final String date;
    private final String time;
    private final String locationName;
    private final String locationAddress;
    private final String price;
    private final List<String> tags;
    private final String organizer;
    private final String description;
    private final int waitlistCount;
    private final int eventPosterResId;
    private final int organizerIconResId;
    private final boolean isTrending;

    public Event(long id, String title, String date, String time, String locationName,
                 String locationAddress, String price, List<String> tags, String organizer,
                 String description, int waitlistCount, int eventPosterResId,
                 int organizerIconResId, boolean isTrending) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.price = price;
        this.tags = tags;
        this.organizer = organizer;
        this.description = description;
        this.waitlistCount = waitlistCount;
        this.eventPosterResId = eventPosterResId;
        this.organizerIconResId = organizerIconResId;
        this.isTrending = isTrending;
    }

    // --- Getters ---
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocationName() { return locationName; }
    public String getLocationAddress() { return locationAddress; }
    public String getPrice() { return price; }
    public List<String> getTags() { return tags; }
    public String getOrganizer() { return organizer; }
    public String getDescription() { return description; }
    public int getWaitlistCount() { return waitlistCount; }
    public int getEventPosterResId() { return eventPosterResId; }
    public int getOrganizerIconResId() { return organizerIconResId; }
    public boolean isTrending() { return isTrending; }


    // --- Parcelable Implementation ---
    protected Event(Parcel in) {
        id = in.readLong();
        title = in.readString();
        date = in.readString();
        time = in.readString();
        locationName = in.readString();
        locationAddress = in.readString();
        price = in.readString();
        tags = in.createStringArrayList();
        organizer = in.readString();
        description = in.readString();
        waitlistCount = in.readInt();
        eventPosterResId = in.readInt();
        organizerIconResId = in.readInt();
        isTrending = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(price);
        dest.writeStringList(tags);
        dest.writeString(organizer);
        dest.writeString(description);
        dest.writeInt(waitlistCount);
        dest.writeInt(eventPosterResId);
        dest.writeInt(organizerIconResId);
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
}