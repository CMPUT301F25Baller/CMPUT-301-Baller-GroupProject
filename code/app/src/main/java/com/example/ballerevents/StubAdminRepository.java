package com.example.ballerevents;

import java.util.Arrays;
import java.util.List;

/** Returns hard-coded data so the UI is demo-able without backend. */
public class StubAdminRepository implements AdminRepository {

    @Override public void getRecentEvents(Callback<Event> cb) {
        List<Event> list = Arrays.asList(
                new Event("e1", "Coldplay – Music of the Spheres", "Nov 15"),
                new Event("e2", "Jazz Night", "Dec 01"),
                new Event("e3", "Open Mic Friday", "Dec 08")
        );
        cb.onResult(list);
    }

    @Override public void getRecentProfiles(Callback<Profile> cb) {
        List<Profile> list = Arrays.asList(
                new Profile("p1", "Silbia"),
                new Profile("p2", "Safi"),
                new Profile("p3", "Baker"),
                new Profile("p4", "Kinch")
        );
        cb.onResult(list);
    }

    @Override public void getRecentImages(Callback<ImageAsset> cb) {
        List<ImageAsset> list = Arrays.asList(
                new ImageAsset("i1", "poster_a"),
                new ImageAsset("i2", "poster_b"),
                new ImageAsset("i3", "poster_c")
        );
        cb.onResult(list);
    }
}
