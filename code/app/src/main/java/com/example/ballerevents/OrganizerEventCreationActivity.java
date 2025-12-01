package com.example.ballerevents;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ballerevents.databinding.ActivityOrganizerEventCreationBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class OrganizerEventCreationActivity extends AppCompatActivity {

    private ActivityOrganizerEventCreationBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerEventCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.etDate.setOnClickListener(v -> pickDateInto(binding.etDate));
        binding.etTime.setOnClickListener(v -> pickTimeInto(binding.etTime));

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void pickDateInto(android.widget.EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTimeInto(android.widget.EditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, h, m) -> target.setText(String.format("%02d:%02d", h, m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }
    private void saveEvent() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String time = binding.etTime.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String capacityStr = binding.etMaxAttendees.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);

        // 🔹 Use the new constructor: (title, desc, date, time, locationName, maxAttendees)
        Event event = new Event(title, desc, date, time, location, capacity);

        FirebaseFirestore.getInstance()
                .collection("events")
                .add(event)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Event published!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}
