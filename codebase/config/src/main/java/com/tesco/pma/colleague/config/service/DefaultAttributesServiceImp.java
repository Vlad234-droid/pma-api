package com.tesco.pma.colleague.config.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.config.dao.DefaultAttributesDAO;
import com.tesco.pma.colleague.config.domain.DefaultAttributeCategory;
import com.tesco.pma.colleague.config.domain.DefaultAttributeCriteria;
import com.tesco.pma.colleague.config.domain.DefaultAttribute;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.domain.AttributeType;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultAttributesServiceImp implements DefaultAttributesService {

    private static final String Q1_CODE_VALUE = "Q1";
    private static final String Q3_CODE_VALUE = "Q3";

    private final DefaultAttributesDAO defaultAttributesDAO;
    private final ProfileService profileService;
    private final ReviewService reviewService;
    private final NamedMessageSourceAccessor messages;

    @Override
    public void updateDefaultAttributes(UUID colleagueId) {

        var colleague = profileService.findColleagueByColleagueUuid(colleagueId);
        var colleagueCriterias = defineCriterias(colleague);

        var defaultAttributes =
                defaultAttributesDAO.findByCriteriasAndCategory(colleagueCriterias, DefaultAttributeCategory.NOTIFICATION);

        populateDefaultAttributes(colleague, defaultAttributes);
    }

    private List<DefaultAttributeCriteria> defineCriterias(Colleague colleague) {

        var result = new ArrayList<DefaultAttributeCriteria>();
        result.add(DefaultAttributeCriteria.ALL);

        if (!CollectionUtils.isEmpty(colleague.getWorkRelationships())) {
            var workRel = colleague.getWorkRelationships().get(0);

            if (workRel.getIsManager()) {
                result.add(DefaultAttributeCriteria.LINE_MANAGER);
            }

            var workLevel = workRel.getWorkLevel();

            if (workLevel == WorkRelationship.WorkLevel.WL4 || workLevel == WorkRelationship.WorkLevel.WL5) {
                result.add(DefaultAttributeCriteria.WL_4_OR_5);
            }
        }

        var timelinePoints = reviewService.getCycleTimelineByColleague(colleague.getColleagueUUID());

        for (PMCycleTimelinePoint timelinePoint : timelinePoints) {

            if (timelinePoint.getReviewType() == PMReviewType.MYR) {
                result.add(DefaultAttributeCriteria.MYR);
            }

            if (timelinePoint.getReviewType() == PMReviewType.OBJECTIVE) {
                result.add(DefaultAttributeCriteria.OBJECTIVE);
            }

            if (timelinePoint.getType() == PMElementType.TIMELINE_POINT) {
                if (Q1_CODE_VALUE.equals(timelinePoint.getCode())) {
                    result.add(DefaultAttributeCriteria.Q1);
                }

                if (Q3_CODE_VALUE.equals(timelinePoint.getCode())) {
                    result.add(DefaultAttributeCriteria.Q3);
                }
            }
        }

        return result;
    }

    private void populateDefaultAttributes(Colleague colleague, List<DefaultAttribute> attributes) {

        var profileAttributesNames = profileService.findProfileByColleagueUuid(colleague.getColleagueUUID())
                .orElseThrow(() -> profileNotFound("colleague UUID", colleague.getColleagueUUID()))
                .getProfileAttributes().stream()
                .map(TypedAttribute::getName)
                .collect(Collectors.toSet());

        var attrToAdd = attributes.stream()
                .filter(attr -> !profileAttributesNames.contains(attr.getName()))
                .map(attr -> this.createTypedAttribute(colleague, attr))
                .collect(Collectors.toList());

        profileService.createProfileAttributes(colleague.getColleagueUUID(), attrToAdd);
    }

    private TypedAttribute createTypedAttribute(Colleague colleague, DefaultAttribute attribute) {
        var typedAttribute = new TypedAttribute();
        typedAttribute.setColleagueUuid(colleague.getColleagueUUID());
        typedAttribute.setName(attribute.getName());
        typedAttribute.setValue(attribute.getValue());
        typedAttribute.setType(AttributeType.BOOLEAN);
        return typedAttribute;
    }

    private NotFoundException profileNotFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(),
                messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}
