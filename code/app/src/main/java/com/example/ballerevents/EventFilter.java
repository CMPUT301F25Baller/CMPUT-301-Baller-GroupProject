package com.example.ballerevents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides reusable filtering logic for {@link Event} objects.
 * <p>
 * This class is stateless, contains only static methods, and is safe to unit-test
 * because it does not rely on Android framework components or Firestore APIs.
 * </p>
 */
public class EventFilter {

    /**
     * Filters a list of events using a search query and an optional list of selected tags.
     * <p>
     * Filtering behavior:
     * <ul>
     *     <li><strong>Query matching</strong> is case-insensitive and checks the event title,
     *     description, and organizer fields.</li>
     *     <li><strong>Tag matching</strong> requires the event to contain <em>all</em> tags in
     *     {@code selectedTags}. If {@code selectedTags} is empty or null, tag filtering is skipped.</li>
     *     <li>If {@code query} is empty, all events pass the text filter.</li>
     * </ul>
     *
     * <p>This method uses Java Streams on Android API 24+ and falls back to a
     * manual loop for older devices.</p>
     *
     * @param allEvents     The complete list of events to evaluate. Must not be null.
     * @param query         The search query (may be empty or null).
     * @param selectedTags  The list of required tags (may be empty or null).
     * @return A new {@link List} containing only the events that match both filters.
     */
    public static List<Event> performSearchAndFilter(List<Event> allEvents, String query, List<String> selectedTags) {
        String normalizedQuery = query.toLowerCase().trim();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Use Java Streams when available (API 24+)
            return allEvents.stream()
                    .filter(event -> {
                        // Query Matching
                        boolean matchesQuery = normalizedQuery.isEmpty() ||
                                (event.getTitle() != null && event.getTitle().toLowerCase().contains(normalizedQuery)) ||
                                (event.getDescription() != null && event.getDescription().toLowerCase().contains(normalizedQuery)) ||
                                (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(normalizedQuery));

                        // Tag Matching
                        boolean matchesTags = (selectedTags == null) || selectedTags.isEmpty() ||
                                (event.getTags() != null && event.getTags().containsAll(selectedTags));

                        return matchesQuery && matchesTags;
                    })
                    .collect(Collectors.toList());
        } else {
            // Manual loop fallback for Android versions prior to API 24
            List<Event> filteredResults = new ArrayList<>();
            for (Event event : allEvents) {
                boolean matchesQuery = normalizedQuery.isEmpty() ||
                        (event.getTitle() != null && event.getTitle().toLowerCase().contains(normalizedQuery)) ||
                        (event.getDescription() != null && event.getDescription().toLowerCase().contains(normalizedQuery)) ||
                        (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(normalizedQuery));

                boolean matchesTags = (selectedTags == null) || selectedTags.isEmpty() ||
                        (event.getTags() != null && event.getTags().containsAll(selectedTags));

                if (matchesQuery && matchesTags) {
                    filteredResults.add(event);
                }
            }
            return filteredResults;
        }
    }
}
