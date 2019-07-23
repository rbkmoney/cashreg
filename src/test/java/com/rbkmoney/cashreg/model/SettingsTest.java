package com.rbkmoney.cashreg.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SettingsTest {

    @Test
    public void prepareMap() {
        Settings settings = new Settings();
        settings.setClientId("clientId");

        Map<String, String> expected = new HashMap<>();
        expected.put("client_id", "clientId");

        assertEquals(expected, settings.prepareMap());
    }
}