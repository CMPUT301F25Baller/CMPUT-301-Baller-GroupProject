package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import java.util.ArrayList;

/**
 * Model class representing a user document inside the Firestore
 * <code>"users"</code> collection. This class acts as a POJO used for
 * Firestore serialization/deserialization and holds profile information
 * for Entrants, Organizers, and Admins.
 *
 * <p>The fields are intentionally public so Firestore can populate them
 * without requiring custom getters/setters. Lists are initialized in the
 * no-argument constructor to avoid null-pointer issues when reading new
 * or partially-filled Firestore documents.</p>
 */
public class UserProfile {

    /**
     * The Firestore document ID for this user.
     * <p>
     * Firestore automatically assigns and injects this value during
     * deserialization. In this app’s design, this matches the FirebaseAuth UID.
     */
    @DocumentId
    private String id;

    /** The user's display name. */
    public String name;

    /** The user's email address. */
    public String email;

    /** The user's role: e.g. "entrant", "organizer", or "admin". */
    public String role;

    /** A short free-form biography shown on profile pages. */
    public String aboutMe;

    /** A list of interest tags chosen by the user. */
    public List<String> interests;

    /** URL of the user's profile picture (typically from Firebase Storage). */
    public String profilePictureUrl;

    /** IDs of events that this user has applied to or joined. */
    public List<String> appliedEventIds;

    public List<String> invitedEventIds;   // Won lottery, pending acceptance
    public List<String> joinedEventIds;    // Accepted invitations

    /** IDs of user profiles that this user follows. */
    public List<String> followingIds;

    /** IDs of user profiles that follow this user. */
    public List<String> followerIds;

    /**
     * No-argument constructor required by Firestore.
     * Initializes list fields to avoid null pointer exceptions when newly
     * created Firestore documents are missing those fields.
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
    // Getters
    // (Firestore can read/write public fields, but getters make the model
    //  easier to use and safer for UI implementations.)
    // ----------------------------------------------------------------------

    /** @return the user’s Firestore document ID (FirebaseAuth UID). */
    public String getId() { return id; }

    /** @return the user’s display name. */
    public String getName() { return name; }

    /** @return the user’s email address. */
    public String getEmail() { return email; }

    /** @return the user’s role (entrant/organizer/admin). */
    public String getRole() { return role; }

    /** @return the biography text the user added to their profile. */
    public String getAboutMe() { return aboutMe; }

    /** @return the list of interests for this user. */
    public List<String> getInterests() { return interests; }

    /** @return URL for the user's profile picture. */
    public String getProfilePictureUrl() { return profilePictureUrl; }

    /** @return IDs of events the user has applied to. */
    public List<String> getAppliedEventIds() { return appliedEventIds; }

    /** @return IDs of events where the user has been selected but hasn't accepted yet. */
    public List<String> getInvitedEventIds() { return invitedEventIds; }

    /** @return IDs of events the user has officially accepted and joined. */
    public List<String> getJoinedEventIds() { return joinedEventIds; }

    /** @return IDs of users this user follows. */
    public List<String> getFollowingIds() { return followingIds; }

    /** @return IDs of users following this user. */
    public List<String> getFollowerIds() { return followerIds; }

    // ----------------------------------------------------------------------
    // Derived values
    // ----------------------------------------------------------------------

    /** @return the number of users this user follows. */
    public int getFollowingCount() {
        return (followingIds != null) ? followingIds.size() : 0;
    }

    /** @return the number of users who follow this user. */
    public int getFollowerCount() {
        return (followerIds != null) ? followerIds.size() : 0;
    }
}
