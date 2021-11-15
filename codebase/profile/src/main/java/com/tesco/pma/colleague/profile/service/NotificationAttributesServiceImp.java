package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.NotificationConfigsDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationAttributesServiceImp {

    private final NotificationConfigsDAO notificationConfigsDAO;

    public void createNotificationAttributesForColleague(UUID colleague){
        //TODO
    }

}
