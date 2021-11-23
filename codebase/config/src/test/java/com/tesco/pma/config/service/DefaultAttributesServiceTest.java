package com.tesco.pma.config.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.config.dao.DefaultAttributesDAO;
import com.tesco.pma.config.domain.DefaultAttribute;
import com.tesco.pma.config.domain.DefaultAttributeCategory;
import com.tesco.pma.config.domain.DefaultAttributeCriteria;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class DefaultAttributesServiceTest {

    private final DefaultAttributesDAO defaultAttributesDAO = Mockito.mock(DefaultAttributesDAO.class);
    private final ProfileService profileService = Mockito.mock(ProfileService.class);
    private final ReviewService reviewService = Mockito.mock(ReviewService.class);
    private final NamedMessageSourceAccessor messages = Mockito.mock(NamedMessageSourceAccessor.class);

    private final DefaultAttributesService defaultAttributesService =
            new DefaultAttributesServiceImp(defaultAttributesDAO, profileService, reviewService, messages);

    @Test
    public void updateDefaultAttributesTest(){

        var colleagueId = UUID.randomUUID();
        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueId);

        var wl = new WorkRelationship();
        wl.setIsManager(true);
        colleague.setWorkRelationships(List.of(wl));

        var defaultAttrs = new ArrayList<DefaultAttribute>();
        defaultAttrs.add(createDefaultAttr("Test1", DefaultAttributeCriteria.ALL));
        defaultAttrs.add(createDefaultAttr("Test1-2", DefaultAttributeCriteria.ALL));
        defaultAttrs.add(createDefaultAttr("Test2", DefaultAttributeCriteria.LINE_MANAGER));
        defaultAttrs.add(createDefaultAttr("Test3", DefaultAttributeCriteria.MYR));

        var cycle = new PMCycleTimelinePoint();
        cycle.setReviewType(PMReviewType.MYR);

        var cycle2 = new PMCycleTimelinePoint();
        cycle2.setReviewType(PMReviewType.MYR);

        var colleagueProfile = new ColleagueProfile();
        colleagueProfile.setProfileAttributes(new ArrayList<>());

        Mockito.when(profileService.findColleagueByColleagueUuid(Mockito.eq(colleagueId)))
                .thenReturn(colleague);

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.eq(colleagueId)))
                .thenReturn(Optional.of(colleagueProfile));

        Mockito.when(reviewService.getCycleTimelineByColleague(Mockito.eq(colleagueId)))
                .thenReturn(List.of(cycle, cycle2));

        Mockito.when(defaultAttributesDAO.findByCriteriasAndCategory(Mockito.any(), Mockito.eq(DefaultAttributeCategory.NOTIFICATION)))
                .thenReturn(defaultAttrs);

        defaultAttributesService.updateDefaultAttributes(colleagueId);

        Mockito.verify(profileService, Mockito.times(1))
                .createProfileAttributes(Mockito.eq(colleagueId), Mockito.argThat(list -> list.size()==4));
    }

    private DefaultAttribute createDefaultAttr(String name, DefaultAttributeCriteria criteria){
        var defaultAttr = new DefaultAttribute();
        defaultAttr.setName(name);
        defaultAttr.setCategory(DefaultAttributeCategory.NOTIFICATION);
        defaultAttr.setCriteria(criteria);
        return defaultAttr;
    }

}
