package com.example.ballerevents;

import com.example.ballerevents.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventRepository {

    // --- MOCK USER ---
    // This represents the "currently logged in" user
    public static final String MOCK_USER_ID = "user_123_abc";

    // Note: You must add these drawables to your res/drawable folder
    private static final List<Event> allEvents = Arrays.asList(
            new Event(
                    1L,
                    "Coldplay : Music of the Spheres",
                    "November 15 2023",
                    "8:00PM - 11:00PM",
                    "Gelora Bung Karno Stadium",
                    "Jakarta, Indonesia",
                    "$120",
                    Arrays.asList("Music Concert", "Rock", "Pop"),
                    "Coldplay",
                    "Enjoy your favorite dishes and a lovely time with your friends and family for a great experience! Food from local food trucks will be available for purchase. Read More..",
                    132,
                    R.drawable.placeholder_coldplay,
                    R.drawable.placeholder_organizer_icon,
                    true
            ),
            new Event(
                    2L,
                    "Muse : Will of the People",
                    "July 23 2023",
                    "7:00PM - 10:00PM",
                    "Jakarta, Indonesia",
                    "Stadium ABC, Jakarta",
                    "IDR500.000",
                    Arrays.asList("Music Concert", "Rock"),
                    "Muse",
                    "The Will of the People Tour comes to Jakarta. Don't miss this epic performance. A night of rock and revolution awaits. Get your tickets before they sell out!",
                    45,
                    R.drawable.placeholder_muse,
                    R.drawable.placeholder_organizer_icon,
                    false
            ),
            new Event(
                    3L,
                    "Stand Up Show: Local Heroes",
                    "December 10 2023",
                    "6:00PM - 8:00PM",
                    "Comedy Central Hall",
                    "123 Laugh St, London, UK",
                    "$25",
                    Arrays.asList("Stand Up Show", "Comedy"),
                    "Comedy Inc.",
                    "Get ready to laugh! Featuring the best local talent in the stand-up scene. A perfect night out.",
                    12,
                    R.drawable.placeholder_standup,
                    R.drawable.placeholder_organizer_icon,
                    true
            )
    );

    // --- NEW: Mock Database for Event Applications ---
    // Map<EventID, List<UserID>>
    private static Map<Long, List<String>> eventApplications = new HashMap<>();

    // Static initializer to pre-populate the application data
    static {
        // Event 1 (Coldplay) - Our mock user IS applied
        List<String> event1Applicants = new ArrayList<>();
        for (int i = 0; i < 131; i++) {
            event1Applicants.add("user_" + i); // Add 131 fake users
        }
        event1Applicants.add(MOCK_USER_ID); // Add our mock user
        eventApplications.put(1L, event1Applicants);

        // Event 2 (Muse) - Our mock user is NOT applied
        List<String> event2Applicants = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            event2Applicants.add("user_" + i); // Add 45 fake users
        }
        eventApplications.put(2L, event2Applicants);

        // Event 3 (Stand Up) - Our mock user is NOT applied
        List<String> event3Applicants = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            event3Applicants.add("user_" + i); // Add 12 fake users
        }
        eventApplications.put(3L, event3Applicants);
    }

    public static List<Event> getTrendingEvents() {
        return allEvents.stream()
                .filter(Event::isTrending)
                .collect(Collectors.toList());
    }

    public static List<Event> getEventsNearYou() {
        return allEvents.stream()
                .filter(event -> !event.isTrending())
                .collect(Collectors.toList());
    }

    public static Event getEventById(long id) {
        return allEvents.stream()
                .filter(event -> event.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // --- NEW: Methods to manage event applications ---

    /**
     * Gets the dynamic, real-time count of applicants for an event.
     */
    public static int getDynamicWaitlistCount(long eventId) {
        if (eventApplications.containsKey(eventId)) {
            List<String> applicants = eventApplications.get(eventId);
            if (applicants != null) {
                return applicants.size();
            }
        }
        return 0;
    }

    /**
     * Checks if a specific user is applied to a specific event.
     */
    public static boolean isUserApplied(long eventId, String userId) {
        if (eventApplications.containsKey(eventId)) {
            List<String> applicants = eventApplications.get(eventId);
            return applicants != null && applicants.contains(userId);
        }
        return false;
    }

    /**
     * Adds a user to an event's application list.
     */
    public static void applyToEvent(long eventId, String userId) {
        // Find or create the list for this event
        if (!eventApplications.containsKey(eventId)) {
            eventApplications.put(eventId, new ArrayList<>());
        }
        List<String> applicants = eventApplications.get(eventId);
        // Add user if they are not already in the list
        if (applicants != null && !applicants.contains(userId)) {
            applicants.add(userId);
        }
    }

    /**
     * Removes a user from an event's application list.
     */
    public static void withdrawFromEvent(long eventId, String userId) {
        if (eventApplications.containsKey(eventId)) {
            List<String> applicants = eventApplications.get(eventId);
            if (applicants != null) {
                applicants.remove(userId);
            }
        }
    }
}