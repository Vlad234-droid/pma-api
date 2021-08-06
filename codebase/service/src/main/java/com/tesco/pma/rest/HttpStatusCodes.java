package com.tesco.pma.rest;

import lombok.experimental.UtilityClass;

/**
 * HttpStatusCodes class is added because we need String representation of status codes to use it in annotations
 */
@UtilityClass
@SuppressWarnings("PMD.ClassNamingConventions")
public final class HttpStatusCodes {

    // --- 2xx Success ---

    public static final String OK = "200";

    public static final String CREATED = "201";

    public static final String ACCEPTED = "202";

    public static final String NO_CONTENT = "204";


    // --- 4xx Client Error ---

    public static final String BAD_REQUEST = "400";

    public static final String UNAUTHORIZED = "401";

    public static final String PAYMENT_REQUIRED = "402";

    public static final String FORBIDDEN = "403";

    public static final String NOT_FOUND = "404";

    public static final String METHOD_NOT_ALLOWED = "405";

    public static final String NOT_ACCEPTABLE = "406";

    public static final String PROXY_AUTHENTICATION_REQUIRED = "407";

    public static final String REQUEST_TIMEOUT = "408";

    public static final String CONFLICT = "409";

    public static final String GONE = "410";

    public static final String LENGTH_REQUIRED = "411";

    public static final String PRECONDITION_FAILED = "412";

    public static final String PAYLOAD_TOO_LARGE = "413";

    public static final String URI_TOO_LONG = "414";

    public static final String UNSUPPORTED_MEDIA_TYPE = "415";

    public static final String REQUESTED_RANGE_NOT_SATISFIABLE = "416";

    public static final String EXPECTATION_FAILED = "417";

    public static final String I_AM_A_TEAPOT = "418";

    public static final String UNPROCESSABLE_ENTITY = "422";

    public static final String LOCKED = "423";

    public static final String FAILED_DEPENDENCY = "424";

    public static final String TOO_EARLY = "425";

    public static final String UPGRADE_REQUIRED = "426";

    public static final String PRECONDITION_REQUIRED = "428";

    public static final String TOO_MANY_REQUESTS = "429";

    public static final String REQUEST_HEADER_FIELDS_TOO_LARGE = "431";

    public static final String UNAVAILABLE_FOR_LEGAL_REASONS = "451";

    
    //  --- 5xx Server Error ---

    public static final String INTERNAL_SERVER_ERROR = "500";

    public static final String NOT_IMPLEMENTED = "501";

    public static final String BAD_GATEWAY = "502";

    public static final String SERVICE_UNAVAILABLE = "503";

    public static final String GATEWAY_TIMEOUT = "504";

    public static final String HTTP_VERSION_NOT_SUPPORTED = "505";

    public static final String VARIANT_ALSO_NEGOTIATES = "506";

    public static final String INSUFFICIENT_STORAGE = "507";

    public static final String LOOP_DETECTED = "508";

    public static final String BANDWIDTH_LIMIT_EXCEEDED = "509";

    public static final String NOT_EXTENDED = "510";

    public static final String NETWORK_AUTHENTICATION_REQUIRED = "511";

}
