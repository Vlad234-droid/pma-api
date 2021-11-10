package com.tesco.pma.configuration.tomcat;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.TomcatServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class TrustedConnectorConfiguration {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${management.server.port:8090}")
    private String managementPort;

    @Value("${server.trusted-port:8081}")
    private String trustedPort;

    /**
     *
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
        Connector[] additionalConnectors = this.additionalConnector();
        ServerProperties serverProperties = new ServerProperties();
        return new TomcatMultiConnectorServletWebServerFactoryCustomizer(serverProperties, additionalConnectors);
    }

    private Connector[] additionalConnector() {

        Set<String> defaultPorts = new HashSet<>();
        defaultPorts.add(serverPort);
        defaultPorts.add(managementPort);

        if (!defaultPorts.contains(trustedPort)) {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(Integer.parseInt(trustedPort));
            return new Connector[]{connector};
        } else {
            return new Connector[]{};
        }
    }

    private static class TomcatMultiConnectorServletWebServerFactoryCustomizer
            extends TomcatServletWebServerFactoryCustomizer {

        private final Connector[] additionalConnectors;

        TomcatMultiConnectorServletWebServerFactoryCustomizer(ServerProperties serverProperties, Connector[] additionalConnectors) {
            super(serverProperties);
            this.additionalConnectors = additionalConnectors;
        }

        @Override
        public void customize(TomcatServletWebServerFactory factory) {
            super.customize(factory);

            if (additionalConnectors != null && additionalConnectors.length > 0) {
                factory.addAdditionalTomcatConnectors(additionalConnectors);
            }
        }
    }

}