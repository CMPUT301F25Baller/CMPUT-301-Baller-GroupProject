package com.example.ballerevents;

import java.util.Collections;

public class StubNotificationsRepository implements NotificationsRepository {
    @Override
    public void getMyNotifications(Callback cb) {
        // EMPTY on purpose (Half-way checkpoint)
        cb.onResult(Collections.emptyList());
    }
}
