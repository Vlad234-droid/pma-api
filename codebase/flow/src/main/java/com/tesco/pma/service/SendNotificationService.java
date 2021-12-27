package com.tesco.pma.service;

import com.tesco.pma.colleague.profile.domain.ColleagueProfile;

import java.util.Map;

public interface SendNotificationService {

    void send(ColleagueProfile colleagueProfile, String templateId, Map<String, String> placeholders);

}
