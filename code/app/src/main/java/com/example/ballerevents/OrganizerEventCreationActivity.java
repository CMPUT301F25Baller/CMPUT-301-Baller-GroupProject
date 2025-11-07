package com.example.ballerevents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Import ViewBinding and other necessary classes
import com.example.ballerevents.databinding.ActivityOrganizerEventCreationBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Activity that allows organizers to create and publish new events.
 * <p>
 * This screen supports editing all event metadata (title, date/time, venue,
 * description, requirements, price, tags, and poster URL). After validation,
 * the event is uploaded to Firestore's <code>events</code> collection.
 * <p>
 * The activity also loads the organizer’s profile so the created event can
 * include organizer name and profile picture.
 */
public class OrganizerEventCreationActivity extends AppCompatActivity {

    private static final String TAG = "EventCreationActivity";

    // Use ViewBinding
    private ActivityOrganizerEventCreationBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private UserProfile organizerProfile; // To store organizer's info

    // --- All your data fields ---
    private String title = "International Band Music Concert";
    private String dateStr = "14 December, 2021";
    private String dayStr = "Tuesday";
    private String fromTime = "4:00PM";
    private String toTime = "9:00PM";
    private String venue = "Gala Convention Center";
    private String address = "36 Guild Street London, UK";
    private String requirements = "Event requirements";
    private String description = "Add description";

    private Calendar eventDate = Calendar.getInstance();
    private Calendar fromCal = Calendar.getInstance();
    private Calendar toCal = Calendar.getInstance();

    /**
     * Initializes ViewBinding, loads organizer profile information, and wires
     * up all UI event listeners for editing and submitting an event.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout with ViewBinding
        binding = ActivityOrganizerEventCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Load the organizer's profile to get their name/icon
        loadOrganizerProfile();

        // --- Wire up all click listeners ---
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveEvent.setOnClickListener(v -> saveEventToFirestore());

        binding.tvTitle.setOnClickListener(v -> editTitle());
        binding.tvDateTime.setOnClickListener(v -> editDateTime());
        binding.tvLocation.setOnClickListener(v -> editLocation());

        binding.tvRequirements.setOnClickListener(v -> editMultiline("Requirements", requirements, newValue -> {
            requirements = newValue;
            updateDisplays();
        }));

        binding.tvDescription.setOnClickListener(v -> editMultiline("Description", description, newValue -> {
            description = newValue;
            updateDisplays();
        }));

        updateDisplays(); // Set initial text
    }

    /**
     * Fetches the current user's (organizer's) profile from Firestore.
     */
    private void loadOrganizerProfile() {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                organizerProfile = documentSnapshot.toObject(UserProfile.class);
            } else {
                Toast.makeText(this, "Could not find organizer profile.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Organizer profile not found for ID: " + currentUserId);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching organizer data.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error fetching user profile", e);
        });
    }

    /**
     * Validates and saves the new event to the "events" collection in Firestore.
     */
    private void saveEventToFirestore() {
        if (!validate() || currentUserId == null) {
            if (currentUserId == null) Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (organizerProfile == null) {
            Toast.makeText(this, "Error: Organizer profile not loaded. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Get data from the new EditText fields ---
        // (These IDs are assumed to be in your XML layout)
        String eventPosterUrl = binding.etEventPosterUrl.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String tagsString = binding.etTags.getText().toString().trim();

        // Convert comma-separated string to a List
        List<String> tagsList;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tagsList = Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            tagsList = new java.util.ArrayList<>();
            for (String s : tagsString.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    tagsList.add(trimmed);
                }
            }
        }

        // --- Create a new Event object using the Firebase-ready model ---
        Event newEvent = new Event();
        newEvent.title = this.title;
        newEvent.date = this.dateStr;
        newEvent.time = this.fromTime + " - " + this.toTime;
        newEvent.locationName = this.venue;
        newEvent.locationAddress = this.address;
        newEvent.description = this.description;

        // Add new fields
        newEvent.price = price;
        newEvent.tags = tagsList;
        newEvent.eventPosterUrl = eventPosterUrl;

        // Add organizer info from their profile
        newEvent.organizerId = this.currentUserId;
        newEvent.organizer = organizerProfile.getName();
        newEvent.organizerIconUrl = organizerProfile.getProfilePictureUrl();
        newEvent.isTrending = false; // Default for new events

        db.collection("events")
                .add(newEvent)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Event saved with ID: " + documentReference.getId());
                    finish(); // Go back to the previous activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);
                });
    }

    /**
     * Updates all on-screen text fields with the latest event values.
     */
    private void updateDisplays() {
        binding.tvTitle.setText(title);
        binding.tvDateTime.setText(String.format("%s, %s, %s - %s", dateStr, dayStr, fromTime, toTime));
        binding.tvLocation.setText(String.format("%s, %s", venue, address));
        binding.tvRequirements.setText(TextUtils.isEmpty(requirements) ? "Event requirements" : requirements);
        binding.tvDescription.setText(TextUtils.isEmpty(description) ? "Add description" : description);
    }

    /**
     * Opens a dialog that allows the user to edit the event title.
     */
    private void editTitle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Title");
        final EditText input = new EditText(this);
        input.setText(title);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            title = input.getText().toString().trim();
            binding.tvTitle.setText(title);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Opens sequential date and time pickers to edit the event's date,
     * start time, and end time.
     */
    private void editDateTime() {
        // First pick date
        new DatePickerDialog(this, (view, y, m, d) -> {
            eventDate.set(y, m, d);
            dateStr = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH).format(eventDate.getTime());
            dayStr = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(eventDate.getTime());

            // Then pick from time
            new TimePickerDialog(this, (v, h, min) -> {
                fromCal.set(Calendar.HOUR_OF_DAY, h);
                fromCal.set(Calendar.MINUTE, min);
                fromTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(fromCal.getTime());

                // Then pick to time
                new TimePickerDialog(this, (vv, hh, mm) -> {
                    toCal.set(Calendar.HOUR_OF_DAY, hh);
                    toCal.set(Calendar.MINUTE, mm);
                    toTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(toCal.getTime());
                    updateDisplays();
                }, toCal.get(Calendar.HOUR_OF_DAY), toCal.get(Calendar.MINUTE), false).show();
            }, fromCal.get(Calendar.HOUR_OF_DAY), fromCal.get(Calendar.MINUTE), false).show();
        }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Opens a dialog for editing the venue and address fields.
     */
    private void editLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Location");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_location, null);
        EditText etVenue = view.findViewById(R.id.etVenue); // Assuming this ID is in dialog_edit_location.xml
        EditText etAddress = view.findViewById(R.id.etAddress); // Assuming this ID is in dialog_edit_location.xml
        etVenue.setText(venue);
        etAddress.setText(address);
        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> {
            venue = etVenue.getText().toString().trim();
            address = etAddress.getText().toString().trim();
            updateDisplays();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Shows a multiline edit dialog for fields such as description or requirements.
     *
     * @param title    dialog title
     * @param current  the existing value
     * @param callback callback that receives the updated string
     */
    private void editMultiline(String title, String current, ValueCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        final EditText input = new EditText(this);
        input.setText(current);
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            callback.onValue(newValue);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Functional callback interface used when editing multi-line text fields.
     */
    private interface ValueCallback {
        void onValue(String newValue);
    }

    /**
     * Validates all event fields before upload.
     *
     * @return true if all required fields are valid, false otherwise
     */
    private boolean validate() {
        boolean ok = true;
        if (TextUtils.isEmpty(title)) { Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(dateStr)) { Toast.makeText(this, "Date required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(fromTime)) { Toast.makeText(this, "From time required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(toTime)) { Toast.makeText(this, "To time required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(venue)) { Toast.makeText(this, "Venue required", Toast.LENGTH_SHORT).show(); ok = false; }

        // --- Add validation for new fields ---
        if (TextUtils.isEmpty(binding.etEventPosterUrl.getText())) { Toast.makeText(this, "Event Poster URL required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(binding.etPrice.getText())) { Toast.makeText(this, "Price required", Toast.LENGTH_SHORT).show(); ok = false; }

        return ok;
    }
}