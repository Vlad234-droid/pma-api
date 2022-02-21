package com.tesco.pma.colleague.security.action;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.request.ColleagueAccountRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventException;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.colleague.security.exception.ErrorCodes.SECURITY_ACCOUNT_ALREADY_EXISTS;

/**
 * Process request on creation a new account
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateAccountByEventAction implements Action {

    private final ProfileService profileService;
    private final UserManagementService userManagementService;
    private final NamedMessageSourceAccessor messages;

    private static final String ACCOUNT_NAME_PARAMETER_NAME = "accountName";

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

        var request = new ColleagueAccountRequest();
        request.setName(colleague.getIamId());
        request.setIamId(colleague.getIamId());
        request.setType(AccountType.USER);
        request.setStatus(AccountStatus.ENABLED);
        request.setColleagueUuid(colleague.getUuid());

        try {
            userManagementService.createAccount(request);
        } catch (AlreadyExistsException e) {
            var params = Map.of(ACCOUNT_NAME_PARAMETER_NAME, request.getName());
            log.warn(LogFormatter.formatMessage(messages, SECURITY_ACCOUNT_ALREADY_EXISTS, params));
        }

    }

}
