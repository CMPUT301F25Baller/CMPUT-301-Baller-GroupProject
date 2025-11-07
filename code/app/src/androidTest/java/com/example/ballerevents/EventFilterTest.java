package com.example.ballerevents;

// Import testing libraries
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for the EventFilter helper class.
 * This covers User Story 5: "As an entrant, I want to filter events..."
 */
public class EventFilterTest {

    private List<Event> mockEvents;

    // Helper method to create a mock event
    private Event createMockEvent(String title, String organizer, String description, List<String> tags) {
        Event event = new Event();
        event.title = title;
        event.organizer = organizer;
        event.description = description;
        event.tags = tags;
        return event;
    }

    // @Before runs before each test
    @Before
    public void setUp() {
        // Create a mock dataset for testing
        mockEvents = new ArrayList<>();
        mockEvents.add(createMockEvent("Pop Concert", "Music Inc", "A fun concert", Arrays.asList("Music", "Pop")));
        mockEvents.add(createMockEvent("Art Show", "Gallery", "Modern art", Arrays.asList("Art", "Exhibition")));
        mockEvents.add(createMockEvent("Rock Fest", "Music Inc", "A loud concert", Arrays.asList("Music", "Rock")));
    }

    /**
     * Test for User Story 5: Filtering by a single tag.
     */
    @Test
    public void test_performSearchAndFilter_bySingleTag() {
        List<String> selectedTags = Arrays.asList("Music");
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, "", selectedTags);

        // We expect 2 events: "Pop Concert" and "Rock Fest"
        assertEquals(2, results.size());
        assertEquals("Pop Concert", results.get(0).getTitle());
        assertEquals("Rock Fest", results.get(1).getTitle());
    }

    /**
     * Test for User Story 5: Filtering by multiple tags.
     */
    @Test
    public void test_performSearchAndFilter_byMultipleTags() {
        List<String> selectedTags = Arrays.asList("Music", "Pop");
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, "", selectedTags);

        // We expect only 1 event: "Pop Concert"
        assertEquals(1, results.size());
        assertEquals("Pop Concert", results.get(0).getTitle());
    }

    /**
     * Test for User Story 5: Filtering by search query (title).
     */
    @Test
    public void test_performSearchAndFilter_byQuery_Title() {
        List<String> selectedTags = new ArrayList<>();
        String query = "Art"; // Should match "Art Show"
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, query, selectedTags);

        assertEquals(1, results.size());
        assertEquals("Art Show", results.get(0).getTitle());
    }

    /**
     * Test for User Story 5: Filtering by search query (organizer).
     */
    @Test
    public void test_performSearchAndFilter_byQuery_Organizer() {
        List<String> selectedTags = new ArrayList<>();
        String query = "Music Inc"; // Should match both concerts
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, query, selectedTags);

        assertEquals(2, results.size());
    }

    /**
     * Test for User Story 5: Filtering by both query AND tag.
     */
    @Test
    public void test_performSearchAndFilter_byQueryAndTag() {
        List<String> selectedTags = Arrays.asList("Music");
        String query = "Pop"; // Should match "Pop Concert"
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, query, selectedTags);

        // Should find only the "Pop Concert"
        assertEquals(1, results.size());
        assertEquals("Pop Concert", results.get(0).getTitle());
    }

    /**
     * Test for User Story 5: No results found.
     */
    @Test
    public void test_performSearchAndFilter_noResults() {
        List<String> selectedTags = Arrays.asList("Theater"); // No events have this tag
        List<Event> results = EventFilter.performSearchAndFilter(mockEvents, "", selectedTags);

        assertEquals(0, results.size());
    }
}