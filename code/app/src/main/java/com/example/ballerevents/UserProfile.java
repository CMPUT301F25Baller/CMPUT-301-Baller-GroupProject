package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import java.util.ArrayList; // Import ArrayList

/**
 * Represents a single user document from the Firestore "users" collection.
 * This class is a "POJO" (Plain Old Java Object) used by Firestore for
 * serializing and deserializing data. It holds all information for
 * Entrants, Organizers, and Admins.
 */
public class UserProfile {

    /**
     * The unique Firestore document ID.
     * This field is automatically populated by Firestore and
     * should match the user's Firebase Authentication UID.
     */
    @DocumentId
    private String id;

    // Fields are public for easy Firestore serialization

    /** The user's display name. */
    public String name;
    /** The user's email address. */
    public String email;
    /** The user's role: "entrant", "organizer", or "admin". */
    public String role;
    /** A short biography for the user's profile. */
    public String aboutMe;
    /** A list of the user's selected interests. */
    public List<String> interests;
    /** A URL (e.g., in Firebase Storage) for the user's profile picture. */
    public String profilePictureUrl;
    /** A list of event document IDs this user has applied to (joined the waitlist for). */
    public List<String> appliedEventIds;
    /** A list of user document IDs that this user is following. */
    public List<String> followingIds;
    /** A list of user document IDs that are following this user. */
    public List<String> followerIds;

    /**
     * Empty constructor required for Firestore deserialization.
     * Initializes lists to avoid NullPointerExceptions when a new
     * user document is created.
     */
    public UserProfile() {
        // Initialize lists to avoid NullPointerExceptions
        interests = new ArrayList<>();
        appliedEventIds = new ArrayList<>();
        followingIds = new ArrayList<>();
        followerIds = new ArrayList<>();
    }

    // --- Getters ---
    // (Good practice, though Firestore can use public fields)

    /** @return The user's unique Firebase Auth UID (which is also their document ID). */
    public String getId() { return id; }
    /** @return The user's display name. */
    public String getName() { return name; }
    /** @return The user's email address. */
    public String getEmail() { return email; }
    /** @return The user's role: "entrant", "organizer", or "admin". */
    public String getRole() { return role; }
    /** @return A short biography for the user's profile. */
    public String getAboutMe() { return aboutMe; }
    /** @return A list of the user's selected interests. */
    public List<String> getInterests() { return interests; }
    /** @return A URL for the user's profile picture. */
    public String getProfilePictureUrl() { return profilePictureUrl; }
    /** @return A list of event document IDs this user has applied to. */
    public List<String> getAppliedEventIds() { return appliedEventIds; }

    // Getters for new lists
    /** @return A list of user document IDs that this user is following. */
    public List<String> getFollowingIds() { return followingIds; }
    /** @return A list of user document IDs that are following this user. */
    public List<String> getFollowerIds() { return followerIds; }

    // Getters for counts (derived from list size)
    /** @return The number of users this user is following. */
    public int getFollowingCount() { return (followingIds != null) ? followingIds.size() : 0; }
    /** @return The number of users who are following this user. */
    public int getFollowerCount() { return (followerIds != null) ? followerIds.size() : 0; }
}