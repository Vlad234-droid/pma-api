package com.tesco.pma.review.service.rest;

import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.review.LocalTestConfig;
import com.tesco.pma.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static com.tesco.pma.review.util.TestDataUtils.files;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReviewEndpoint.class})
class ReviewEndpointTest extends AbstractEndpointTest {

    public static final String REVIEWS_FILES_URL_TEMPLATE = "/colleagues/{colleagueUuid}/reviews/files";

    @MockBean
    private ReviewService mockReviewService;

    @MockBean
    private AuditorAware<UUID> mockAuditorAware;

    @MockBean
    private PMCycleService mockPmCycleService;

    @Test
    void getReviewsFilesByColleagueWithColleague() throws Exception {
        UUID colleagueUuid = UUID.randomUUID();

        when(mockReviewService.getReviewsFilesByColleague(colleagueUuid, colleagueUuid, new RequestQuery()))
                .thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(colleagueUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void getReviewsFilesByColleagueWithLineManager() throws Exception {
        UUID colleagueUuid = UUID.randomUUID();
        UUID currentUserUuid = UUID.randomUUID();

        when(mockReviewService.getReviewsFilesByColleague(colleagueUuid, currentUserUuid, new RequestQuery()))
                .thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(currentUserUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

}