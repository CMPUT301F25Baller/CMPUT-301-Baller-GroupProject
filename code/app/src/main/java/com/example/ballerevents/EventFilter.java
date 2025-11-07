package com.example.ballerevents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A testable, pure Java helper class to handle event filtering logic.
 */
public class EventFilter {

    /**
     * Filters a list of events based on a search query and a list of tags.
     * @param allEvents The complete list of events to filter.
     * @param query The search query string.
     * @param selectedTags A list of tags that must be present.
     * @return A new, filtered list of events.
     */
    public static List<Event> performSearchAndFilter(List<Event> allEvents, String query, List<String> selectedTags) {
        String normalizedQuery = query.toLowerCase().trim();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Use modern Java streams
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