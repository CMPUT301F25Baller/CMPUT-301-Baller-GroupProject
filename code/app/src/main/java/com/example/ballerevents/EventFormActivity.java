package com.example.ballerevents;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventFormActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageButton favButton;
    private TextView titleText;
    private ImageButton editTitleButton;
    private ImageView calendarImg;
    private ImageButton editCalendarButton;
    private TextView dateText;
    private TextView timeText;
    private ImageView locationImg;
    private ImageButton editLocationButton;

    private TextView locationName;
    private TextView locationDetails;
    private TextView aboutText;
    private ImageButton editAboutButton;
    boolean geolocationRequired;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageView eventImage;
    private ImageButton editPosterButton;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_form);
        backButton = findViewById(R.id.backButton);
        favButton = findViewById(R.id.favButton);
        eventImage = findViewById(R.id.eventImage);
        editPosterButton = findViewById(R.id.editPosterButton);

        titleText = findViewById(R.id.titleText);
        editTitleButton = findViewById(R.id.editTitle);

        calendarImg = findViewById(R.id.calendar_img);
        editCalendarButton = findViewById(R.id.editDate);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        locationImg = findViewById(R.id.location_img);
        editLocationButton = findViewById(R.id.editLocation);
        locationName = findViewById(R.id.locationNameText);
        locationDetails = findViewById(R.id.locationDetailsText);


        aboutText = findViewById(R.id.aboutText);
        editAboutButton = findViewById(R.id.editAboutButton);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        eventImage.setImageURI(uri);
                    }
                }
        );

        editPosterButton.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });


        calendarImg.setOnClickListener( v ->dateTimePicker());
        editCalendarButton.setOnClickListener( v ->dateTimePicker());
        editTitleButton.setOnClickListener(v ->inputDialog(titleText, "Edit Title"));
        editAboutButton.setOnClickListener(v ->inputDialog(aboutText, "Edit Description"));
        MaterialCheckBox materialCheckBox = findViewById(R.id.geolocationCheckBox);
        materialCheckBox.setOnCheckedChangeListener(((buttonView, isChecked) -> geolocationRequired= isChecked));

        // To be Completed
        favButton.setOnClickListener(v -> {
            Toast.makeText(this, "This adds event to your favourites.", Toast.LENGTH_SHORT).show();
        });
        locationImg.setOnClickListener(v -> {
            Toast.makeText(this, "This changes the location of your event..", Toast.LENGTH_SHORT).show();
        });

        editLocationButton.setOnClickListener(v -> {
            Toast.makeText(this, "This changes the location of your event..", Toast.LENGTH_SHORT).show();
        });



    }

    private void inputDialog(TextView target, String title) {
        EditText input = new EditText(this);
        input.setText(target.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Save", (d, which) -> {
                    target.setText(input.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dateTimePicker(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    pickStartTime(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        TextView title = new TextView(this);
        title.setText("Pick Date");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 40);
        datePickerDialog.setCustomTitle(title);
        datePickerDialog.show();
    }

    private void pickStartTime(Calendar calendar){
        TimePickerDialog startTimePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    Calendar startCal = (Calendar) calendar.clone();
                    startCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                    startCal.set(Calendar.MINUTE, selectedMinute);
                    pickEndTime(calendar, startCal);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        TextView title = new TextView(this);
        title.setText("Pick Start Time");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 40);

        startTimePickerDialog.setCustomTitle(title);
        startTimePickerDialog.show();
    }

    private void pickEndTime(Calendar dateCal, Calendar startCal){

        TimePickerDialog endTimePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) ->{
                    Calendar endCal = (Calendar) dateCal.clone();
                    endCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                    endCal.set(Calendar.MINUTE, selectedMinute);

                    if (endCal.before(startCal)) {
                        Toast.makeText(this, "End time must be AFTER start time", Toast.LENGTH_SHORT).show();
                        pickEndTime(dateCal, startCal); // show picker again
                        return;
                    }


                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                    SimpleDateFormat dayFormat =
                            new SimpleDateFormat("EEEE", Locale.getDefault());
                    SimpleDateFormat timeFormat =
                            new SimpleDateFormat("h:mm", Locale.getDefault());
                    SimpleDateFormat timeAmPm =
                            new SimpleDateFormat("a", Locale.getDefault());

                    String datePart = dateFormat.format(dateCal.getTime());
                    String dayPart = dayFormat.format(dateCal.getTime());
                    String startPart = timeFormat.format(startCal.getTime());
                    String endPart = timeFormat.format(endCal.getTime());

                    String startPeriod = timeAmPm.format(startCal.getTime()); // AM or PM
                    String endPeriod = timeAmPm.format(endCal.getTime());


                    String timePart;
                    if (startPeriod.equals(endPeriod)) {
                        timePart = startPart + "–" + endPart + " " + startPeriod;
                    } else {
                        // Different periods → show both
                        timePart = startPart + " " + startPeriod + "–" + endPart + " " + endPeriod;
                    }


                    String finalTimeText = dayPart + ", " + timePart;
                    dateText.setText(datePart);
                    timeText.setText(finalTimeText);

                },
                dateCal.get(Calendar.HOUR_OF_DAY),
                dateCal.get(Calendar.MINUTE),
                false
        );
        TextView title = new TextView(this);
        title.setText("Pick End Time");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 40);

        endTimePickerDialog.setCustomTitle(title);
        endTimePickerDialog.show();
    }
}
