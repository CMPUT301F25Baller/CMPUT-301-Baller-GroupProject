package com.example.ballerevents;

import com.example.ballerevents.Event;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventRepository {

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
}