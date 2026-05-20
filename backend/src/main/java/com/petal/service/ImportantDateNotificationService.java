package com.petal.service;

import com.petal.entity.SavedDate;

public interface ImportantDateNotificationService {
    void sendReminder(SavedDate savedDate);
}
