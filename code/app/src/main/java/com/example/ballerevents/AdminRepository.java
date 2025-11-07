package com.example.ballerevents;

import java.util.List;

public interface AdminRepository {
    interface Callback<T> { void onResult(List<T> items); }
    void getRecentEvents(Callback<Event> cb);
    void getRecentProfiles(Callback<Profile> cb);
    void getRecentImages(Callback<ImageAsset> cb);
}
