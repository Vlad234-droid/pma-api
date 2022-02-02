package com.tesco.pma.review.service.rest;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.review.LocalTestConfig;
import com.tesco.pma.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.review.util.TestDataUtils.files;
import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReviewEndpoint.class})
class ReviewEndpointTest extends AbstractEndpointTest {

    public static final String REVIEWS_FILES_URL = "/reviews/files";
    public static final String REVIEWS_FILES_URL_TEMPLATE = "/colleagues/{colleagueUuid}" + REVIEWS_FILES_URL;

    @MockBean
    private ReviewService mockReviewService;

    @MockBean
    private AuditorAware<UUID> mockAuditorAware;

    @MockBean
    private PMCycleService mockPmCycleService;

    @MockBean
    private FileService mockFileService;

    @MockBean
    private ProfileService mockProfileService;

    @Test
    void getReviewsFilesByColleagueWithColleague() throws Exception {
        var colleagueUuid = UUID.randomUUID();

        when(mockFileService.get(new RequestQuery(), false, colleagueUuid, true)).thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(colleagueUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void getReviewsFilesByColleagueWithLineManager() throws Exception {
        var colleagueUuid = UUID.randomUUID();
        var currentUserUuid = UUID.randomUUID();
        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setManagerUuid(currentUserUuid);

        when(mockProfileService.getColleague(colleagueUuid)).thenReturn(colleagueEntity);
        when(mockFileService.get(new RequestQuery(), false, colleagueUuid, true)).thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(currentUserUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void deleteFileByUuids() throws Exception {
        var colleagueUuid = UUID.randomUUID();
        var fileUuid = UUID.randomUUID();
        doNothing().when(mockFileService).delete(fileUuid, colleagueUuid);

        performDeleteWith(colleague(colleagueUuid.toString()), status().isOk(), REVIEWS_FILES_URL + "/" + fileUuid);
    }

    @Test
    void deleteFileByUuidsUnsuccessIfFileIsNotFound() throws Exception {
        var fileUuid = UUID.randomUUID();
        doThrow(NotFoundException.class).when(mockFileService).delete(fileUuid, null);

        performDeleteWith(roles(List.of(COLLEAGUE, ADMIN)), status().isNotFound(), REVIEWS_FILES_URL + "/" + fileUuid);
    }

}