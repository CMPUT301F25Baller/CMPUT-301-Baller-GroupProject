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

public class OrganizerEventCreationActivity extends AppCompatActivity {

    private static final String TAG = "EventCreationActivity";

    private ActivityOrganizerEventCreationBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private UserProfile organizerProfile;

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

        loadOrganizerProfile();
        setupImagePickers();
        setupClickListeners();
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
        binding.ivEventBanner.setOnClickListener(v -> bannerPickerLauncher.launch("image/*"));
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

        Event newEvent = new Event();
        newEvent.title = title;
        newEvent.date = dateStr;
        newEvent.time = fromTime + " - " + toTime;
        newEvent.locationName = venue;
        newEvent.locationAddress = address;
        newEvent.description = description;
        newEvent.price = price;
        newEvent.tags = tagsList;

        // NOTE: In a real app, you must upload these URIs to Firebase Storage
        // and get a download URL. For now, we are saving the local URI.
        // This image will ONLY load on this specific device.
        newEvent.eventPosterUrl = posterUriString; // Using poster as main image for now
        // You might want a separate field for bannerUrl in your Event model later

        newEvent.organizerId = currentUserId;
        if (organizerProfile != null) {
            newEvent.organizer = organizerProfile.getName();
            newEvent.organizerIconUrl = organizerProfile.getProfilePictureUrl();
        }
        newEvent.isTrending = false;

        db.collection("events").add(newEvent)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show());
    }
}