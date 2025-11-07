package com.example.ballerevents;

import java.util.List;

public interface NotificationsRepository {
    interface Callback { void onResult(List<NotificationItem> items); }
    void getMyNotifications(Callback cb);
}
