package com.tesco.pma.review.domain;


import com.tesco.pma.api.GroupObjectiveStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GroupObjective {
    UUID uuid;
    Integer number;
    GroupObjectiveStatus status;
    String title;
    Integer version;
}
