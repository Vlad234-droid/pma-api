package com.tesco.pma.api.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Used to give the user access to a particular subsidiary under a particular role.
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SubsidiaryPermission {
    private UUID colleagueUuid;
    private UUID subsidiaryUuid;
    private String role;
}