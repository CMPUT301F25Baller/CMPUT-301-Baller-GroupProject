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
 * - Date/Time pickers
 * - Image pickers for Banner + Poster
 * - Full Firebase Storage upload
 * - Keeps old images when editing
 * - Supports registration open/close timestamps
 */
public class OrganizerEventCreationActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "EventCreation";

    private ActivityOrganizerEventCreationBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String eventIdToEdit;

    // New image URIs selected by the user
    private Uri selectedPosterUri = null;
    private Uri selectedBannerUri = null;

    // Original URLs (only used when editing)
    private String existingPosterUrl = null;
    private String existingBannerUrl = null;

    // Date/time storage
    private final Calendar eventCal = Calendar.getInstance();
    private final Calendar regStartCal = Calendar.getInstance();
    private final Calendar regEndCal = Calendar.getInstance();

    // Formatters
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
            regStartCal.setTimeInMillis(System.currentTimeMillis());
            regEndCal.setTimeInMillis(System.currentTimeMillis());
        }

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());
        binding.btnBack.setOnClickListener(v -> finish());
    }


    // ---------------------------------------------
    // IMAGE UPLOAD CLICKS
    // ---------------------------------------------
    private void setupImageUploads() {
        binding.ivPageHeader.setOnClickListener(v -> bannerPicker.launch("image/*"));
        binding.ivEventPoster.setOnClickListener(v -> posterPicker.launch("image/*"));
    }


    // ---------------------------------------------
    // DATE/TIME PICKERS
    // ---------------------------------------------
    private void setupPickers() {

        binding.etDate.setOnClickListener(v -> showDatePicker(binding.etDate, eventCal));
        binding.etTime.setOnClickListener(v -> showTimePicker(binding.etTime, eventCal));

        binding.etRegStartDate.setOnClickListener(v -> showDatePicker(binding.etRegStartDate, regStartCal));
        binding.etRegStartTime.setOnClickListener(v -> showTimePicker(binding.etRegStartTime, regStartCal));

        binding.etRegEndDate.setOnClickListener(v -> showDatePicker(binding.etRegEndDate, regEndCal));
        binding.etRegEndTime.setOnClickListener(v -> showTimePicker(binding.etRegEndTime, regEndCal));
    }

    private void showDatePicker(EditText target, Calendar cal) {
        new DatePickerDialog(this, (view, y, m, d) -> {
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m);
            cal.set(Calendar.DAY_OF_MONTH, d);
            target.setText(dateFormat.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText target, Calendar cal) {
        new TimePickerDialog(this, (view, h, min) -> {
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, min);
            target.setText(timeFormat.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }


    // ---------------------------------------------
    // LOAD EXISTING EVENT (EDIT MODE)
    // ---------------------------------------------
    private void loadExistingEvent() {
        binding.tvPageTitle.setText("Edit Event");
        binding.btnSaveEvent.setText("Update Event");

        db.collection("events").document(eventIdToEdit).get()
                .addOnSuccessListener(doc -> {
                    Event e = doc.toObject(Event.class);
                    if (e == null) return;

                    binding.etTitle.setText(e.getTitle());
                    binding.etDescription.setText(e.getDescription());
                    binding.etLocation.setText(e.getLocationName());
                    binding.etDate.setText(e.getDate());
                    binding.etTime.setText(e.getTime());

                    if (e.getMaxAttendees() > 0) {
                        binding.etMaxAttendees.setText(String.valueOf(e.getMaxAttendees()));
                    }

                    binding.etGeolocation.setChecked(e.isGeolocationRequired());

                    // Poster
                    if (e.getEventPosterUrl() != null && !e.getEventPosterUrl().isEmpty()) {
                        existingPosterUrl = e.getEventPosterUrl();
                        Glide.with(this).load(existingPosterUrl).into(binding.ivEventPoster);
                    }

                    // Banner
                    if (e.getEventBannerUrl() != null && !e.getEventBannerUrl().isEmpty()) {
                        existingBannerUrl = e.getEventBannerUrl();
                        Glide.with(this).load(existingBannerUrl).centerCrop().into(binding.ivPageHeader);
                    }

                    // Registration window
                    if (e.registrationOpenAtMillis() > 0) {
                        regStartCal.setTimeInMillis(e.registrationOpenAtMillis());
                        binding.etRegStartDate.setText(dateFormat.format(regStartCal.getTime()));
                        binding.etRegStartTime.setText(timeFormat.format(regStartCal.getTime()));
                    }

                    if (e.registrationCloseAtMillis() > 0) {
                        regEndCal.setTimeInMillis(e.registrationCloseAtMillis());
                        binding.etRegEndDate.setText(dateFormat.format(regEndCal.getTime()));
                        binding.etRegEndTime.setText(timeFormat.format(regEndCal.getTime()));
                    }
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    // ---------------------------------------------
    // SAVE EVENT
    // ---------------------------------------------
    private void saveEvent() {

        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String time = binding.etTime.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String capacityStr = binding.etMaxAttendees.getText().toString().trim();
        boolean geolocationRequired = binding.etGeolocation.isChecked();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = 0;
        if (!capacityStr.isEmpty()) {
            try {
                capacity = Integer.parseInt(capacityStr);
            } catch (Exception e) {
                binding.etMaxAttendees.setError("Invalid number");
                return;
            }
        }

        binding.btnSaveEvent.setEnabled(false);
        binding.btnSaveEvent.setText("Saving...");

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("date", date);
        data.put("time", time);
        data.put("locationName", location);
        data.put("maxAttendees", capacity);
        data.put("geolocationRequired", geolocationRequired);

        if (!TextUtils.isEmpty(binding.etRegStartDate.getText())) {
            data.put("registrationOpenAtMillis", regStartCal.getTimeInMillis());
        }
        if (!TextUtils.isEmpty(binding.etRegEndDate.getText())) {
            data.put("registrationCloseAtMillis", regEndCal.getTimeInMillis());
        }

        if (auth.getCurrentUser() != null) {
            data.put("organizerId", auth.getCurrentUser().getUid());
        }

        uploadImagesAndSave(data);
    }


    // ---------------------------------------------
    // UPLOAD IMAGES (poster + banner)
    // ---------------------------------------------
    private void uploadImagesAndSave(Map<String, Object> data) {

        int uploadCount = 0;

        if (selectedPosterUri != null) uploadCount++;
        if (selectedBannerUri != null) uploadCount++;

        final int[] remaining = {uploadCount};

        // No uploads needed → save directly
        if (uploadCount == 0) {

            if (existingPosterUrl != null) data.put("eventPosterUrl", existingPosterUrl);
            if (existingBannerUrl != null) data.put("eventBannerUrl", existingBannerUrl);

            saveEventToFirestore(data);
            return;
        }

        // Upload poster
        if (selectedPosterUri != null) {
            ImageUploadHelper.uploadEventPoster(selectedPosterUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    data.put("eventPosterUrl", downloadUrl);

                    if (existingPosterUrl != null && !existingPosterUrl.isEmpty()) {
                        ImageUploadHelper.deleteImage(existingPosterUrl, null);
                    }

                    if (--remaining[0] == 0) {
                        saveEventToFirestore(data);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    resetSaveButton();
                    Toast.makeText(OrganizerEventCreationActivity.this, "Poster upload failed", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Upload banner
        if (selectedBannerUri != null) {
            ImageUploadHelper.uploadEventBanner(selectedBannerUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    data.put("eventBannerUrl", downloadUrl);

                    if (existingBannerUrl != null && !existingBannerUrl.isEmpty()) {
                        ImageUploadHelper.deleteImage(existingBannerUrl, null);
                    }

                    if (--remaining[0] == 0) {
                        saveEventToFirestore(data);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    resetSaveButton();
                    Toast.makeText(OrganizerEventCreationActivity.this, "Banner upload failed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    private void saveEventToFirestore(Map<String, Object> data) {

        if (eventIdToEdit != null) {
            db.collection("events").document(eventIdToEdit)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        resetSaveButton();
                        Toast.makeText(this, "Update failed", Toast.LENGTH_LONG).show();
                    });
        } else {
            db.collection("events").add(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        resetSaveButton();
                        Toast.makeText(this, "Creation failed", Toast.LENGTH_LONG).show();
                    });
        }
    }


    private void resetSaveButton() {
        binding.btnSaveEvent.setEnabled(true);
        binding.btnSaveEvent.setText(eventIdToEdit != null ? "Update Event" : "Save Event");
    }
}
