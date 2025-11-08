package com.example.ballerevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.anything;

import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ballerevents.databinding.ActivityOrganizerEventCreationBinding;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for OrganizerEventCreationActivity.
 *
 * These tests verify:
 *  - Screen loads and key widgets are displayed
 *  - Clicking title/date/location opens dialogs
 *  - Required field validation prevents saving
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEventCreationActivityTest {
    @Before
    public void setup() {
        System.setProperty("IS_TESTING", "true");
    }

    @Rule
    public ActivityScenarioRule<OrganizerEventCreationActivity> rule =
            new ActivityScenarioRule<>(OrganizerEventCreationActivity.class);

    @Test
    public void screen_renders_all_required_widgets() {

        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvDateTime)).check(matches(isDisplayed()));
        onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.tvRequirements)).check(matches(isDisplayed()));
        onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));

        onView(withId(R.id.et_event_poster_url)).check(matches(isDisplayed()));
        onView(withId(R.id.et_price)).check(matches(isDisplayed()));
        onView(withId(R.id.et_tags)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_save_event)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void clicking_title_opens_edit_dialog() {
        ActivityScenario.launch(OrganizerEventCreationActivity.class);

        // Click title
        onView(withId(R.id.tvTitle)).perform(click());

        // Dialog should appear
        onView(withText("Edit Title"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void clicking_dateTime_opens_date_picker() {
        ActivityScenario.launch(OrganizerEventCreationActivity.class);

        onView(withId(R.id.tvDateTime)).perform(click());

        // DatePickerDialog should show
        onView(withClassName(org.hamcrest.Matchers.containsString("DatePicker")))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void clicking_location_opens_location_dialog() {
        ActivityScenario.launch(OrganizerEventCreationActivity.class);

        onView(withId(R.id.tvLocation)).perform(click());

        // Custom dialog should show
        onView(withText("Edit Location"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void saveEvent_shows_errors_when_required_fields_empty() {
        ActivityScenario.launch(OrganizerEventCreationActivity.class);

        // Clear required fields
        onView(withId(R.id.et_event_poster_url)).perform(clearText());
        onView(withId(R.id.et_price)).perform(clearText());

        onView(withId(R.id.btn_save_event)).perform(click());

        // We expect Toast messages — Espresso can't assert toasts easily,
        // but we can assert the activity does NOT close.
        onView(withId(R.id.btn_save_event)).check(matches(isDisplayed()));
    }


    @Test
    public void editDateTime_selects_new_date() {
        ActivityScenario.launch(OrganizerEventCreationActivity.class);

        onView(withId(R.id.tvDateTime)).perform(click());

        // Set date: 2025-10-10
        onView(withClassName(org.hamcrest.Matchers.containsString("DatePicker")))
                .perform(PickerActions.setDate(2025, 10, 10));

        onView(withText("OK")).perform(click());
    }

}
