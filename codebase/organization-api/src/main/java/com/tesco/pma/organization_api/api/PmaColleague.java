package com.tesco.pma.organization_api.api;

import com.tesco.pma.service.colleague.client.model.workrelationships.Department;
import com.tesco.pma.service.colleague.client.model.workrelationships.Grade;
import com.tesco.pma.service.colleague.client.model.workrelationships.Job;
import com.tesco.pma.service.colleague.client.model.workrelationships.Position;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PmaColleague {
    private UUID uuid;
    private String employeeId;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String email;
    private String workPhoneNumber;

    private Department department;
    private Job job;
    private Grade grade;
    private Position position;

    public PmaColleague(UUID uuid) {
        this.uuid = uuid;
    }
}
