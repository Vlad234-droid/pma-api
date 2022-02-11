package com.tesco.pma.cms.controller;

import com.tesco.pma.cms.controller.dto.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentEndpointTest {


    @Test
    void keyTest() {
        var expect = "knowledge-library/gb/manager/iam1/content1";

        var key = new Key();
        key.setContent("content1");
        key.setIam("iam1");
        key.setRole("manager");
        key.setCountryCode("gb");

        assertEquals(expect, Key.createKey("knowledge-library", key.toString()));
    }

}
