package com.example.ballerevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class EventFormActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageButton favButton;
    private ImageButton editTitleButton;
    private ImageView calendar;
    private ImageButton editCalendarButton;
    private ImageView location;
    private ImageButton editLocationButton;

    private ImageButton editRequirementsButton;
    private ImageButton editAboutButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_form);
    }
}
