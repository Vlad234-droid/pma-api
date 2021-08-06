package com.tesco.pma.configuration.cep;

import lombok.Data;

import java.util.Map;

@Data
public class CEPProperties {
    /**
     * Used to validate CEP 'sub' from token according to
     * <a href='https://github.dev.global.tesco.org/24Colleague/ColleagueEventingPlatform/wiki/Generic-Eventing-Platform-Onboarding#additional-technical-details'>this</a>.
     */
    private String subject;
    private PublishEvent publish;

    @Data
    public static class PublishEvent {
        private String url;
        private Map<String, Event> events;
    }

    @Data
    public static class Event {
        private String feedId;
    }
}
