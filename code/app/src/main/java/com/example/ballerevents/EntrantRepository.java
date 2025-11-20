package com.example.ballerevents;

import java.util.*;

public class EntrantRepository {

    // Map<eventId, List<Entrant>>
    private static final Map<Long, List<Entrant>> EVENT_ENTRANTS = new HashMap<>();

    static {
        // Hardcode entrants for eventId = 1 (example)
        EVENT_ENTRANTS.put(1L, Arrays.asList(
                new Entrant("David Silbia",    "Declined",   R.drawable.placeholder_avatar_david, System.currentTimeMillis() - (1000)),
                new Entrant("Joan Baker",   "Enrolled",   R.drawable.placeholder_avatar_joan, System.currentTimeMillis() - (20 * 60 * 1000)),
                new Entrant("Adnan Safi",  "Cancelled",  R.drawable.placeholder_avatar_adnan, System.currentTimeMillis() - (60 * 60 * 1000)),
                new Entrant("Ronald C. Kinch",    "Waitlisted", R.drawable.placeholder_avatar_ronald, System.currentTimeMillis() - (2 * 60 * 60 * 1000)),
                new Entrant("Clara Tolson",   "Invited",   R.drawable.placeholder_avatar_klara, System.currentTimeMillis() - (24 * 60 * 60 * 1000)),
                new Entrant("Jennifer Fritz",   "Cancelled",   R.drawable.placeholder_avatar_jennifer, System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)),
                new Entrant("Eric G. Pickett",   "Enrolled",   R.drawable.placeholder_avatar_joan, System.currentTimeMillis() - (32L * 24 * 60 * 60 * 1000))

        ));
    }

    public static List<Entrant> getAllEntrants(long eventId) {
        return EVENT_ENTRANTS.getOrDefault(eventId, Collections.emptyList());
    }

    public static List<Entrant> getEntrantsByStatus(long eventId, String status) {
        if (status.equals("All")){
            return getAllEntrants(eventId);
        }
        List<Entrant> all = EVENT_ENTRANTS.getOrDefault(eventId, Collections.emptyList());

        List<Entrant> result = new ArrayList<>();
        assert all != null;
        for (Entrant e : all) {
            if (e.getStatus().equalsIgnoreCase(status)) {
                result.add(e);
            }
        }
        return result;
    }
}
