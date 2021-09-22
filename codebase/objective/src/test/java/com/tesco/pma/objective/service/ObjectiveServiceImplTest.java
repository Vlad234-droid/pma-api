package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.LocalTestConfig;
import com.tesco.pma.objective.dao.ObjectiveDAO;
import com.tesco.pma.objective.domain.PersonalObjective;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ObjectiveServiceImplTest {

    final static String PERSONAL_OBJECTIVE_NOT_FOUND_MESSAGE =
            "Personal Objective not found for personalObjectiveUuid=ddb9ab0b-f50f-4442-8900-b03777ee0011";

    @Autowired
    private NamedMessageSourceAccessor messages;

    @MockBean
    private ObjectiveDAO mockProfileDAO;

    private ObjectiveServiceImpl objectiveService;

    @BeforeEach
    void setUp() {
        objectiveService = new ObjectiveServiceImpl(mockProfileDAO, messages);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getPersonalObjectiveByUuidShouldReturnPersonalObjective() {
        final var personalObjectiveUuid = UUID.randomUUID();
        final var expectedPersonalObjective = PersonalObjective.builder().build();

        when(mockProfileDAO.getPersonalObjective(any(UUID.class)))
                .thenReturn(expectedPersonalObjective);

        final var res = objectiveService.getPersonalObjectiveByUuid(personalObjectiveUuid);

        assertThat(res).isSameAs(expectedPersonalObjective);
    }

    @Test
    void updatePersonalObjectiveShouldReturnUpdatedPersonalObjective() {
        final var expectedPersonalObjective = PersonalObjective.builder().build();

        when(mockProfileDAO.updatePersonalObjective(any(PersonalObjective.class)))
                .thenReturn(1);

        final var res = objectiveService.updatePersonalObjective(expectedPersonalObjective);

        assertThat(res).isSameAs(expectedPersonalObjective);
    }

    @Test
    void createPersonalObjectiveShouldReturnCreatedPersonalObjective() {
        final var expectedPersonalObjective = PersonalObjective.builder().build();

        when(mockProfileDAO.createPersonalObjective(any(PersonalObjective.class)))
                .thenReturn(1);

        final var res = objectiveService.createPersonalObjective(expectedPersonalObjective);

        assertThat(res).isSameAs(expectedPersonalObjective);
    }

    @Test
    void deletePersonalObjectiveNotExists() {
        final var personalObjectiveUuid = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
        when(mockProfileDAO.deletePersonalObjective(any(UUID.class)))
                .thenReturn(0);
        final var exception = assertThrows(NotFoundException.class,
                () -> objectiveService.deletePersonalObjective(personalObjectiveUuid));

        assertEquals(PERSONAL_OBJECTIVE_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(PERSONAL_OBJECTIVE_NOT_FOUND_MESSAGE, exception.getMessage());

    }
}
