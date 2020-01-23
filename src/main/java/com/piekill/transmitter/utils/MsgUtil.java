package com.piekill.transmitter.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

public class MsgUtil {
    private static final String groupId = "Transmitter";

    public static Notification showMsg(String msg, NotificationType error) {
        Notification notification = new Notification(groupId, groupId, msg, error);
        Notifications.Bus.notify(notification);
        return notification;
    }
}
