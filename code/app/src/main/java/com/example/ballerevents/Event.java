package com.example.ballerevents;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class Event implements Parcelable {

    @DocumentId
    private String id; // Changed to String and added @DocumentId

    // All fields must be public or have getters/setters for Firestore
    public String title;
    public String date;
    public String time;
    public String locationName;
    public String locationAddress;
    public String price;
    public List<String> tags;
    public String organizer;
    public String organizerId; // Added to link to the User who created it
    public String description;
    public String eventPosterUrl; // Changed from int resource ID
    public String organizerIconUrl; // Changed from int resource ID
    public boolean isTrending;

    // --- Empty constructor REQUIRED for Firestore ---
    public Event() {}

    // --- Parcelable Implementation ---
    // (This is only needed if you pass the *entire* object between activities)
    // (We pass the ID, but good to keep this)

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
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocationName() { return locationName; }
    public String getLocationAddress() { return locationAddress; }
    public String getPrice() { return price; }
    public List<String> getTags() { return tags; }
    public String getOrganizer() { return organizer; }
    public String getOrganizerId() { return organizerId; }
    public String getDescription() { return description; }
    public String getEventPosterUrl() { return eventPosterUrl; }
    public String getOrganizerIconUrl() { return organizerIconUrl; }
    public boolean isTrending() { return isTrending; }
}