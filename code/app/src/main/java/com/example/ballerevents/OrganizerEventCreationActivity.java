package com.example.ballerevents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Activity that allows organizers to create NEW events or EDIT existing ones.
 * <p>
 * If an EXTRA_EVENT_ID is passed in the intent, this activity enters "Edit Mode":
 * - Loads the existing event data.
 * - Populates all input fields.
 * - Updates the existing Firestore document on save.
 * <p>
 * Otherwise, it enters "Create Mode" (default behavior).
 */
public class OrganizerEventCreationActivity extends AppCompatActivity {

    private static final String TAG = "EventCreationActivity";
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID"; // Key for Intent

    private ActivityOrganizerEventCreationBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private UserProfile organizerProfile;

    // If this is not null, we are in "Edit Mode"
    private String eventIdToEdit = null;

    // Data fields
    private String dateStr = "";
    private String fromTime = "";
    private String toTime = "";
    private String venue = "";
    private String address = "";

    // Image URIs (stored as strings)
    private String bannerUriString = "";
    private String posterUriString = "";

    private Calendar eventDate = Calendar.getInstance();
    private Calendar fromCal = Calendar.getInstance();
    private Calendar toCal = Calendar.getInstance();

    // Image Pickers
    private ActivityResultLauncher<String> bannerPickerLauncher;
    private ActivityResultLauncher<String> posterPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Check if we are editing an existing event
        if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventIdToEdit = getIntent().getStringExtra(EXTRA_EVENT_ID);
            binding.btnSaveEvent.setText("Update Event"); // Change button text
            // We might also want to change the header text
            // binding.tvHeaderTitle.setText("Edit Event");
            loadEventData(eventIdToEdit);
        }

        loadOrganizerProfile();
        setupImagePickers();
        setupClickListeners();
    }

    /**
     * Loads existing event data from Firestore and populates the UI fields.
     */
    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        // Populate text fields
                        binding.etEventTitle.setText(event.getTitle());
                        binding.etDescription.setText(event.getDescription());
                        binding.etPrice.setText(event.getPrice());

                        // Populate location
                        venue = event.getLocationName();
                        address = event.getLocationAddress();
                        binding.tvLocationPicker.setText(venue);
                        if (!TextUtils.isEmpty(address)) {
                            binding.tvAddressPicker.setText(address);
                            binding.tvAddressPicker.setVisibility(View.VISIBLE);
                        }

                        // Populate Date & Time
                        dateStr = event.getDate();
                        binding.tvDatePicker.setText(dateStr);
                        // Time format in DB is "start - end". We populate the full string.
                        // Splitting it back to fromTime/toTime is complex without stricter formatting,
                        // so we'll just display it and let the user overwrite if they pick new times.
                        String fullTime = event.getTime();
                        binding.tvTimePicker.setText(fullTime);
                        // Try to parse simple " - " split for state
                        if (fullTime != null && fullTime.contains(" - ")) {
                            String[] parts = fullTime.split(" - ");
                            fromTime = parts[0];
                            toTime = parts[1];
                        } else {
                            fromTime = fullTime; // Fallback
                        }

                        // Populate Tags
                        if (event.getTags() != null && !event.getTags().isEmpty()) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                binding.etTags.setText(String.join(", ", event.getTags()));
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for(String tag : event.getTags()) {
                                    if(sb.length() > 0) sb.append(", ");
                                    sb.append(tag);
                                }
                                binding.etTags.setText(sb.toString());
                            }
                        }

                        // Populate Images
                        posterUriString = event.getEventPosterUrl();
                        if (!TextUtils.isEmpty(posterUriString)) {
                            binding.ivPosterImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                            Glide.with(this).load(posterUriString).into(binding.ivPosterImage);
                        }
                        // Banner URL is not currently in Event model (using poster for both in save logic)
                        // If you added a bannerUrl field, load it here.
                        if (!TextUtils.isEmpty(posterUriString)) {
                            Glide.with(this).load(posterUriString).centerCrop().into(binding.ivEventBanner);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event data", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error fetching event", e);
                    finish();
                });
    }

    private void setupImagePickers() {
        // Banner Picker
        bannerPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                bannerUriString = uri.toString();
                Glide.with(this).load(uri).centerCrop().into(binding.ivEventBanner);
            }
        });

        // Poster Picker
        posterPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                posterUriString = uri.toString();
                binding.ivPosterImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                Glide.with(this).load(uri).into(binding.ivPosterImage);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveEvent.setOnClickListener(v -> saveEventToFirestore());

        // Date & Time
        binding.tvDatePicker.setOnClickListener(v -> pickDate());
        binding.tvTimePicker.setOnClickListener(v -> pickTime());

        // Location
        binding.tvLocationPicker.setOnClickListener(v -> pickLocation());

        // Images
        // Use the new view_banner_click_area because ivEventBanner is behind the scrollview
        binding.viewBannerClickArea.setOnClickListener(v -> bannerPickerLauncher.launch("image/*"));
        binding.ivPosterImage.setOnClickListener(v -> posterPickerLauncher.launch("image/*"));
    }

    private void pickDate() {
        new DatePickerDialog(this, (view, y, m, d) -> {
            eventDate.set(y, m, d);
            dateStr = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH).format(eventDate.getTime());
            binding.tvDatePicker.setText(dateStr);
        }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        new TimePickerDialog(this, (v, h, min) -> {
            fromCal.set(Calendar.HOUR_OF_DAY, h);
            fromCal.set(Calendar.MINUTE, min);
            fromTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(fromCal.getTime());

            // Pick End Time immediately after
            new TimePickerDialog(this, (vv, hh, mm) -> {
                toCal.set(Calendar.HOUR_OF_DAY, hh);
                toCal.set(Calendar.MINUTE, mm);
                toTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(toCal.getTime());
                binding.tvTimePicker.setText(fromTime + " - " + toTime);
            }, toCal.get(Calendar.HOUR_OF_DAY), toCal.get(Calendar.MINUTE), false).show();

        }, fromCal.get(Calendar.HOUR_OF_DAY), fromCal.get(Calendar.MINUTE), false).show();
    }

    private void pickLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Location");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_location, null);
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

    private void loadOrganizerProfile() {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                organizerProfile = documentSnapshot.toObject(UserProfile.class);
            }
        });
    }

    private void saveEventToFirestore() {
        String title = binding.etEventTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String tagsString = binding.etTags.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(dateStr)) { Toast.makeText(this, "Date required", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(venue)) { Toast.makeText(this, "Venue required", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(price)) { Toast.makeText(this, "Price required", Toast.LENGTH_SHORT).show(); return; }

        // Convert tags
        List<String> tagsList;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tagsList = Arrays.stream(tagsString.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } else {
            tagsList = new java.util.ArrayList<>();
            for (String s : tagsString.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) tagsList.add(trimmed);
            }
        }

        // Construct the Event object
        Event eventData = new Event();
        eventData.title = title;
        eventData.date = dateStr;
        // Use entered times if available, else try to preserve existing
        String timeString = (fromTime.isEmpty() || toTime.isEmpty()) ? binding.tvTimePicker.getText().toString() : (fromTime + " - " + toTime);
        eventData.time = timeString;
        eventData.locationName = venue;
        eventData.locationAddress = address;
        eventData.description = description;
        eventData.price = price;
        eventData.tags = tagsList;

        // Handle images: use new URI if picked, otherwise keep existing (if editing)
        // NOTE: In a real app, you upload the new URI to Storage here.
        if (!posterUriString.isEmpty()) {
            eventData.eventPosterUrl = posterUriString;
        }
        // If editing, we want to preserve the old image if user didn't pick a new one.
        // But since we populated posterUriString in loadEventData, it should hold the old URL if not changed.

        eventData.organizerId = currentUserId;
        if (organizerProfile != null) {
            eventData.organizer = organizerProfile.getName();
            eventData.organizerIconUrl = organizerProfile.getProfilePictureUrl();
        }
        eventData.isTrending = false; // Or keep existing if editing

        // SAVE or UPDATE based on mode
        if (eventIdToEdit != null) {
            // --- UPDATE EXISTING ---
            db.collection("events").document(eventIdToEdit)
                    .set(eventData) // .set() overwrites. Use .update() for partial, but we want full overwrite here with new data.
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating event", Toast.LENGTH_SHORT).show());
        } else {
            // --- CREATE NEW ---
            db.collection("events").add(eventData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show());
        }
    }
}