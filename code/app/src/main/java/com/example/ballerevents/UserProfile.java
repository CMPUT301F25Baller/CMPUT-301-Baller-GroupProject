package com.example.ballerevents;

import java.util.List;

public class UserProfile {
    private String name;
    private int followingCount;
    private int followerCount;
    private String aboutMe;
    private List<String> interests;
    private int profilePictureResId;

    public UserProfile(String name, int followingCount, int followerCount, String aboutMe, List<String> interests, int profilePictureResId) {
        this.name = name;
        this.followingCount = followingCount;
        this.followerCount = followerCount;
        this.aboutMe = aboutMe;
        this.interests = interests;
        this.profilePictureResId = profilePictureResId;
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getFollowingCount() { return followingCount; }
    public int getFollowerCount() { return followerCount; }
    public String getAboutMe() { return aboutMe; }
    public List<String> getInterests() { return interests; }
    public int getProfilePictureResId() { return profilePictureResId; }

    // --- Setters (for editing) ---
    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
}