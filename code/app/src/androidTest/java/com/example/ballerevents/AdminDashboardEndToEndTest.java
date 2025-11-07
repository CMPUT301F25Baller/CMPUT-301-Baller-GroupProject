package com.example.ballerevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Single UI test file that verifies the Half-Way admin dashboard:
 *  - Screen renders (3 lists + 4 chips visible)
 *  - Lists are HORIZONTAL
 *  - Lists have >= 1 item (stubbed data)
 *  - Chips are clickable (smoke: no crash)
 */
@RunWith(AndroidJUnit4.class)
public class AdminDashboardEndToEndTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> rule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    @Test
    public void dashboard_renders_listsHorizontal_haveItems_and_chipsClickable() {
        // 1) Visible widgets
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.rvProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.rvImages)).check(matches(isDisplayed()));
        onView(withId(R.id.chipEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.chipPeople)).check(matches(isDisplayed()));
        onView(withId(R.id.chipImages)).check(matches(isDisplayed()));
        onView(withId(R.id.chipLogs)).check(matches(isDisplayed()));

        // 2) Orientation + item count checks (run on the Activity thread)
        rule.getScenario().onActivity(activity -> {
            assertHorizontal(activity.findViewById(R.id.rvEvents));
            assertHorizontal(activity.findViewById(R.id.rvProfiles));
            assertHorizontal(activity.findViewById(R.id.rvImages));

            assertHasAtLeast(activity.findViewById(R.id.rvEvents), 1);
            assertHasAtLeast(activity.findViewById(R.id.rvProfiles), 1);
            assertHasAtLeast(activity.findViewById(R.id.rvImages), 1);
        });

        // 3) Chip click smoke (no crash)
        onView(withId(R.id.chipEvents)).perform(click());
        onView(withId(R.id.chipPeople)).perform(click());
        onView(withId(R.id.chipImages)).perform(click());
        onView(withId(R.id.chipLogs)).perform(click());
    }

    // ---- helpers ----
    private static void assertHorizontal(RecyclerView rv) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) {
            throw new AssertionError("Expected LinearLayoutManager, got " + (lm == null ? "null" : lm.getClass()));
        }
        int orientation = ((LinearLayoutManager) lm).getOrientation();
        if (orientation != LinearLayoutManager.HORIZONTAL) {
            throw new AssertionError("Expected HORIZONTAL orientation");
        }
    }

    private static void assertHasAtLeast(RecyclerView rv, int min) {
        RecyclerView.Adapter<?> a = rv.getAdapter();
        if (a == null) throw new AssertionError("Adapter is null");
        if (a.getItemCount() < min) {
            throw new AssertionError("Expected at least " + min + " items, but was " + a.getItemCount());
        }
    }
}
