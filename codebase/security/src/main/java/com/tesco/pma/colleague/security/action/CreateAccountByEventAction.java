package com.tesco.pma.colleague.security.action;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.exception.AccountAlreadyExistsException;
import com.tesco.pma.colleague.security.exception.DuplicatedAccountException;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Process request on creation a new account
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateAccountByEventAction implements Action {

    private final ProfileService profileService;
    private final UserManagementService userManagementService;

    @Override
    public EventResponse perform(Event event) throws EventException {
        var eventProperties = event.getEventProperties();
        if (eventProperties.containsKey(EventParams.COLLEAGUE_UUID.name())) {
            var colleagueUuid = UUID.fromString(eventProperties.get(EventParams.COLLEAGUE_UUID.name()).toString());
            createAccount(colleagueUuid);
        }
        return new EventResponseSupport(event.getEventName(), EventResponse.END);
    }

    private void createAccount(UUID colleagueUuid) {
        var colleague = profileService.getColleague(colleagueUuid);
        if (colleague == null) {
            return;
        }

        var request = new CreateAccountRequest();
        request.setName(colleague.getIamId());
        request.setIamId(colleague.getIamId());
        request.setType(AccountType.USER);
        request.setStatus(AccountStatus.ENABLED);

        try {
            userManagementService.createAccount(request);
        } catch (AccountAlreadyExistsException | DuplicatedAccountException e) {
            log.error("Can't create account : {}", e.getMessage());
        }

    }

}
