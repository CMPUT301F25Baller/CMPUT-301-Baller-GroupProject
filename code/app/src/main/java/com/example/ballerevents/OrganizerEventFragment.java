package com.example.ballerevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventFragment extends Fragment {

    private static final String ARG_ORG_ID = "org_id";

    public static OrganizerEventFragment newInstance(@NonNull String organizerId) {
        OrganizerEventFragment f = new OrganizerEventFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORG_ID, organizerId);
        f.setArguments(b);
        return f;
    }

    private String organizerId;
    private RecyclerView rv;
    private EventsAdapter adapter = new EventsAdapter(new ArrayList<>());

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        organizerId = getArguments() != null ? getArguments().getString(ARG_ORG_ID) : null;
    }

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View v = inflater.inflate(R.layout.fragment_organizer_event, container, false);
        rv = v.findViewById(R.id.rvOrganizerEvents);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        fetchMyEvents();
        return v;
    }

    private void fetchMyEvents() {
        if (organizerId == null) return;

        FirebaseFirestore.getInstance()
                .collection("events")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Event> list = snap.toObjects(Event.class);
                    adapter.submit(list);
                });
    }

    // --- Adapter that reuses your view_event_card_large layout ---
    private static class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        private List<Event> data;
        EventsAdapter(List<Event> d) { data = d; }
        void submit(List<Event> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View row = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.view_event_card_large, p, false);
            return new VH(row);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(data.get(pos)); }
        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView ivPoster, ivOrganizer;
            TextView tvTitle, tvDate, tvTime, tvLocationName, tvLocationAddress, tvOrganizerName;
            VH(@NonNull View v) {
                super(v);
                ivPoster = v.findViewById(R.id.ivEventPoster);
                ivOrganizer = v.findViewById(R.id.ivOrganizer);
                tvTitle = v.findViewById(R.id.tvEventTitle);
                tvDate = v.findViewById(R.id.tvEventDate);
                tvTime = v.findViewById(R.id.tvEventTime);
                tvLocationName = v.findViewById(R.id.tvEventLocationName);
                tvLocationAddress = v.findViewById(R.id.tvEventLocationAddress);
                tvOrganizerName = v.findViewById(R.id.tvOrganizerName);
            }
            void bind(Event e) {
                tvTitle.setText(e.getTitle());
                tvDate.setText(e.getDate());
                tvTime.setText(e.getTime());
                tvLocationName.setText(e.getLocationName());
                tvLocationAddress.setText(e.getLocationAddress());
                tvOrganizerName.setText(e.getOrganizer());

                Glide.with(itemView.getContext())
                        .load(e.getEventPosterUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(ivPoster);

                Glide.with(itemView.getContext())
                        .load(e.getOrganizerIconUrl())
                        .placeholder(R.drawable.placeholder_avatar1)
                        .error(R.drawable.placeholder_avatar1)
                        .into(ivOrganizer);
            }
        }
    }
}
