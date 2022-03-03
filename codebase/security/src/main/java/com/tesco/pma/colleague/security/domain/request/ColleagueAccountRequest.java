package com.tesco.pma.colleague.security.domain.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ColleagueAccountRequest extends CreateAccountRequest {
    private UUID colleagueUuid;
}
