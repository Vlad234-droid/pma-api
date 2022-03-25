package com.tesco.pma.config.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.config.parser.model.FieldSet;
import com.tesco.pma.config.parser.model.Value;
import com.tesco.pma.api.ValueType;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ColleagueEntityMapper {

    private static final String JOB_ID = "job_id";
    private static final String DEPARTMENT_ID = "department_id";
    private static final String DEPARTMENT_NAME = "department_name";
    private static final String BUSINESS_TYPE = "business_type";

    List<ColleagueEntity> mapColleagues(List<FieldSet> data, Set<ColleagueEntity.WorkLevel> workLevels,
                                        Set<ColleagueEntity.Country> countries, Set<ColleagueEntity.Department> departments,
                                        Set<ColleagueEntity.Job> jobs, Set<String> validationErrors) {

        var wlMap = workLevels.stream().collect(Collectors.toMap(ColleagueEntity.WorkLevel::getCode, Function.identity()));
        var countryMap = countries.stream().collect(Collectors.toMap(ColleagueEntity.Country::getCode, Function.identity()));
        var jobMap = jobs.stream().collect(Collectors.toMap(ColleagueEntity.Job::getId, Function.identity()));
        return data.stream()
                .map(FieldSet::getValues)
                .filter(fs -> !containsErrors(validationErrors, fs, "colleague_uuid"))
                .map(fs -> {
                    var colleague = new ColleagueEntity();
                    colleague.setUuid(getUuidValueNullSafe(fs, "colleague_uuid"));
                    colleague.setFirstName(getValueNullSafe(fs, "first_name"));
                    colleague.setLastName(getValueNullSafe(fs, "last_name"));
                    colleague.setEmail(getValueNullSafe(fs, "email"));
                    colleague.setWorkLevel(wlMap.get(getValueNullSafe(fs, "work_level")));
                    colleague.setPrimaryEntity(getValueNullSafe(fs, "primary_entity"));
                    colleague.setCountry(countryMap.get(getValueNullSafe(fs, "country_code")));
                    colleague.setDepartment(resolveDepartment(fs, departments));
                    colleague.setSalaryFrequency(getValueNullSafe(fs, "salary_frequency"));
                    colleague.setJob(jobMap.get(getValueNullSafe(fs, JOB_ID)));
                    colleague.setIamId(getValueNullSafe(fs, "iam_id"));
                    colleague.setIamSource(getValueNullSafe(fs, "iam_source"));
                    colleague.setManagerUuid(getUuidValueNullSafe(fs, "manager_uuid"));
                    colleague.setEmploymentType(getValueNullSafe(fs, "employment_type"));
                    colleague.setLegalEntity(getValueNullSafe(fs, "legal_employer_name"));
                    colleague.setLocationId(getValueNullSafe(fs, "location_uuid"));
                    colleague.setHireDate(getISODate(getValueNullSafe(fs, "hire_date")));
                    colleague.setLeavingDate(getISODate(getValueNullSafe(fs, "leaving_date")));
                    colleague.setManager(getBooleanValueNullSafe(fs, "is_manager", "1"));
                    return colleague;
                }).collect(Collectors.toList());
    }

    private static String getValueNullSafe(Map<String, Value> fs, String columnName) {
        var value = fs.get(columnName);
        if (value == null || StringUtils.isEmpty(value.getFormatted())) {
            return null;
        }
        return value.getFormatted();
    }

    private static boolean getBooleanValueNullSafe(Map<String, Value> fs, String columnName, String trueValue) {
        var value = fs.get(columnName);
        if (value == null || StringUtils.isEmpty(value.getFormatted())) {
            return false;
        }
        if (ValueType.BOOLEAN == value.getType()) {
            return Boolean.parseBoolean(value.getFormatted());
        }
        return trueValue.equalsIgnoreCase(value.getFormatted());
    }

    private static UUID getUuidValueNullSafe(Map<String, Value> fs, String columnName) {
        var value = fs.get(columnName);
        if (value == null || StringUtils.isEmpty(value.getFormatted())) {
            return null;
        }
        try {
            return UUID.fromString(value.getFormatted());
        } catch (IllegalArgumentException ex) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.METHOD_ARGUMENT_TYPE_MISMATCH, ex.getMessage()), ex);
            return null;
        }
    }

    private static boolean containsErrors(Set<String> errors, Map<String, Value> fs, String columnName) {
        var value = fs.get(columnName);
        return value == null || StringUtils.isEmpty(value.getFormatted()) || errors.contains(value.getFormatted());
    }

    Set<ColleagueEntity.WorkLevel> mapWLs(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .map(v -> v.get("work_level"))
                .filter(Objects::nonNull)
                .filter(v -> ValueType.BLANK != v.getType())
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

    Set<ColleagueEntity.Department> mapDepartments(List<FieldSet> data, List<ColleagueEntity.Department> existingDepartments) {
        var btNameToUuidMap = existingDepartments
                .stream()
                .map(ColleagueEntity.Department::getBusinessType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(ColleagueEntity.Department.BusinessType::getName,
                        ColleagueEntity.Department.BusinessType::getUuid));
        return data.stream()
                .map(FieldSet::getValues)
                .map(c -> getDepartment(existingDepartments, btNameToUuidMap, c))
                .filter(Objects::nonNull)
                .filter(distinctByKeys(ColleagueEntity.Department::getId,
                        ColleagueEntity.Department::getName, ColleagueEntity.Department::getBusinessType))
                .collect(Collectors.toSet());
    }

    private ColleagueEntity.Department getDepartment(Collection<ColleagueEntity.Department> existingDepartments,
                                                     Map<String, UUID> btNameToUuidMap, Map<String, Value> fields) {
        var id = getValueNullSafe(fields, DEPARTMENT_ID);
        var name = getValueNullSafe(fields, DEPARTMENT_NAME);
        var businessTypeString = getValueNullSafe(fields, BUSINESS_TYPE);
        if (StringUtils.isAllEmpty(id, name, businessTypeString)) {
            return null;
        }
        var department = new ColleagueEntity.Department();
        department.setUuid(resolveDepartmentUuid(existingDepartments, id, name, businessTypeString));
        department.setId(id);
        department.setName(name);
        Optional.ofNullable(businessTypeString)
                .ifPresent(bt -> {
                    var businessType = new ColleagueEntity.Department.BusinessType();
                    businessType.setUuid(btNameToUuidMap.computeIfAbsent(bt, s -> UUID.randomUUID()));
                    businessType.setName(bt);
                    department.setBusinessType(businessType);
                });
        return department;
    }

    UUID resolveDepartmentUuid(Collection<ColleagueEntity.Department> existingDepartments, String id,
                               String name, String businessTypeString) {
        return getDepartmentOptional(existingDepartments, id, name, businessTypeString)
                .map(ColleagueEntity.Department::getUuid)
                .orElseGet(UUID::randomUUID);
    }

    ColleagueEntity.Department resolveDepartment(Map<String, Value> fields, Collection<ColleagueEntity.Department> existingDepartments) {
        var id = getValueNullSafe(fields, DEPARTMENT_ID);
        var name = getValueNullSafe(fields, DEPARTMENT_NAME);
        var businessTypeString = getValueNullSafe(fields, BUSINESS_TYPE);
        return getDepartmentOptional(existingDepartments, id, name, businessTypeString)
                .orElse(null);
    }

    private Optional<ColleagueEntity.Department> getDepartmentOptional(Collection<ColleagueEntity.Department> existingDepartments,
                                                                       String id, String name, String businessTypeString) {
        return existingDepartments.stream()
                .filter(d -> Objects.equals(d.getId(), id) && Objects.equals(d.getName(), name)
                        && (d.getBusinessType() == null && businessTypeString == null || d.getBusinessType() != null
                        && Objects.equals(d.getBusinessType().getName(), businessTypeString)))
                .findFirst();
    }

    Set<ColleagueEntity.Job> mapJobs(List<FieldSet> data) {
        return data.stream()
                .map(FieldSet::getValues)
                .filter(c -> c.get(JOB_ID).getType() != ValueType.BLANK)
                .map(c -> {
                    var job = new ColleagueEntity.Job();
                    job.setId(getValueNullSafe(c, JOB_ID));
                    job.setName(getValueNullSafe(c, "job_name"));
                    return job;
                })
                .distinct()
                .collect(Collectors.groupingBy(ColleagueEntity.Job::getId))
                .values()
                .stream()
                .filter(list -> list.size() == 1)
                .map(CollectionUtils::firstElement)
                .collect(Collectors.toSet());
    }

    private LocalDate getISODate(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        var isoFormatter = DateTimeFormatter.ISO_INSTANT;
        var dateInstant = Instant.from(isoFormatter.parse(dateString));
        return LocalDate.ofInstant(dateInstant, ZoneOffset.UTC);
    }

    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

        return t -> {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }
}
