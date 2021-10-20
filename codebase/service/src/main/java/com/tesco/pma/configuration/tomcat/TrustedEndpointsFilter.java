package com.tesco.pma.configuration.tomcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TrustedEndpointsFilter implements Filter {

    private int trustedPortNum = 0;
    private String trustedPathPrefix;

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    /**
     *
     * @param trustedPort
     * @param trustedPathPrefix
     */
    TrustedEndpointsFilter(String trustedPort, String trustedPathPrefix) {
        if (trustedPort != null && trustedPathPrefix != null && !"null".equals(trustedPathPrefix)) {
            trustedPortNum = Integer.parseInt(trustedPort);
            this.trustedPathPrefix = trustedPathPrefix;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.debug("Trusted port filter init: " + trustedPortNum + ":" + trustedPathPrefix);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (trustedPortNum != 0) {
            if (isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() != trustedPortNum) {
                prepareNotFoundResponse(servletResponse, "Denying request for trusted endpoint on untrusted port");
                return;
            }
            if (!isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() == trustedPortNum) {
                prepareNotFoundResponse(servletResponse, "Denying request for untrusted endpoint on trusted port");
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRequestForTrustedEndpoint(ServletRequest servletRequest) {
        String requestURI = ((HttpServletRequest) servletRequest).getRequestURI();
        return requestURI.startsWith(trustedPathPrefix);
    }

    private void prepareNotFoundResponse(ServletResponse servletResponse, String message) throws IOException {
        log.warn(message);
        ((HttpServletResponse) servletResponse).setStatus(HttpStatus.NOT_FOUND.value());
        servletResponse.getOutputStream().close();
    }

    @Override
    public void destroy() {
    }

}
