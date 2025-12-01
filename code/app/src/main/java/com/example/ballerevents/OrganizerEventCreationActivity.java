package com.example.ballerevents;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityOrganizerEventCreationBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity allowing Organizers to create or edit events.
 * Features:
 * - Date/Time pickers for Event Schedule and Registration Window.
 * - Image pickers for Banner and Poster.
 * - Input for Lottery Capacity (Max Attendees).
 */
public class OrganizerEventCreationActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "EventCreation";

    private ActivityOrganizerEventCreationBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String eventIdToEdit;

    // Uri objects for images
    private Uri selectedPosterUri = null;
    private Uri selectedBannerUri = null;

    // Track existing URLs for deletion when updating
    private String existingPosterUrl = null;
    private String existingBannerUrl = null;

    // Calendars to store selected dates/times
    private final Calendar eventDateCal = Calendar.getInstance();
    private final Calendar regStartCal = Calendar.getInstance();
    private final Calendar regEndCal = Calendar.getInstance();

    // Formatters for display
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    // Image Pickers
    private final ActivityResultLauncher<String> posterPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPosterUri = uri;
                    Glide.with(this).load(uri).into(binding.ivEventPoster);
                }
            });

    private final ActivityResultLauncher<String> bannerPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBannerUri = uri;
                    Glide.with(this).load(uri).centerCrop().into(binding.ivPageHeader);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerEventCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        eventIdToEdit = getIntent().getStringExtra(EXTRA_EVENT_ID);

        setupPickers();
        setupImageUploads();

        if (eventIdToEdit != null) {
            loadExistingEvent();
        } else {
            // Default: clear registration calendars to current time
            regStartCal.setTimeInMillis(System.currentTimeMillis());
            regEndCal.setTimeInMillis(System.currentTimeMillis());
        }

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupImageUploads() {
        // Top Banner Click
        binding.ivPageHeader.setOnClickListener(v -> bannerPicker.launch("image/*"));

        // Poster Click
        binding.ivEventPoster.setOnClickListener(v -> posterPicker.launch("image/*"));
    }

    private void setupPickers() {
        // Event Date/Time
        binding.etDate.setOnClickListener(v -> showDatePicker(binding.etDate, eventDateCal));
        binding.etTime.setOnClickListener(v -> showTimePicker(binding.etTime, eventDateCal));

        // Registration Start
        binding.etRegStartDate.setOnClickListener(v -> showDatePicker(binding.etRegStartDate, regStartCal));
        binding.etRegStartTime.setOnClickListener(v -> showTimePicker(binding.etRegStartTime, regStartCal));

        // Registration End
        binding.etRegEndDate.setOnClickListener(v -> showDatePicker(binding.etRegEndDate, regEndCal));
        binding.etRegEndTime.setOnClickListener(v -> showTimePicker(binding.etRegEndTime, regEndCal));
    }

    private void showDatePicker(EditText target, Calendar cal) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            target.setText(dateFormat.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText target, Calendar cal) {
        new TimePickerDialog(this, (view, hour, minute) -> {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            target.setText(timeFormat.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    private void loadExistingEvent() {
        binding.tvPageTitle.setText("Edit Event");
        binding.btnSaveEvent.setText("Update Event");

        db.collection("events").document(eventIdToEdit).get()
                .addOnSuccessListener(doc -> {
                    Event e = doc.toObject(Event.class);
                    if (e != null) {
                        binding.etTitle.setText(e.getTitle());
                        binding.etDescription.setText(e.getDescription());
                        binding.etLocation.setText(e.getLocationName());
                        binding.etDate.setText(e.getDate());
                        binding.etTime.setText(e.getTime());

                        if (e.getMaxAttendees() > 0) {
                            binding.etMaxAttendees.setText(String.valueOf(e.getMaxAttendees()));
                        }
                        binding.etGeolocation.setChecked(e.isGeolocationRequired());


                        // Load Poster
                        if (e.getEventPosterUrl() != null && !e.getEventPosterUrl().isEmpty()) {
                            existingPosterUrl = e.getEventPosterUrl();
                            Glide.with(this).load(e.getEventPosterUrl()).into(binding.ivEventPoster);
                        }
                        // Load Banner
                        if (e.getEventBannerUrl() != null && !e.getEventBannerUrl().isEmpty()) {
                            existingBannerUrl = e.getEventBannerUrl();
                            Glide.with(this).load(e.getEventBannerUrl()).centerCrop().into(binding.ivPageHeader);
                        }

                        // Load registration timestamps back into calendars
                        if (e.registrationOpenAtMillis > 0) {
                            regStartCal.setTimeInMillis(e.registrationOpenAtMillis);
                            binding.etRegStartDate.setText(dateFormat.format(regStartCal.getTime()));
                            binding.etRegStartTime.setText(timeFormat.format(regStartCal.getTime()));
                        }

                        if (e.registrationCloseAtMillis > 0) {
                            regEndCal.setTimeInMillis(e.registrationCloseAtMillis);
                            binding.etRegEndDate.setText(dateFormat.format(regEndCal.getTime()));
                            binding.etRegEndTime.setText(timeFormat.format(regEndCal.getTime()));
                        }
                    }
                })
                .addOnFailureListener(ex -> {
                    Toast.makeText(this, "Failed to load event: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void saveEvent() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String dateStr = binding.etDate.getText().toString().trim();
        String timeStr = binding.etTime.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String capacityStr = binding.etMaxAttendees.getText().toString().trim();
        boolean geolocationRequirement = binding.etGeolocation.isChecked();

        if (TextUtils.isEmpty(title)) {
            binding.etTitle.setError("Title required");
            return;
        }

        int maxAttendees = 0;
        if (!TextUtils.isEmpty(capacityStr)) {
            try {
                maxAttendees = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                binding.etMaxAttendees.setError("Invalid number");
                return;
            }
        }

        // Disable save button and show progress
        binding.btnSaveEvent.setEnabled(false);
        binding.btnSaveEvent.setText("Saving...");

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("date", dateStr);
        data.put("time", timeStr);
        data.put("locationName", location);
        data.put("maxAttendees", maxAttendees);
        data.put("geolocationRequired", geolocationRequirement);

        // Save timestamps for Lottery Logic
        if (!TextUtils.isEmpty(binding.etRegStartDate.getText())) {
            data.put("registrationOpenAtMillis", regStartCal.getTimeInMillis());
        }
        if (!TextUtils.isEmpty(binding.etRegEndDate.getText())) {
            data.put("registrationCloseAtMillis", regEndCal.getTimeInMillis());
        }

        if (auth.getCurrentUser() != null) {
            data.put("organizerId", auth.getCurrentUser().getUid());
        }

        // Handle image uploads
        uploadImagesAndSave(data);
    }

    /**
     * Uploads poster and banner images to Firebase Storage, then saves the event.
     */
    private void uploadImagesAndSave(Map<String, Object> data) {
        // Track upload completion
        final int[] uploadsRemaining = {0};

        // Count how many uploads we need
        if (selectedPosterUri != null) uploadsRemaining[0]++;
        if (selectedBannerUri != null) uploadsRemaining[0]++;

        // If no new images, save immediately with existing URLs
        if (uploadsRemaining[0] == 0) {
            if (existingPosterUrl != null) {
                data.put("eventPosterUrl", existingPosterUrl);
            }
            if (existingBannerUrl != null) {
                data.put("eventBannerUrl", existingBannerUrl);
            }
            saveEventToFirestore(data);
            return;
        }

        // Upload poster if selected
        if (selectedPosterUri != null) {
            ImageUploadHelper.uploadEventPoster(selectedPosterUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    data.put("eventPosterUrl", downloadUrl);

                    // Delete old poster if we're updating
                    if (existingPosterUrl != null && !existingPosterUrl.isEmpty()) {
                        ImageUploadHelper.deleteImage(existingPosterUrl, new ImageUploadHelper.DeleteCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Old poster deleted");
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.w(TAG, "Failed to delete old poster", e);
                            }
                        });
                    }

                    uploadsRemaining[0]--;
                    checkAndSaveEvent(data, uploadsRemaining[0]);
                }

                @Override
                public void onFailure(Exception e) {
                    resetSaveButton();
                    Toast.makeText(OrganizerEventCreationActivity.this,
                            "Failed to upload poster: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        // Upload banner if selected
        if (selectedBannerUri != null) {
            ImageUploadHelper.uploadEventBanner(selectedBannerUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    data.put("eventBannerUrl", downloadUrl);

                    // Delete old banner if we're updating
                    if (existingBannerUrl != null && !existingBannerUrl.isEmpty()) {
                        ImageUploadHelper.deleteImage(existingBannerUrl, new ImageUploadHelper.DeleteCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Old banner deleted");
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.w(TAG, "Failed to delete old banner", e);
                            }
                        });
                    }

                    uploadsRemaining[0]--;
                    checkAndSaveEvent(data, uploadsRemaining[0]);
                }

                @Override
                public void onFailure(Exception e) {
                    resetSaveButton();
                    Toast.makeText(OrganizerEventCreationActivity.this,
                            "Failed to upload banner: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    /**
     * Checks if all image uploads are complete, then saves the event.
     */
    private void checkAndSaveEvent(Map<String, Object> data, int remaining) {
        if (remaining == 0) {
            saveEventToFirestore(data);
        }
    }

    /**
     * Saves the event data to Firestore (create or update).
     */
    private void saveEventToFirestore(Map<String, Object> data) {
        if (eventIdToEdit != null) {
            // Update existing event
            db.collection("events").document(eventIdToEdit)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        resetSaveButton();
                        Toast.makeText(this, "Update failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            // Create new event
            db.collection("events").add(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        resetSaveButton();
                        Toast.makeText(this, "Creation failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    /**
     * Re-enables the save button after an error.
     */
    private void resetSaveButton() {
        binding.btnSaveEvent.setEnabled(true);
        binding.btnSaveEvent.setText(eventIdToEdit != null ? "Update Event" : "Save Event");
    }
}