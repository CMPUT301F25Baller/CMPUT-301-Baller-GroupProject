package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map; // Explicitly import java.util.Map

/**
 * Activity that displays a map of where entrants joined the waitlist from.
 */
public class OrganizerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_map);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadEventLocations();
    }

    private void loadEventLocations() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.toObject(Event.class);
                    if (event != null) {
                        Map<String, GeoPoint> locations = event.getEntrantLocations();
                        if (locations != null && !locations.isEmpty()) {
                            displayMarkers(locations);
                        } else {
                            Toast.makeText(this, "No locations to display.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayMarkers(Map<String, GeoPoint> locations) {
        if (mMap == null) return;

        for (Map.Entry<String, GeoPoint> entry : locations.entrySet()) {
            GeoPoint gp = entry.getValue();
            if (gp != null) {
                LatLng position = new LatLng(gp.getLatitude(), gp.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Entrant"));
            }
        }

        if (!locations.isEmpty()) {
            GeoPoint first = locations.values().iterator().next();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(first.getLatitude(), first.getLongitude()), 10));
        }
    }
}