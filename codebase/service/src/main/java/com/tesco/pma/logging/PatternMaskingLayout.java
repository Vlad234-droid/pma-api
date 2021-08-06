package com.tesco.pma.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Custom layout for hiding sensitive data, defined by regex in the logback.xml config.
 * Found sensitive data will be replaced with '*'.
 * PatternMaskingLayout can have multiple values for hiding, defined by 'maskPattern' property.
 * Example of configuring multiple patterns for hiding:
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *         <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
 *             <layout class="PatternMaskingLayout">
 *                 <maskPattern>([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)</maskPattern>
 *                 <maskPattern>password:?\s*(\w+)</maskPattern>
 *                 <pattern>%date{ISO8601,UTC} %level ${HOSTNAME} [%thread] %logger{20} %msg%n</pattern>
 *             </layout>
 *         </encoder>
 *     </appender>
 */
public class PatternMaskingLayout extends PatternLayout {
    private Pattern multilinePattern;
    private final List<String> maskPatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern
                .compile(maskPatterns.stream()
                                .collect(Collectors.joining("|")),
                        Pattern.MULTILINE);
    }


    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event));
    }

    private String maskMessage(String message) {

        if (multilinePattern == null) {
            return message;
        }

        var sb = new StringBuilder(message);
        var matcher = multilinePattern.matcher(sb);
        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount()).forEach(
                    group -> {
                        if (matcher.group(group) != null) {
                            IntStream.range(matcher.start(group), matcher.end(group))
                                    .forEach(i -> sb.setCharAt(i, '*'));
                        }
                    }
            );
        }
        return sb.toString();
    }
}
