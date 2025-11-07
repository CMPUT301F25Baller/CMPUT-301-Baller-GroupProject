package com.example.ballerevents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrganizerEventCreationActivity extends AppCompatActivity {

    private TextView tvTitle, tvDate, tvTime, tvVenue, tvAddress, tvRequirements, tvDescription;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_creation);

        findViewById(R.id.toolbarBack).setOnClickListener(v -> onBackPressed());

        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvVenue = findViewById(R.id.tvVenue);
        tvAddress = findViewById(R.id.tvAddress);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvDescription = findViewById(R.id.tvDescription);

        // Initialize calendars with default values
        try {
            Date date = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH).parse(dateStr);
            if (date != null) eventDate.setTime(date);
        } catch (ParseException e) {
            // Ignore, use current
        }
        try {
            Date from = DateFormat.getTimeInstance(DateFormat.SHORT).parse(fromTime);
            if (from != null) {
                fromCal.set(Calendar.HOUR_OF_DAY, from.getHours());
                fromCal.set(Calendar.MINUTE, from.getMinutes());
            }
            Date to = DateFormat.getTimeInstance(DateFormat.SHORT).parse(toTime);
            if (to != null) {
                toCal.set(Calendar.HOUR_OF_DAY, to.getHours());
                toCal.set(Calendar.MINUTE, to.getMinutes());
            }
        } catch (ParseException e) {
            // Ignore
        }

        updateDisplays();

        // Edit listeners
        findViewById(R.id.editTitle).setOnClickListener(v -> editTitle());
        findViewById(R.id.editDateTime).setOnClickListener(v -> editDateTime());
        findViewById(R.id.editLocation).setOnClickListener(v -> editLocation());
        findViewById(R.id.editRequirements).setOnClickListener(v -> editMultiline("Event Requirements", requirements, newValue -> {
            requirements = newValue;
            tvRequirements.setText(TextUtils.isEmpty(newValue) ? "Event requirements" : newValue);
        }));
        findViewById(R.id.editDescription).setOnClickListener(v -> editMultiline("About Event", description, newValue -> {
            description = newValue;
            tvDescription.setText(TextUtils.isEmpty(newValue) ? "Add description" : newValue);
        }));

        ((MaterialButton) findViewById(R.id.btnPublish)).setOnClickListener(v -> {
            if (validate()) {
                Toast.makeText(this, "Event published (prototype)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDisplays() {
        tvTitle.setText(title);
        tvDate.setText(dateStr);
        tvTime.setText(String.format("%s, %s - %s", dayStr, fromTime, toTime));
        tvVenue.setText(venue);
        tvAddress.setText(address);
        tvRequirements.setText(TextUtils.isEmpty(requirements) ? "Event requirements" : requirements);
        tvDescription.setText(TextUtils.isEmpty(description) ? "Add description" : description);
    }

    private void editTitle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Title");
        final EditText input = new EditText(this);
        input.setText(title);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            title = input.getText().toString().trim();
            tvTitle.setText(title);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

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

    private void editLocation() {
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
            tvVenue.setText(venue);
            tvAddress.setText(address);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

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

    private interface ValueCallback {
        void onValue(String newValue);
    }

    private boolean validate() {
        boolean ok = true;
        if (TextUtils.isEmpty(title)) { Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(dateStr)) { Toast.makeText(this, "Date required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(fromTime)) { Toast.makeText(this, "From time required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(toTime)) { Toast.makeText(this, "To time required", Toast.LENGTH_SHORT).show(); ok = false; }
        if (TextUtils.isEmpty(venue)) { Toast.makeText(this, "Venue required", Toast.LENGTH_SHORT).show(); ok = false; }
        return ok;
    }
}