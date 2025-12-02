package com.example.ballerevents;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Provides reusable filtering logic for {@link Event} objects.
 * <p>
 * This class filters events based on:
 * <ul>
 * <li>Text Search (Title, Description, Organizer)</li>
 * <li>Tags/Categories</li>
 * <li>Date Range (Availability)</li>
 * </ul>
 */
public class EventFilter {

    private static final String TAG = "EventFilter";
    // Adjust format to match your Event date string format (e.g., "dd MMMM, yyyy" or "yyyy-MM-dd")
    // Based on your previous screenshots ("05 December, 2025"), the format is likely:
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMM, yyyy", Locale.US);

    /**
     * Filters a list of events using search query, tags, and date range.
     *
     * @param allEvents     The complete list of events.
     * @param query         The search query (case-insensitive).
     * @param selectedTags  The list of required tags.
     * @param startDate     The start of the availability range (null if ignored).
     * @param endDate       The end of the availability range (null if ignored).
     * @return A list of matching events.
     */
    public static List<Event> performSearchAndFilter(List<Event> allEvents,
                                                     String query,
                                                     List<String> selectedTags,
                                                     Date startDate,
                                                     Date endDate) {

        String normalizedQuery = (query != null) ? query.toLowerCase().trim() : "";
        List<Event> filteredResults = new ArrayList<>();

        for (Event event : allEvents) {
            // 1. Text Matching
            boolean matchesQuery = normalizedQuery.isEmpty() ||
                    (event.getTitle() != null && event.getTitle().toLowerCase().contains(normalizedQuery)) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(normalizedQuery)) ||
                    (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(normalizedQuery));

            // 2. Tag Matching
            boolean matchesTags = (selectedTags == null) || selectedTags.isEmpty() ||
                    (event.getTags() != null && event.getTags().containsAll(selectedTags));

            // 3. Date Matching
            boolean matchesDate = true;
            if (startDate != null && endDate != null && event.getDate() != null) {
                try {
                    // Parse the event's date string into a Date object
                    Date eventDate = DATE_FORMAT.parse(event.getDate());
                    if (eventDate != null) {
                        // Check if eventDate is within range [startDate, endDate]
                        matchesDate = !eventDate.before(startDate) && !eventDate.after(endDate);
                    }
                } catch (ParseException e) {
                    Log.w(TAG, "Failed to parse event date: " + event.getDate());
                    // Decide fail-safe: include or exclude? Let's exclude if date is invalid.
                    matchesDate = false;
                }
            }

            if (matchesQuery && matchesTags && matchesDate) {
                filteredResults.add(event);
            }
        }
        return filteredResults;
    }
}