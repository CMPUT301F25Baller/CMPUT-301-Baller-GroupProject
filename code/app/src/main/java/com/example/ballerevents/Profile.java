package com.example.ballerevents;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profile {

    @DocumentId
    private String id;

    private String name;
    private String email;
    private String aboutMe;
    private String profilePictureUrl;

    private List<String> interests;
    private List<String> appliedEventIds;

    // NEW: make these match your Firestore field names
    private List<String> following;   // e.g., ["uidA","uidB",...]
    private List<String> followers;   // e.g., ["uidX","uidY",...]

    public Profile() {
        // Firestore needs an empty ctor
    }

    // -------- Getters (return empty lists to avoid NPEs) --------
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAboutMe() { return aboutMe; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public List<String> getInterests() {
        return interests != null ? interests : Collections.emptyList();
    }

    public List<String> getAppliedEventIds() {
        return appliedEventIds != null ? appliedEventIds : Collections.emptyList();
    }

    public List<String> getFollowing() {
        return following == null ? new java.util.ArrayList<>() : following;
    }

    public List<String> getFollowers() {
        return followers != null ? followers : Collections.emptyList();
    }

    // (Optional) Setters if you need to update locally before writing to Firestore
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public void setAppliedEventIds(List<String> appliedEventIds) { this.appliedEventIds = appliedEventIds; }
    public void setFollowing(List<String> following) { this.following = following; }
    public void setFollowers(List<String> followers) { this.followers = followers; }
}
