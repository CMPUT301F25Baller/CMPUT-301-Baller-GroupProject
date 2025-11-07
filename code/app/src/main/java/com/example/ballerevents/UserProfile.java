package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class UserProfile {

    @DocumentId
    private String id; // User's Firebase Auth UID

    public String name;
    public String email;
    public String role; // "entrant", "organizer", or "admin"
    public String aboutMe;
    public List<String> interests;
    public String profilePictureUrl; // Changed from int
    public List<String> appliedEventIds; // List of Event IDs
    public int followingCount; // Switched to public field
    public int followerCount; // Switched to public field

    // --- Empty constructor REQUIRED for Firestore ---
    public UserProfile() {}

    // --- Getters ---
    // (Not strictly needed if fields are public, but good practice)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAboutMe() { return aboutMe; }
    public List<String> getInterests() { return interests; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public List<String> getAppliedEventIds() { return appliedEventIds; }
    public int getFollowingCount() { return followingCount; }
    public int getFollowerCount() { return followerCount; }
}