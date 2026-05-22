package com.petal.service;

import com.petal.entity.SavedDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingImportantDateNotificationService implements ImportantDateNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingImportantDateNotificationService.class);

    @Override
    public void sendReminder(SavedDate savedDate) {
        log.info("Forget-Me-Not reminder due for user {} and saved date {}",
                savedDate.getUser().getId(),
                savedDate.getId());
    }
}
