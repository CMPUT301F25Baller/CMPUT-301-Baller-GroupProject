package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import java.util.ArrayList;

/**
 * Model class representing a user document inside the Firestore
 * <code>"users"</code> collection.
 * <p>
 * This class acts as a POJO used for Firestore serialization/deserialization and
 * holds profile information for Entrants, Organizers, and Admins.
 * Fields are public to allow Firestore to populate them directly.
 * </p>
 */
public class UserProfile {

    /**
     * The Firestore document ID for this user (matches FirebaseAuth UID).
     */
    @DocumentId
    private String id;

    public String name;
    public String email;
    public String role;
    public String aboutMe;
    public List<String> interests;
    public String profilePictureUrl;

    public List<String> appliedEventIds;
    public List<String> invitedEventIds;
    public List<String> joinedEventIds;

    public List<String> followingIds;
    public List<String> followerIds;

    private boolean notificationsEnabled = true;

    /**
     * No-argument constructor required by Firestore.
     * Initializes list fields to avoid null pointer exceptions.
     */
    public UserProfile() {
        interests = new ArrayList<>();
        appliedEventIds = new ArrayList<>();
        invitedEventIds = new ArrayList<>();
        joinedEventIds = new ArrayList<>();
        followingIds = new ArrayList<>();
        followerIds = new ArrayList<>();
    }

    // ----------------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------------

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    /**
     * Alias for {@link #getId()}.
     * Provides compatibility with code expecting standard Firebase naming conventions.
     */
    public String getUid() { return id; }

    /**
     * Alias for {@link #setId(String)}.
     * Provides compatibility with code expecting standard Firebase naming conventions.
     */
    public void setUid(String uid) { this.id = uid; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAboutMe() { return aboutMe; }
    public List<String> getInterests() { return interests; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public List<String> getAppliedEventIds() { return appliedEventIds; }
    public List<String> getInvitedEventIds() { return invitedEventIds; }
    public List<String> getJoinedEventIds() { return joinedEventIds; }

    public List<String> getFollowingIds() { return followingIds; }
    public List<String> getFollowerIds() { return followerIds; }

    public int getFollowingCount() {
        return (followingIds != null) ? followingIds.size() : 0;
    }

    public int getFollowerCount() {
        return (followerIds != null) ? followerIds.size() : 0;
    }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}