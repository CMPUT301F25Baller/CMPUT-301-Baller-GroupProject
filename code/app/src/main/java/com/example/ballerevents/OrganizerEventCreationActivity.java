package com.example.ballerevents;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

    // Image URIs
    private Uri selectedPosterUri = null;
    private Uri selectedBannerUri = null;

    // Calendars for event + registration windows
    private final Calendar eventCal = Calendar.getInstance();
    private final Calendar regStartCal = Calendar.getInstance();
    private final Calendar regEndCal = Calendar.getInstance();

    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    // Image pickers
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
        }

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------
    // IMAGE UPLOAD
    // ---------------------------------------------------------
    private void setupImageUploads() {
        binding.ivPageHeader.setOnClickListener(v -> bannerPicker.launch("image/*"));
        binding.ivEventPoster.setOnClickListener(v -> posterPicker.launch("image/*"));
    }

    // ---------------------------------------------------------
    // DATE / TIME PICKERS
    // ---------------------------------------------------------
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
        }, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker(EditText target, Calendar cal) {
        new TimePickerDialog(this, (view, h, min) -> {
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, min);
            target.setText(timeFormat.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false)
                .show();
    }

    // ---------------------------------------------------------
    // LOAD EXISTING EVENT (EDIT MODE)
    // ---------------------------------------------------------
    private void loadExistingEvent() {
        binding.tvPageTitle.setText("Edit Event");
        binding.btnSaveEvent.setText("Update Event");

        db.collection("events")
                .document(eventIdToEdit)
                .get()
                .addOnSuccessListener(doc -> {
                    Event e = doc.toObject(Event.class);
                    if (e == null) return;

                    binding.etTitle.setText(e.getTitle());
                    binding.etDescription.setText(e.getDescription());
                    binding.etLocation.setText(e.getLocationName());
                    binding.etDate.setText(e.getDate());
                    binding.etTime.setText(e.getTime());
                    binding.etMaxAttendees.setText(String.valueOf(e.getMaxAttendees()));

                    binding.etGeolocation.setChecked(e.isGeolocationRequired());

                    if (e.getEventPosterUrl() != null) {
                        Glide.with(this).load(e.getEventPosterUrl()).into(binding.ivEventPoster);
                        Glide.with(this).load(e.getEventPosterUrl())
                                .centerCrop().into(binding.ivPageHeader);
                    }

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
                });
    }

    // ---------------------------------------------------------
    // SAVE EVENT (NEW OR UPDATE)
    // ---------------------------------------------------------
    private void saveEvent() {

        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
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

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("date", date);
        data.put("time", time);
        data.put("locationName", location);
        data.put("maxAttendees", capacity);
        data.put("geolocationRequired", geolocationRequired);

        // Registration timestamps
        if (!TextUtils.isEmpty(binding.etRegStartDate.getText())) {
            data.put("registrationOpenAtMillis", regStartCal.getTimeInMillis());
        }
        if (!TextUtils.isEmpty(binding.etRegEndDate.getText())) {
            data.put("registrationCloseAtMillis", regEndCal.getTimeInMillis());
        }

        // Organizer ID
        if (auth.getCurrentUser() != null) {
            data.put("organizerId", auth.getCurrentUser().getUid());
        }

        // Poster + banner uploads
        if (selectedPosterUri != null) {
            data.put("eventPosterUrl", selectedPosterUri.toString());
        }
        if (selectedBannerUri != null) {
            data.put("eventBannerUrl", selectedBannerUri.toString());
        }

        // SAVE TO FIRESTORE
        if (eventIdToEdit != null) {
            db.collection("events").document(eventIdToEdit)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {

            // This preserves YOUR simplified constructor use case
            Event newEvent = new Event(title, desc, date, time, location, capacity);

            db.collection("events")
                    .add(data)  // Save data map (full version)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Event published!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
