package com.example.ballerevents;

import java.util.List;

/** Read-only data access used by the Admin dashboard (stub for Half-Way). */
public interface AdminRepository {
    interface Callback<T> { void onResult(List<T> items); }

    void getRecentEvents(Callback<Event> cb);
    void getRecentProfiles(Callback<Profile> cb);
    void getRecentImages(Callback<ImageAsset> cb);
}
