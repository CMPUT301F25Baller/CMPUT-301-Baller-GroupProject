package com.example.ballerevents;

public class Entrant {
    private final String name;
    private final String status;
    private final int avatarResId;
    private final long timeStamp;

    public Entrant(String name, String status, int avatarResId, long timeStamp) {
        this.name = name;
        this.status = status;
        this.avatarResId = avatarResId;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }
    public String getStatus() {
        return status;
    }
    public int getAvatarResId() {
        return avatarResId;
    }
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timeStamp;

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;


        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");

        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";

        if (weeks < 4) return weeks + (weeks == 1 ? " week ago" : " weeks ago");

        if (months < 12)
            return months + (months == 1 ? " month ago" : " months ago");

        return years + (years == 1 ? " year ago" : " years ago");
    }

}
