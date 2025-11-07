package com.example.ballerevents;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    private TextView requirementsText;
    private ImageButton editRequirementsButton;
    private TextView aboutText;
    private ImageButton editAboutButton;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_form);
        backButton = findViewById(R.id.backButton);
        favButton = findViewById(R.id.favButton);

        titleText = findViewById(R.id.eventFormTitle);
        editTitleButton = findViewById(R.id.editTitle);

        calendarImg = findViewById(R.id.calendar_img);
        editCalendarButton = findViewById(R.id.editDate);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        locationImg = findViewById(R.id.location_img);
        editLocationButton = findViewById(R.id.editLocation);
        locationName = findViewById(R.id.locationNameText);
        locationDetails = findViewById(R.id.locationDetailsText);

        requirementsText = findViewById(R.id.requirementsText);
        editRequirementsButton = findViewById(R.id.editRequirementsButton);

        aboutText = findViewById(R.id.aboutText);
        editAboutButton = findViewById(R.id.editAboutButton);
    }

    private void dateTimePicker(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    // selectedMonth + 1 because months are 0-based
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();

    }
}
