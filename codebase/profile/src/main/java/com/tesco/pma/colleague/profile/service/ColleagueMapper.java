package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.parser.model.FieldSet;
import com.tesco.pma.colleague.profile.parser.model.ValueType;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class ColleagueMapper {
    List<ColleagueEntity> mapColleagues(List<FieldSet> data, Set<ColleagueEntity.WorkLevel> workLevels,
                                        Set<ColleagueEntity.Country> countries, Set<ColleagueEntity.Department> departments,
                                        Set<ColleagueEntity.Job> jobs) {
        var wlMap = workLevels.stream().collect(Collectors.toMap(ColleagueEntity.WorkLevel::getCode, Function.identity()));
        var countryMap = countries.stream().collect(Collectors.toMap(ColleagueEntity.Country::getCode, Function.identity()));
        var depMap = departments.stream().collect(Collectors.toMap(ColleagueEntity.Department::getId, Function.identity()));
        var jobMap = jobs.stream().collect(Collectors.toMap(ColleagueEntity.Job::getId, Function.identity()));
        return data.stream()
                .map(FieldSet::getValues)
                .map(fs -> {
                    var colleague = new ColleagueEntity();
                    colleague.setUuid(UUID.fromString(fs.get("colleague_uuid").getFormatted()));
                    /*
                    colleague.setFirstName(fs.get("").getFormatted());
                    colleague.setLastName(fs.get("").getFormatted());
                    colleague.setEmail(fs.get("").getFormatted());
                     */
                    colleague.setWorkLevel(wlMap.get(fs.get("work_level").getFormatted()));
                    colleague.setPrimaryEntity(fs.get("primary_entity").getFormatted());
                    colleague.setCountry(countryMap.get(fs.get("country_code").getFormatted()));
                    colleague.setDepartment(depMap.get(fs.get("department_id").getFormatted()));
                    colleague.setSalaryFrequency(fs.get("salary_frequency").getFormatted());
                    colleague.setJob(jobMap.get(fs.get("job_id").getFormatted()));
                    colleague.setIamId(fs.get("iam_id").getFormatted());
                    colleague.setIamSource(fs.get("iam_source").getFormatted());
                    colleague.setManagerUuid(StringUtils.isNotEmpty(fs.get("manager_uuid").getFormatted())
                            ? UUID.fromString(fs.get("manager_uuid").getFormatted()) : null);
                    colleague.setEmploymentType(fs.get("employment_type").getFormatted());
                    colleague.setHireDate(getISODate(fs.get("hire_date").getFormatted()));
                    colleague.setLeavingDate(getISODate(fs.get("leaving_date").getFormatted()));
                    colleague.setManager("1".equals(fs.get("is_manager").getFormatted()));
                    return colleague;
                }).collect(Collectors.toList());
    }

    Set<ColleagueEntity.WorkLevel> mapWLs(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .map(v -> v.get("work_level"))
                .filter(Objects::nonNull)
                .distinct()
                .map(wl -> {
                    var workLevel = new ColleagueEntity.WorkLevel();
                    workLevel.setCode(wl.getFormatted());
                    workLevel.setName(wl.getFormatted());
                    return workLevel;
                }).collect(Collectors.toSet());
    }

    Set<ColleagueEntity.Country> mapCountries(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .map(v -> v.get("country_code"))
                .filter(Objects::nonNull)
                .distinct()
                .map(c -> {
                    var country = new ColleagueEntity.Country();
                    country.setCode(c.getFormatted());
                    country.setName(c.getFormatted());
                    return country;
                }).collect(Collectors.toSet());
    }

    Set<ColleagueEntity.Department> mapDepartments(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .filter(c -> c.get("department_id").getType() != ValueType.BLANK)
                .map(c -> {
                    var department = new ColleagueEntity.Department();
                    department.setId(c.get("department_id").getFormatted());
                    department.setName(c.get("department_name").getFormatted());
                    department.setBusinessType(c.get("business_type").getFormatted());
                    return department;
                }).collect(Collectors.toSet());
    }

    Set<ColleagueEntity.Job> mapJobs(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .filter(c -> c.get("job_id").getType() != ValueType.BLANK)
                .map(c -> {
                    var job = new ColleagueEntity.Job();
                    job.setId(c.get("job_id").getFormatted());
                    job.setName(c.get("job_name").getFormatted());
                    return job;
                }).collect(Collectors.toSet());
    }

    private LocalDate getISODate(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        var isoFormatter = DateTimeFormatter.ISO_INSTANT;
        var dateInstant = Instant.from(isoFormatter.parse(dateString));
        return LocalDate.ofInstant(dateInstant, ZoneOffset.UTC);
    }
}
