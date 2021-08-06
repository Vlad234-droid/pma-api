package com.tesco.pma.logging.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Simple request filter that logs the request URI/response code (and optionally the query string with headers and body).
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * The default value prepended to the log message written <i>after</i> a request is
     * processed.
     */
    private static final String DEFAULT_MESSAGE_PREFIX = "Request [";

    /**
     * The default value appended to the log message written <i>after</i> a request is
     * processed.
     */
    private static final String DEFAULT_MESSAGE_SUFFIX = "]";

    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 100;

    /**
     * Configure a request storage for getting body in case if
     * redirect to /error page was happened
     */
    private final ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();

    private boolean includeQueryString;

    private boolean includeClientInfo;

    private boolean includeHeaders;

    private boolean includePayload;

    private Set<MediaType> allowedRequestMediaType = Collections.singleton(MediaType.ALL);

    private Set<MediaType> allowedResponseMediaType = Collections.singleton(MediaType.ALL);

    private Predicate<String> headerPredicate;

    private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

    private String messagePrefix = DEFAULT_MESSAGE_PREFIX;

    private String messageSuffix = DEFAULT_MESSAGE_SUFFIX;


    /**
     * Set whether the query string should be included in the log message.
     */
    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public boolean isIncludeQueryString() {
        return this.includeQueryString;
    }

    /**
     * Set whether the client address and session id should be included in the
     * log message.
     */
    public void setIncludeClientInfo(boolean includeClientInfo) {
        this.includeClientInfo = includeClientInfo;
    }

    public boolean isIncludeClientInfo() {
        return this.includeClientInfo;
    }

    /**
     * Set whether the request headers should be included in the log message.
     */
    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public boolean isIncludeHeaders() {
        return this.includeHeaders;
    }

    /**
     * Set whether the request payload (body) should be included in the log message.
     */
    public void setIncludePayload(boolean includePayload) {
        this.includePayload = includePayload;
    }

    public boolean isIncludePayload() {
        return this.includePayload;
    }

    /**
     * Configure a predicate for selecting which headers should be logged if
     * {@link #setIncludeHeaders(boolean)} is set to {@code true}.
     * By default this is not set in which case all headers are logged.
     *
     * @param headerPredicate the predicate to use
     */
    public void setHeaderPredicate(@Nullable Predicate<String> headerPredicate) {
        this.headerPredicate = headerPredicate;
    }

    /**
     * @return the configured {@link #setHeaderPredicate(Predicate) headerPredicate}.
     */
    @Nullable
    public Predicate<String> getHeaderPredicate() {
        return this.headerPredicate;
    }

    /**
     * Set the maximum length of the payload body to be included in the log message.
     */
    public void setMaxPayloadLength(int maxPayloadLength) {
        if (maxPayloadLength < DEFAULT_MAX_PAYLOAD_LENGTH) {
            this.maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;
            logger.warn("'maxPayloadLength' should be larger than or equal to default value: " + DEFAULT_MAX_PAYLOAD_LENGTH);
            return;
        }
        this.maxPayloadLength = maxPayloadLength;
    }

    /**
     * @return the maximum length of the payload body to be included in the log message.
     */
    public int getMaxPayloadLength() {
        return this.maxPayloadLength;
    }

    /**
     * Set the value that should be prepended to the log message written
     * <i>after</i> a request is processed.
     */
    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    /**
     * Set the value that should be appended to the log message written
     * <i>after</i> a request is processed.
     */
    public void setMessageSuffix(String messageSuffix) {
        this.messageSuffix = messageSuffix;
    }

    public Set<MediaType> getAllowedRequestMediaType() {
        return allowedRequestMediaType;
    }

    public void setAllowedRequestMediaType(Set<MediaType> allowedRequestMediaType) {
        this.allowedRequestMediaType = allowedRequestMediaType;
    }

    public Set<MediaType> getAllowedResponseMediaType() {
        return allowedResponseMediaType;
    }

    public void setAllowedResponseMediaType(Set<MediaType> allowedResponseMediaType) {
        this.allowedResponseMediaType = allowedResponseMediaType;
    }

    /**
     * Forwards the request to the next filter in the chain and delegates down to the subclasses
     * to perform the actual request logging after the request is processed.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isFirstRequest = !isAsyncDispatch(request);

        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        if (isIncludePayload() && isFirstRequest) {
            if (!(request instanceof ContentCachingRequestWrapper)) {
                requestToUse = new ContentCachingRequestWrapper(request, getMaxPayloadLength());
            }
            if (!(response instanceof ContentCachingResponseWrapper)) {
                responseToUse = new ContentCachingResponseWrapper(response);
            }
        }

        if (threadLocal.get() == null) {
            threadLocal.set(requestToUse);
        }

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            boolean shouldLog = shouldLog(requestToUse, responseToUse);

            if (shouldLog && !isAsyncStarted(requestToUse)) {
                String message = createMessage(threadLocal.get(), responseToUse, this.messagePrefix, this.messageSuffix);
                logger.debug(message);
                threadLocal.remove();
            }
            if (responseToUse instanceof ContentCachingResponseWrapper) {
                ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
            }
        }
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    /**
     * Create a log message for the given request, prefix and suffix.
     * If {@code includeQueryString} is {@code true}, then the inner part
     * of the log message will take the form {@code request_uri?query_string};
     * otherwise the message will simply be of the form {@code request_uri}.
     * The final message is composed of the inner part as described and
     * the supplied prefix and suffix.
     */
    private String createMessage(HttpServletRequest request, HttpServletResponse response, String prefix, String suffix) {
        StringBuilder msg = new StringBuilder()
                .append(prefix)
                .append(request.getMethod())
                .append(' ')
                .append(request.getRequestURI());

        createQueryMessage(request, msg);
        createClientInfoMessage(request, msg);
        createHeadersMessage(request, msg);
        createPayloadMessage(request, response, msg);

        msg.append(suffix);
        return msg.toString();
    }

    private void createPayloadMessage(HttpServletRequest request, HttpServletResponse response, StringBuilder msg) {
        if (isIncludeRequestPayload(request)) {
            String payload = getRequestPayload(request);
            if (payload != null) {
                msg.append(", request payload=").append(payload);
            }
        }

        msg.append(", response code=").append(response.getStatus());
        if (isIncludeResponsePayload(response)) {
            String payload = getResponsePayload(response);
            if (payload != null) {
                msg.append(", response payload=").append(payload);
            }
        }
    }

    private void createHeadersMessage(HttpServletRequest request, StringBuilder msg) {
        if (isIncludeHeaders()) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            var predicate = getHeaderPredicate();
            if (predicate != null) {
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String header = names.nextElement();
                    if (!predicate.test(header)) {
                        headers.set(header, "masked");
                    }
                }
            }
            msg.append(", headers=").append(headers);
        }
    }

    private void createClientInfoMessage(HttpServletRequest request, StringBuilder msg) {
        if (isIncludeClientInfo()) {
            String client = request.getRemoteAddr();
            if (StringUtils.hasLength(client)) {
                msg.append(", client=").append(client);
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                msg.append(", session=").append(session.getId());
            }
            String user = request.getRemoteUser();
            if (user != null) {
                msg.append(", user=").append(user);
            }
        }
    }

    private void createQueryMessage(HttpServletRequest request, StringBuilder msg) {
        if (isIncludeQueryString()) {
            var queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }
    }

    private boolean isIncludeResponsePayload(HttpServletResponse response) {
        return isIncludePayload() && isContentTypeMatchMediaType(getAllowedResponseMediaType(), response.getContentType());

    }

    private boolean isIncludeRequestPayload(HttpServletRequest request) {
        return isIncludePayload() && isContentTypeMatchMediaType(getAllowedRequestMediaType(), request.getContentType());
    }

    private boolean isContentTypeMatchMediaType(Set<MediaType> allowedMediaType, String contentType) {
        if (!StringUtils.hasLength(contentType)) {
            return false;
        }
        var mediaType = MediaType.parseMediaType(contentType);
        return allowedMediaType.stream().anyMatch(mt -> mt.includes(mediaType));
    }

    @Nullable
    private String getRequestPayload(HttpServletRequest request) {
        var wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            return getBody(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
        }
        return null;
    }

    @Nullable
    private String getResponsePayload(HttpServletResponse response) {
        var wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            return getBody(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
        }
        return null;
    }

    @Nullable
    private String getBody(byte[] buf, String characterEncoding) {
        if (buf.length > 0) {
            var length = Math.min(buf.length, getMaxPayloadLength());
            try {
                return new String(buf, 0, length, characterEncoding);
            } catch (UnsupportedEncodingException ex) {
                return "[unknown]";
            }
        }
        return null;
    }


    /**
     * Determine whether to call the logger.debug()
     * method for the current request, i.e. whether logging is currently active
     * (and the log message is worth building).
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return {@code true} if the after method should get called;
     * {@code false} otherwise
     */
    private boolean shouldLog(HttpServletRequest request, HttpServletResponse response) {
        if (!logger.isDebugEnabled()) {
            return false;
        }
        if (isIncludePayload() && request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE) != null) {
            return request.getDispatcherType() == DispatcherType.ERROR
                    || (response instanceof ContentCachingResponseWrapper //NOPMD
                    && ((ContentCachingResponseWrapper) response).getContentSize() > 0);
        }
        return true;
    }

}
