package com.tesco.pma.tip.util;

import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.tip.api.Tip;

import java.util.UUID;

public final class TestDataUtil {

    public static final UUID TIP_UUID = UUID.fromString("982b2124-b712-429f-aad1-656cbcadc1b5");
    public static final UUID TIP_UNPUBLISHED_UUID = UUID.fromString("80741a5e-c41c-47a0-9181-d202e9d077b7");
    public static final UUID TARGET_ORGANISATION_UUID = UUID.fromString("56141037-6e2d-45f0-b47f-4875e68dd1d7");
    public static final String TIP_TITLE = "title";
    public static final String TIP_DESCRIPTION = "description";
    public static final String TIP_IMAGE_LINK = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png";

    private TestDataUtil() {
    }

    public static Tip buildTip() {
        Tip tip = new Tip();
        tip.setTitle(TIP_TITLE);
        tip.setDescription(TIP_DESCRIPTION);
        ConfigEntry targetOrganisation = new ConfigEntry();
        targetOrganisation.setUuid(TARGET_ORGANISATION_UUID);
        tip.setTargetOrganisation(targetOrganisation);
        tip.setImageLink(TIP_IMAGE_LINK);
        return tip;
    }

}
