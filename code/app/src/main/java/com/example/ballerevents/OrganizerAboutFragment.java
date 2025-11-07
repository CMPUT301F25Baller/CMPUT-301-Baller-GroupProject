package com.example.ballerevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrganizerAboutFragment extends Fragment {

    private static final String ARG_ORG_ID = "org_id";

    public static OrganizerAboutFragment newInstance(@NonNull String organizerId) {
        OrganizerAboutFragment f = new OrganizerAboutFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORG_ID, organizerId);
        f.setArguments(b);
        return f;
    }

    private String organizerId;
    private TextView tvAboutMe;
    private ChipGroup chipGroupInterests;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        organizerId = getArguments() != null ? getArguments().getString(ARG_ORG_ID) : null;
    }

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle state) {
        View v = inf.inflate(R.layout.fragment_organizer_about, parent, false);
        tvAboutMe = v.findViewById(R.id.tvAboutMe);
        chipGroupInterests = v.findViewById(R.id.chipGroupInterests);
        loadProfile();
        return v;
    }

    private void loadProfile() {
        if (organizerId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(organizerId)
                .get()
                .addOnSuccessListener(doc -> {
                    UserProfile up = doc.toObject(UserProfile.class);
                    if (up == null) return;

                    tvAboutMe.setText(up.getAboutMe() == null ? "" : up.getAboutMe());

                    chipGroupInterests.removeAllViews();
                    List<String> interests = up.getInterests();
                    if (interests != null) {
                        for (String tag : interests) {
                            Chip c = new Chip(requireContext());
                            c.setText(tag);
                            c.setCheckable(false);
                            c.setClickable(false);
                            chipGroupInterests.addView(c);
                        }
                    }
                });
    }
}
