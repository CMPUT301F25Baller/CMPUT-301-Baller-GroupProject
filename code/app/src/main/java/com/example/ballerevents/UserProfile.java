package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import java.util.ArrayList; // Import ArrayList

public class UserProfile {

    @DocumentId
    private String id; // User's Firebase Auth UID

    // Fields are public for easy Firestore serialization
    public String name;
    public String email;
    public String role; // "entrant", "organizer", or "admin"
    public String aboutMe;
    public List<String> interests;
    public String profilePictureUrl;
    public List<String> appliedEventIds; // List of Event IDs

    // --- UPDATED/ADDED FIELDS ---
    public List<String> followingIds; // List of User IDs this user is following
    public List<String> followerIds;  // List of User IDs that follow this user

    // Note: The counts are now derived from the size of these lists.
    // The 'followingCount' and 'followerCount' fields are no longer needed
    // unless you want to store them for quick reads.
    // For simplicity, I've removed them to avoid data duplication.
    // We will get the count from `.size()`

    // --- Empty constructor REQUIRED for Firestore ---
    public UserProfile() {
        // Initialize lists to avoid NullPointerExceptions
        interests = new ArrayList<>();
        appliedEventIds = new ArrayList<>();
        followingIds = new ArrayList<>();
        followerIds = new ArrayList<>();
    }

    // --- Getters ---
    // (Good practice, though Firestore can use public fields)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAboutMe() { return aboutMe; }
    public List<String> getInterests() { return interests; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public List<String> getAppliedEventIds() { return appliedEventIds; }

    // Getters for new lists
    public List<String> getFollowingIds() { return followingIds; }
    public List<String> getFollowerIds() { return followerIds; }

    // Getters for counts (derived from list size)
    public int getFollowingCount() { return (followingIds != null) ? followingIds.size() : 0; }
    public int getFollowerCount() { return (followerIds != null) ? followerIds.size() : 0; }
}