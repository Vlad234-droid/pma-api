package com.tesco.pma.configuration.rest.model;

import com.tesco.pma.api.Subsidiary;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Subsidiary")
public abstract class AbstractSubsidiaryMixIn extends Subsidiary implements UuidReadOnlyMixIn {
}