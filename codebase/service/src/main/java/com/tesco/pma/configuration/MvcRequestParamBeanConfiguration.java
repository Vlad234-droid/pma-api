package com.tesco.pma.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.qs.core.QS;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to convert Request parameters to Java Bean using Jackson object mapper.
 */
@Configuration
public class MvcRequestParamBeanConfiguration {

    @Bean
    public WebMvcConfigurer requestParamBeanConfigurer(@Autowired(required = false) ObjectMapper objectMapper,
                                                       @Value("${tesco.application.page.size:20}") int defaultPageLimit) {
        return new WebMvcConfigurer() {

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new RequestParamBeanHandlerMethodArgumentResolver(objectMapper));
                resolvers.add(new RequestQueryBeanHandlerMethodArgumentResolver(objectMapper, defaultPageLimit));
            }
        };
    }

    public static class RequestParamBeanHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

        private final ObjectMapper objectMapper;

        public RequestParamBeanHandlerMethodArgumentResolver(ObjectMapper objectMapper) {
            this.objectMapper = ObjectUtils.defaultIfNull(objectMapper.copy(), new ObjectMapper())
                    .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true)
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(RequestParamBean.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            if (!webRequest.getParameterMap().isEmpty()) {
                Map<String, String[]> params = new HashMap<>();
                for (Map.Entry<String, String[]> param : webRequest.getParameterMap().entrySet()) {
                    final var values = Arrays.stream(param.getValue())
                            .flatMap(s -> s.contains(",") ? Arrays.stream(s.split(",")) : Stream.of(s))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList()).toArray(String[]::new);
                    params.put(param.getKey(), values);
                }

                return objectMapper.convertValue(params, parameter.getParameterType());
            }
            return null;
        }
    }

    /**
     * Converts Strapi query string to java bean, using QS library and jackson mapper
     * Incoming request looks like:
     * _sort=created_at%3Adesc&_publicationState=preview&published_at_null=false&_start=5&_limit=20&_search=string
     * and will be converted to JSON:
     * {
     * "_sort": "created_at:desc",
     * "_publicationState": "preview",
     * "published_at_null": "false",
     * "_start": "5",
     * "_limit": "20",
     * "_search": "string"
     * }
     * and then to java bean using Jackson object mapper
     */
    public static class RequestQueryBeanHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

        private final ObjectMapper objectMapper;
        private final int defaultLimit;

        public RequestQueryBeanHandlerMethodArgumentResolver(ObjectMapper objectMapper, int defaultLimit) {
            this.objectMapper = ObjectUtils.defaultIfNull(objectMapper.copy(), new ObjectMapper());
            this.defaultLimit = defaultLimit;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(RequestQuery.class);
        }

        @Override
        public RequestQuery resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            var pageRequest = new RequestQuery();
            var nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);

            if (nativeRequest != null) {
                var queryString = nativeRequest.getQueryString();

                if (StringUtils.isNoneEmpty(queryString)) {
                    var jsonString = QS.parse(queryString).toJsonString();
                    pageRequest = objectMapper.readValue(jsonString, RequestQuery.class);
                }
            }

            if (pageRequest.getLimit() == null) {
                pageRequest.setLimit(defaultLimit);
            }

            removeRequestParamsFromFilter(parameter, pageRequest);

            return pageRequest;
        }

        private void removeRequestParamsFromFilter(MethodParameter parameter, RequestQuery pageRequest) {
            var method = parameter.getMethod();
            if (method != null) {
                var methodParameters = method.getParameters();
                var propertyNames = Arrays.stream(methodParameters)
                        .filter(p -> p.isAnnotationPresent(RequestParam.class))
                        .map(p -> StringUtils.defaultIfEmpty(p.getAnnotation(RequestParam.class).name(), p.getName()))
                        .collect(Collectors.toSet());
                pageRequest.getFilters().removeIf(e -> propertyNames.contains(e.getProperty()));
            }
        }
    }
}
