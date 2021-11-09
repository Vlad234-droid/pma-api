package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";

    @Autowired
    private ProfileDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues-config.xml"})
    void getColleagueByIamId() {
        var colleague = dao.getColleagueByIamId("TPX1");

        assertNotNull(colleague);
        assertEquals(UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935"), colleague.getUuid());
        assertNotNull(colleague.getCountry());
        assertNotNull(colleague.getDepartment());
        assertNotNull(colleague.getJob());
        assertNotNull(colleague.getWorkLevel());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForGettingColleagues")
    @DataSet({BASE_PATH_TO_DATA_SET + "colleagues-config.xml"})
    void readColleaguesByKey(String key, Set<UUID> colleagueUuids) {
        var colleagues = dao.findColleaguesByTypes(key);

        assertEquals(colleagueUuids.size(), colleagues.size());

        var uuids = colleagues.stream()
                .map(ColleagueEntity::getUuid)
                .collect(Collectors.toSet());

        assertTrue(colleagueUuids.containsAll(uuids));

    }

    private static Stream<Arguments> provideArgsForGettingColleagues() { //NOPMD
        var e1 = UUID.fromString("b919b9f4-7a25-42e0-805d-e351c4e33ff9");
        var e2 = UUID.fromString("03121555-7246-4b03-b2ed-718cf81d4d31");
        var e3 = UUID.fromString("1efbf78b-d413-4dbb-990d-b05affb4d11e");
        var e4 = UUID.fromString("b5c74f42-665d-4c9e-9b00-2682eaabe592");
        var e5 = UUID.fromString("71eb5890-4f41-4d70-a3a0-0071fcfd2edb");
        var e11 = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");
        var e21 = UUID.fromString("119e0d2b-1dc2-409f-8198-ecd66e59d47a");
        var e31 = UUID.fromString("45fd1870-9745-41c0-90b5-a902cfca6961");
        var e12 = UUID.fromString("b5f79bef-7905-400f-8cb6-d0ebe41b961c");
        var e22 = UUID.fromString("1e0f2f8a-5d8f-428b-a7c1-9bc0fe214a85");
        var e13 = UUID.fromString("3ca4807c-b08b-4d53-b3a4-0c3d26868720");
        var e23 = UUID.fromString("2c8ba89a-2756-42cc-9504-e136cdb94782");
        return Stream.of(
                Arguments.of(
                        "group",
                        Set.of(e1, e2, e3)),
                Arguments.of(
                        "group/ho_c",
                        Set.of(e1, e2, e3)),
                Arguments.of(
                        "group/ho_c/salaried",
                        Set.of(e1, e2, e3)),
                Arguments.of(
                        "group/ho_c/salaried/wl3",
                        Set.of(e1)),
                Arguments.of(
                        "group/ho_c/salaried/wl4",
                        Set.of(e2)),
                Arguments.of(
                        "group/ho_c/salaried/wl5",
                        Set.of(e3)),
                Arguments.of(
                        "uk",
                        Set.of(e1, e2, e3, e4, e5)),
                Arguments.of(
                        "uk/ho",
                        Set.of(e4, e5)),
                Arguments.of(
                        "uk/ho/salaried",
                        Set.of(e4, e5)),
                Arguments.of(
                        "uk/ho/salaried/wl1",
                        Set.of(e4)),
                Arguments.of(
                        "uk/ho/salaried/wl2",
                        Set.of(e5)),
                Arguments.of(
                        "uk/channels",
                        Set.of(e11, e21, e31)),
                Arguments.of(
                        "uk/channels/salaried",
                        Set.of(e11, e21)),
                Arguments.of(
                        "uk/channels/salaried/wl2_on_bonus",
                        Set.of(e11)),
                Arguments.of(
                        "uk/channels/salaried/wl1",
                        Set.of(e21)),
                Arguments.of(
                        "uk/channels/salaried/wl2_tl",
                        Set.of(e11)),
                Arguments.of(
                        "uk/channels/hp",
                        Set.of(e31)),
                Arguments.of(
                        "uk/channels/hp/colleague",
                        Set.of(e31)),
                Arguments.of(
                        "roi/ho_c",
                        Set.of(e12, e22)),
                Arguments.of(
                        "roi/ho_c/salaried",
                        Set.of(e12, e22)),
                Arguments.of(
                        "roi/ho_c/salaried/wl1",
                        Set.of(e12)),
                Arguments.of(
                        "roi/ho_c/salaried/wl2",
                        Set.of(e22)),
                Arguments.of(
                        "india",
                        Set.of(e13, e23)),
                Arguments.of(
                        "india/ho",
                        Set.of(e13, e23)),
                Arguments.of(
                        "india/ho/salaried",
                        Set.of(e13, e23)),
                Arguments.of(
                        "india/ho/salaried/wl1",
                        Set.of(e13)),
                Arguments.of(
                        "india/ho/salaried/wl2",
                        Set.of(e23)),
                Arguments.of(
                        "uk_m",
                        Set.of(e13, e23)),
                Arguments.of(
                        "uk_m/salaried",
                        Set.of(e13, e23)),
                Arguments.of(
                        "uk_m/salaried/wl1",
                        Set.of(e13)),
                Arguments.of(
                        "uk_m/salaried/wl2",
                        Set.of(e23))
        );
    }
}
