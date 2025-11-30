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

    // Uri strings for images
    private Uri selectedPosterUri = null;
    private Uri selectedBannerUri = null;

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
                            Glide.with(this).load(e.getEventPosterUrl()).into(binding.ivEventPoster);
                            // We also load it into banner for visual consistency if desired, or leave placeholder
                        }
                        if (e.getEventBannerUrl() != null && !e.getEventBannerUrl().isEmpty()) {
                            Glide.with(this).load(e.getEventBannerUrl()).centerCrop().into(binding.ivPageHeader);
                        }

                        // Note: Ideally we load registration timestamps back into calendars here
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
                });
    }

    private void saveEvent() {
        String title = binding.etTitle.getText().toString();
        String description = binding.etDescription.getText().toString();
        String dateStr = binding.etDate.getText().toString();
        String timeStr = binding.etTime.getText().toString();
        String location = binding.etLocation.getText().toString();
        String capacityStr = binding.etMaxAttendees.getText().toString();
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

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("date", dateStr);
        data.put("time", timeStr);
        data.put("locationName", location);
        data.put("maxAttendees", maxAttendees);
        data.put("geolocationRequired", geolocationRequirement);


        // Save timestamps for Lottery Logic
        // Only save if the fields are actually filled
        if (!TextUtils.isEmpty(binding.etRegStartDate.getText())) {
            data.put("registrationOpenAtMillis", regStartCal.getTimeInMillis());
        }
        if (!TextUtils.isEmpty(binding.etRegEndDate.getText())) {
            data.put("registrationCloseAtMillis", regEndCal.getTimeInMillis());
        }

        if (auth.getCurrentUser() != null) {
            data.put("organizerId", auth.getCurrentUser().getUid());
        }

        // Handle Image URIs (For prototype, we store string URI. Production needs Storage upload)
        if (selectedPosterUri != null) {
            data.put("eventPosterUrl", selectedPosterUri.toString());
        }
        if (selectedBannerUri != null){
            data.put("eventBannerUrl", selectedBannerUri.toString());
        }
        // Note: Event model doesn't have 'bannerUrl', so we only save poster for now.

        if (eventIdToEdit != null) {
            db.collection("events").document(eventIdToEdit)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("events").add(data)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e -> Toast.makeText(this, "Creation failed", Toast.LENGTH_SHORT).show());
        }
    }
}