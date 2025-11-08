package com.example.ballerevents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A testable, pure Java helper class to handle event filtering logic.
 * This class is stateless and provides a static method to perform filtering,
 * making it easy to unit test.
 */
public class EventFilter {

    /**
     * Filters a list of events based on a search query and a list of selected tags.
     * <p>
     * The query matching is case-insensitive and checks:
     * - Event Title
     * - Event Description
     * - Event Organizer
     * <p>
     * The tag matching requires the event to contain *all* selected tags.
     *
     * @param allEvents The complete list of events to filter.
     * @param query The search query string (can be empty).
     * @param selectedTags A list of tags that must be present (can be empty).
     * @return A new, filtered list of events.
     */
    public static List<Event> performSearchAndFilter(List<Event> allEvents, String query, List<String> selectedTags) {
        String normalizedQuery = query.toLowerCase().trim();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Use modern Java streams (API 24+)
            return allEvents.stream()
                    .filter(event -> {
                        // Match Search Query
                        boolean matchesQuery = normalizedQuery.isEmpty() ||
                                (event.getTitle() != null && event.getTitle().toLowerCase().contains(normalizedQuery)) ||
                                (event.getDescription() != null && event.getDescription().toLowerCase().contains(normalizedQuery)) ||
                                (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(normalizedQuery));

                        // Match Tags
                        boolean matchesTags = (selectedTags == null) || selectedTags.isEmpty() ||
                                (event.getTags() != null && event.getTags().containsAll(selectedTags));

                        return matchesQuery && matchesTags;
                    })
                    .collect(Collectors.toList());
        } else {
            // Fallback for older Android (pre-API 24)
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