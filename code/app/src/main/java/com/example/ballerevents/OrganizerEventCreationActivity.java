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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityOrganizerEventCreationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity allowing organizers to create a new event or edit an existing one.
 * <p>
 * Behavior depends on whether {@link #EXTRA_EVENT_ID} is included in the launching intent:
 * <ul>
 *     <li><b>Create Mode:</b> No existing event ID is passed. All fields start blank.</li>
 *     <li><b>Edit Mode:</b> An event ID is provided. The activity loads the existing
 *         event from Firestore and pre-populates all fields for editing.</li>
 * </ul>
 *
 * <p>The activity manages:
 * <ul>
 *     <li>Date selection</li>
 *     <li>Time selection</li>
 *     <li>Location input</li>
 *     <li>Tag parsing</li>
 *     <li>Poster and banner image picking</li>
 *     <li>Registration period (open / close)</li>
 *     <li>Save/Update operations to Firestore</li>
 * </ul>
 * </p>
 */
public class OrganizerEventCreationActivity extends AppCompatActivity {

    /** Logging tag. */
    private static final String TAG = "EventCreationActivity";

    /** Intent key for passing an event ID for edit mode. */
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    /** Date-time format used for displaying registration period. */
    private static final SimpleDateFormat REGISTRATION_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH);

    /** ViewBinding for all views in the layout. */
    private ActivityOrganizerEventCreationBinding binding;

    /** Firestore reference. */
    private FirebaseFirestore db;

    /** FirebaseAuth for retrieving the current user. */
    private FirebaseAuth mAuth;

    /** ID of currently authenticated organizer. */
    private String currentUserId;

    /** Organizer profile loaded from Firestore. */
    private UserProfile organizerProfile;

    /** If non-null, the activity is editing an existing event. */
    private String eventIdToEdit = null;

    // -------------------------------------------------------------------------
    // Cached form values
    // -------------------------------------------------------------------------

    private String dateStr = "";
    private String fromTime = "";
    private String toTime = "";
    private String venue = "";
    private String address = "";

    /** URIs for banner or poster images (stored as strings). */
    private String bannerUriString = "";
    private String posterUriString = "";

    /** Calendar instances for date/time picking. */
    private final Calendar eventDate = Calendar.getInstance();
    private final Calendar fromCal = Calendar.getInstance();
    private final Calendar toCal = Calendar.getInstance();

    /** Registration period calendars and state. */
    private final Calendar registrationOpenCal = Calendar.getInstance();
    private final Calendar registrationCloseCal = Calendar.getInstance();
    private boolean hasRegistrationOpen = false;
    private boolean hasRegistrationClose = false;

    private static final String[] PRESET_TAGS = new String[]{
            "Music Concert",
            "Exhibition",
            "Stand Up Show",
            "Theater"
    };

    // For multi-select dialog state
    private final boolean[] presetTagChecked = new boolean[PRESET_TAGS.length];

    // Final selected tags (saved to Firestore)
    private final java.util.List<String> selectedTags = new java.util.ArrayList<>();

    /** ActivityResultLaunchers for picking images. */
    private ActivityResultLauncher<String> bannerPickerLauncher;
    private ActivityResultLauncher<String> posterPickerLauncher;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerEventCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Check edit/create mode
        if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventIdToEdit = getIntent().getStringExtra(EXTRA_EVENT_ID);
            binding.btnSaveEvent.setText("Update Event");
            loadEventData(eventIdToEdit);
        }

        loadOrganizerProfile();
        setupImagePickers();
        setupClickListeners();
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {

                        binding.etEventTitle.setText(event.getTitle());
                        binding.etDescription.setText(event.getDescription());
                        binding.etPrice.setText(event.getPrice());

                        venue = event.getLocationName();
                        address = event.getLocationAddress();
                        binding.tvLocationPicker.setText(venue);

                        if (!TextUtils.isEmpty(address)) {
                            binding.tvAddressPicker.setText(address);
                            binding.tvAddressPicker.setVisibility(View.VISIBLE);
                        }

                        dateStr = event.getDate();
                        binding.tvDatePicker.setText(dateStr);

                        String fullTime = event.getTime();
                        binding.tvTimePicker.setText(fullTime);

                        if (fullTime != null && fullTime.contains(" - ")) {
                            String[] parts = fullTime.split(" - ");
                            fromTime = parts[0];
                            toTime = parts[1];
                        } else {
                            fromTime = fullTime;
                        }

                        selectedTags.clear();
                        Arrays.fill(presetTagChecked, false);

                        if (event.getTags() != null && !event.getTags().isEmpty()) {
                            selectedTags.addAll(event.getTags());
                            for (int i = 0; i < PRESET_TAGS.length; i++) {
                                if (selectedTags.contains(PRESET_TAGS[i])) {
                                    presetTagChecked[i] = true;
                                }
                            }
                            binding.etTags.setText(joinTags(selectedTags));
                        } else {
                            binding.etTags.setText("");
                        }

                        posterUriString = event.getEventPosterUrl();
                        if (!TextUtils.isEmpty(posterUriString)) {
                            binding.ivPosterImage.setScaleType(
                                    android.widget.ImageView.ScaleType.CENTER_CROP);
                            Glide.with(this).load(posterUriString).into(binding.ivPosterImage);
                        }

                        if (!TextUtils.isEmpty(posterUriString)) {
                            Glide.with(this).load(posterUriString).centerCrop()
                                    .into(binding.ivEventBanner);
                        }

                        long openMillis = event.getRegistrationOpenAtMillis();
                        long closeMillis = event.getRegistrationCloseAtMillis();

                        if (openMillis > 0) {
                            hasRegistrationOpen = true;
                            registrationOpenCal.setTimeInMillis(openMillis);
                            binding.tvRegistrationOpenPicker.setText(
                                    REGISTRATION_FORMAT.format(registrationOpenCal.getTime())
                            );
                        }

                        if (closeMillis > 0) {
                            hasRegistrationClose = true;
                            registrationCloseCal.setTimeInMillis(closeMillis);
                            binding.tvRegistrationClosePicker.setText(
                                    REGISTRATION_FORMAT.format(registrationCloseCal.getTime())
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event data", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error fetching event", e);
                    finish();
                });
    }

    private void loadOrganizerProfile() {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                organizerProfile = documentSnapshot.toObject(UserProfile.class);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Image pickers
    // -------------------------------------------------------------------------

    private void setupImagePickers() {
        bannerPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        bannerUriString = uri.toString();
                        Glide.with(this)
                                .load(uri)
                                .centerCrop()
                                .into(binding.ivEventBanner);
                    }
                });

        posterPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        posterUriString = uri.toString();
                        binding.ivPosterImage.setScaleType(
                                android.widget.ImageView.ScaleType.CENTER_CROP);
                        Glide.with(this).load(uri).into(binding.ivPosterImage);
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Click listeners
    // -------------------------------------------------------------------------

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveEvent.setOnClickListener(v -> saveEventToFirestore());

        binding.tvDatePicker.setOnClickListener(v -> pickDate());
        binding.tvTimePicker.setOnClickListener(v -> pickTime());
        binding.tvLocationPicker.setOnClickListener(v -> pickLocation());

        binding.viewBannerClickArea.setOnClickListener(
                v -> bannerPickerLauncher.launch("image/*"));

        binding.ivPosterImage.setOnClickListener(
                v -> posterPickerLauncher.launch("image/*"));

        binding.etTags.setKeyListener(null);
        binding.etTags.setFocusable(false);
        binding.etTags.setOnClickListener(v -> showTagsDialog());

        binding.tvRegistrationOpenPicker.setOnClickListener(v -> pickRegistrationOpen());
        binding.tvRegistrationClosePicker.setOnClickListener(v -> pickRegistrationClose());
    }

    // -------------------------------------------------------------------------
    // Pickers
    // -------------------------------------------------------------------------

    private void pickDate() {
        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    eventDate.set(y, m, d);
                    dateStr = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
                            .format(eventDate.getTime());
                    binding.tvDatePicker.setText(dateStr);
                },
                eventDate.get(Calendar.YEAR),
                eventDate.get(Calendar.MONTH),
                eventDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime() {
        new TimePickerDialog(
                this,
                (v, h, min) -> {
                    fromCal.set(Calendar.HOUR_OF_DAY, h);
                    fromCal.set(Calendar.MINUTE, min);
                    fromTime = DateFormat.getTimeInstance(DateFormat.SHORT)
                            .format(fromCal.getTime());

                    new TimePickerDialog(
                            this,
                            (vv, hh, mm) -> {
                                toCal.set(Calendar.HOUR_OF_DAY, hh);
                                toCal.set(Calendar.MINUTE, mm);
                                toTime = DateFormat.getTimeInstance(DateFormat.SHORT)
                                        .format(toCal.getTime());
                                binding.tvTimePicker.setText(fromTime + " - " + toTime);
                            },
                            toCal.get(Calendar.HOUR_OF_DAY),
                            toCal.get(Calendar.MINUTE),
                            false
                    ).show();
                },
                fromCal.get(Calendar.HOUR_OF_DAY),
                fromCal.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void pickLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Location");

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_location, null);
        EditText etVenue = view.findViewById(R.id.etVenue);
        EditText etAddress = view.findViewById(R.id.etAddress);

        etVenue.setText(venue);
        etAddress.setText(address);

        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> {
            venue = etVenue.getText().toString().trim();
            address = etAddress.getText().toString().trim();
            binding.tvLocationPicker.setText(venue);

            if (!address.isEmpty()) {
                binding.tvAddressPicker.setText(address);
                binding.tvAddressPicker.setVisibility(View.VISIBLE);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void pickRegistrationOpen() {
        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    registrationOpenCal.set(y, m, d);

                    new TimePickerDialog(
                            this,
                            (timeView, h, min) -> {
                                registrationOpenCal.set(Calendar.HOUR_OF_DAY, h);
                                registrationOpenCal.set(Calendar.MINUTE, min);
                                registrationOpenCal.set(Calendar.SECOND, 0);
                                registrationOpenCal.set(Calendar.MILLISECOND, 0);

                                hasRegistrationOpen = true;
                                binding.tvRegistrationOpenPicker.setText(
                                        REGISTRATION_FORMAT.format(registrationOpenCal.getTime())
                                );

                                if (hasRegistrationClose &&
                                        registrationCloseCal.getTimeInMillis()
                                                <= registrationOpenCal.getTimeInMillis()) {
                                    hasRegistrationClose = false;
                                    registrationCloseCal.setTimeInMillis(0L);
                                    binding.tvRegistrationClosePicker
                                            .setText("Select registration end");
                                }
                            },
                            registrationOpenCal.get(Calendar.HOUR_OF_DAY),
                            registrationOpenCal.get(Calendar.MINUTE),
                            false
                    ).show();
                },
                registrationOpenCal.get(Calendar.YEAR),
                registrationOpenCal.get(Calendar.MONTH),
                registrationOpenCal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickRegistrationClose() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    registrationCloseCal.set(y, m, d);

                    new TimePickerDialog(
                            this,
                            (timeView, h, min) -> {
                                registrationCloseCal.set(Calendar.HOUR_OF_DAY, h);
                                registrationCloseCal.set(Calendar.MINUTE, min);
                                registrationCloseCal.set(Calendar.SECOND, 0);
                                registrationCloseCal.set(Calendar.MILLISECOND, 0);

                                if (hasRegistrationOpen &&
                                        registrationCloseCal.getTimeInMillis()
                                                <= registrationOpenCal.getTimeInMillis()) {
                                    Toast.makeText(
                                            this,
                                            "End must be after the start time.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    registrationCloseCal.setTimeInMillis(
                                            registrationOpenCal.getTimeInMillis() + 60 * 60 * 1000L
                                    );
                                }

                                hasRegistrationClose = true;
                                binding.tvRegistrationClosePicker.setText(
                                        REGISTRATION_FORMAT.format(registrationCloseCal.getTime())
                                );
                            },
                            registrationCloseCal.get(Calendar.HOUR_OF_DAY),
                            registrationCloseCal.get(Calendar.MINUTE),
                            false
                    ).show();
                },
                registrationCloseCal.get(Calendar.YEAR),
                registrationCloseCal.get(Calendar.MONTH),
                registrationCloseCal.get(Calendar.DAY_OF_MONTH)
        );

        if (hasRegistrationOpen) {
            datePickerDialog.getDatePicker()
                    .setMinDate(registrationOpenCal.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    private void showTagsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Tags");

        builder.setMultiChoiceItems(PRESET_TAGS, presetTagChecked, (dialog, which, isChecked) -> {
            String tag = PRESET_TAGS[which];
            if (isChecked) {
                if (!selectedTags.contains(tag)) {
                    selectedTags.add(tag);
                }
            } else {
                selectedTags.remove(tag);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            binding.etTags.setText(joinTags(selectedTags));
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String joinTags(java.util.List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String t : tags) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(t);
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    private void saveEventToFirestore() {
        String title = binding.etEventTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(dateStr)) {
            Toast.makeText(this, "Date required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(venue)) {
            Toast.makeText(this, "Venue required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Price required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasRegistrationOpen ^ hasRegistrationClose) {
            Toast.makeText(this,
                    "Please set both registration start and end, or leave both empty.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasRegistrationOpen && hasRegistrationClose &&
                registrationCloseCal.getTimeInMillis()
                        < registrationOpenCal.getTimeInMillis()) {
            Toast.makeText(this,
                    "Registration end must be after registration start.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.List<String> tagsList = new java.util.ArrayList<>(selectedTags);

        Event eventData = new Event();
        eventData.title = title;
        eventData.date = dateStr;

        String timeString = (fromTime.isEmpty() || toTime.isEmpty())
                ? binding.tvTimePicker.getText().toString()
                : fromTime + " - " + toTime;
        eventData.time = timeString;

        eventData.locationName = venue;
        eventData.locationAddress = address;
        eventData.description = description;
        eventData.price = price;
        eventData.tags = tagsList;

        if (!posterUriString.isEmpty()) {
            eventData.eventPosterUrl = posterUriString;
        }

        eventData.organizerId = currentUserId;

        if (organizerProfile != null) {
            eventData.organizer = organizerProfile.getName();
            eventData.organizerIconUrl = organizerProfile.getProfilePictureUrl();
        }

        eventData.registrationOpenAtMillis =
                hasRegistrationOpen ? registrationOpenCal.getTimeInMillis() : 0L;
        eventData.registrationCloseAtMillis =
                hasRegistrationClose ? registrationCloseCal.getTimeInMillis() : 0L;

        eventData.isTrending = false;

        if (eventIdToEdit != null) {
            db.collection("events").document(eventIdToEdit)
                    .set(eventData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating event", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error updating event", e);
                    });
        } else {
            db.collection("events").add(eventData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error creating event", e);
                    });
        }
    }
}
