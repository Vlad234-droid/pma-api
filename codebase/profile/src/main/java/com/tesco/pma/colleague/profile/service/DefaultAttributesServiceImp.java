package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.dao.DefaultAttributesDAO;
import com.tesco.pma.colleague.profile.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultAttributesServiceImp implements DefaultAttributesService {

    private final DefaultAttributesDAO defaultAttributesDAO;
    private final ProfileService profileService;

    public void updateDefaultAttributes(UUID colleagueId) {

        var colleague = profileService.findColleagueByColleagueUuid(colleagueId);
        var colleagueCriterias = defineCriterias(colleague);

        var defaultAttributes =
                defaultAttributesDAO.findByCriteriasAndCategory(colleagueCriterias, DefaultAttributeCategory.NOTIFICATION);

        populateDefaultAttributes(colleague, defaultAttributes);
    }

    private List<DefaultAttributeCriteria> defineCriterias(Colleague colleague) {

        List<DefaultAttributeCriteria> result = new ArrayList<>();
        result.add(DefaultAttributeCriteria.ALL_COLLEAGUES);

        if (colleague.getWorkRelationships().get(0).getIsManager()) {
            result.add(DefaultAttributeCriteria.LINE_MANAGER_ONLY);
        }

        var workLevel = colleague.getWorkRelationships().get(0).getWorkLevel();

        if(workLevel == WorkRelationship.WorkLevel.WL4 || workLevel == WorkRelationship.WorkLevel.WL5) {
            result.add(DefaultAttributeCriteria.WK_4_5_ONLY);
        }

        //TODO

        return result;
    }

    private void populateDefaultAttributes(Colleague colleague, List<DefaultAttribute> attributes) {

        var profileAttributesNames = profileService.findProfileAttributes(colleague.getColleagueUUID()).stream()
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

}
