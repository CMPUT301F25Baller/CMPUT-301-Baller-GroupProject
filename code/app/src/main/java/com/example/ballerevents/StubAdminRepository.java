package com.example.ballerevents;

import java.util.Arrays;
import java.util.List;

public class StubAdminRepository implements AdminRepository {

    @Override
    public void getRecentEvents(Callback<Event> cb) {
        // Reuse your teammate’s hard-coded repo
        cb.onResult(EventRepository.getTrendingEvents());
    }

    @Override
    public void getRecentProfiles(Callback<Profile> cb) {
        List<Profile> list = Arrays.asList(
                new Profile("p1", "Silbia", R.drawable.placeholder_avatar1),
                new Profile("p2", "Safi",   R.drawable.placeholder_avatar2),
                new Profile("p3", "Baker",  R.drawable.placeholder_avatar3)
        );
        cb.onResult(list);
    }

    @Override
    public void getRecentImages(Callback<ImageAsset> cb) {
        List<ImageAsset> list = Arrays.asList(
                new ImageAsset("i1", "poster_001", R.drawable.placeholder_coldplay),
                new ImageAsset("i2", "poster_002", R.drawable.placeholder_muse),
                new ImageAsset("i3", "poster_003", R.drawable.placeholder_standup)
        );
        cb.onResult(list);
    }
}
