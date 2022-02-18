package com.tesco.pma.cms.rest;

import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.cms.service.HelpService;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-15 21:00
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/cms")
public class CMSEndpoint {
    private final HelpService helpService;
    private final ProfileService profileService;

    @Operation(summary = "Find URLs by keys", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Find URLs by keys")
    @GetMapping(path = "/help-faq-urls", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<Map<String, String>> getHelpFaqUrls(@RequestParam(required = false) List<String> keys,
                                                            @CurrentSecurityContext(expression = "authentication")
                                                                    Authentication authentication) {
        var colleagueProfile = profileService.findProfileByColleagueUuid(SecurityUtils.getColleagueUuid(authentication));
        return colleagueProfile.map(profile -> RestResponse.success(helpService.getHelpFaqUrls(profile.getColleague(), keys)))
                .orElseGet(() -> RestResponse.success(Collections.emptyMap()));
    }

    @Operation(summary = "Find Content Entries by keys", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Find Content Entries by keys")
    @GetMapping(path = "/help-faq-content-entries", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<Map<String, List<ContentEntry>>> getHelpFaqContentEntries(@RequestParam(required = false) List<String> keys,
                                                                                  @CurrentSecurityContext(expression = "authentication")
                                                                    Authentication authentication) {
        var colleagueProfile = profileService.findProfileByColleagueUuid(SecurityUtils.getColleagueUuid(authentication));
        return colleagueProfile.map(profile -> RestResponse.success(helpService.getHelpFaqContentEntries(profile.getColleague(), keys)))
                .orElseGet(() -> RestResponse.success(Collections.emptyMap()));
    }


}
