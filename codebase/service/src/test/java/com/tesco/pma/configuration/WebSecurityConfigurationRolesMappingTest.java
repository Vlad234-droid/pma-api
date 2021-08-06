package com.tesco.pma.configuration;

import com.tesco.pma.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
@Import(WebSecurityConfiguration.RolesMappingConfiguration.class)
@TestPropertySource(properties = {
        "tesco.application.security.role-mapping.TARGET_ONE_TO_ONE[0]=SOURCE_ONE_TO_ONE",
        "tesco.application.security.role-mapping.TARGET_ONE_TO_MULTI_1[0]=SOURCE_ONE_TO_MULTI",
        "tesco.application.security.role-mapping.TARGET_ONE_TO_MULTI_2[0]=SOURCE_ONE_TO_MULTI",
        "tesco.application.security.role-mapping.TARGET_MULTI_TO_ONE[0]=SOURCE_MULTI_TO_ONE_1",
        "tesco.application.security.role-mapping.TARGET_MULTI_TO_ONE[1]=SOURCE_MULTI_TO_ONE_2"
})
class WebSecurityConfigurationRolesMappingTest {
    @Autowired
    private Attributes2GrantedAuthoritiesMapper mapper;

    @Test
    void rolesMappingOneToOne() {
        final var res = mapper.getGrantedAuthorities(List.of("SOURCE_ONE_TO_ONE"));

        assertThat(res).extracting(GrantedAuthority::getAuthority)
                .containsExactly("TARGET_ONE_TO_ONE");
    }

    @Test
    void rolesMappingSingleSourceToMultiTarget() {
        final var res = mapper.getGrantedAuthorities(List.of("SOURCE_ONE_TO_MULTI"));

        assertThat(res).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("TARGET_ONE_TO_MULTI_1", "TARGET_ONE_TO_MULTI_2");
    }

    @Test
    void rolesMappingSingleTargetToMultiSource() {
        final var res1 = mapper.getGrantedAuthorities(List.of("SOURCE_MULTI_TO_ONE_1"));
        final var res2 = mapper.getGrantedAuthorities(List.of("SOURCE_MULTI_TO_ONE_2"));

        assertThat(res1).isEqualTo(res2)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("TARGET_MULTI_TO_ONE");
    }

    @Test
    void rolesMappingAll() {
        final var res = mapper.getGrantedAuthorities(List.of(
                "SOURCE_ONE_TO_ONE",
                "SOURCE_ONE_TO_MULTI",
                "SOURCE_MULTI_TO_ONE_1",
                "SOURCE_MULTI_TO_ONE_2"));

        assertThat(res).extracting(GrantedAuthority::getAuthority)
                .containsOnly(
                        "TARGET_ONE_TO_ONE",
                        "TARGET_ONE_TO_MULTI_1", "TARGET_ONE_TO_MULTI_2",
                        "TARGET_MULTI_TO_ONE");
    }
}