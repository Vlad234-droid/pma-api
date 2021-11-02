package com.tesco.pma.organisation.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SearchColleaguesService {

    private final UserService userService;
    private final ConfigEntryDAO configEntryDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    public List<Colleague> getAllSuggestions(String fullName){
        return getSuggestions(fullName, false);
    }

    public List<Colleague> getSuggestionsSubordinates(String fullName){
        return getSuggestions(fullName, true);
    }

    private List<Colleague> getSuggestions(String fullName, boolean isAmongSubordinates){
        UUID managerId = isAmongSubordinates? currentUserUUID(): null;
        fullName = fullName.trim();
        return configEntryDAO.findColleagueSuggestionsByFullName(fullName, managerId);
    }

    @NonNull
    private UUID currentUserUUID(){
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth==null){
            throw new AuthenticationCredentialsNotFoundException(messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_AUTHENTICATED));
        }

        return userService
                .findUserByAuthentication(auth, List.of(UserIncludes.SUBSIDIARY_PERMISSIONS))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_FOUND)))
                .getColleagueUuid();
    }

}
