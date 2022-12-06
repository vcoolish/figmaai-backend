package com.app.drivn.backend.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;

public class ExtendedCorsProcessor extends DefaultCorsProcessor {

  private static final Log logger = LogFactory.getLog(ExtendedCorsProcessor.class);

  @Override
  protected boolean handleInternal(
      @NonNull final ServerHttpRequest request,
      @NonNull final ServerHttpResponse response,
      @NonNull final CorsConfiguration config,
      final boolean preFlightRequest
  ) throws IOException {

    String requestOrigin = request.getHeaders().getOrigin();
    String allowOrigin = checkOrigin(config, requestOrigin);
    HttpHeaders responseHeaders = response.getHeaders();

    if (allowOrigin == null) {
      logger.debug("Reject: '" + requestOrigin + "' origin is not allowed");
      rejectRequest(response);
      return false;
    }

    HttpMethod requestMethod = this.getMethodToUse(request, preFlightRequest);
    List<HttpMethod> allowMethods = checkMethods(config, requestMethod);
    if (allowMethods == null) {
      logger.debug("Reject: HTTP '" + requestMethod + "' is not allowed");
      rejectRequest(response);
      return false;
    }

    List<String> requestHeaders = this.getHeadersToUse(request, preFlightRequest);
    List<String> allowHeaders = checkHeaders(config, requestHeaders);
    if (preFlightRequest && allowHeaders == null) {
      logger.debug("Reject: headers '" + requestHeaders + "' are not allowed");
      rejectRequest(response);
      return false;
    }

    responseHeaders.setAccessControlAllowOrigin(allowOrigin);

    if (preFlightRequest) {
      responseHeaders.setAccessControlAllowMethods(allowMethods);
    }

    if (preFlightRequest && !allowHeaders.isEmpty()) {
      responseHeaders.setAccessControlAllowHeaders(allowHeaders);
    }

    if (!CollectionUtils.isEmpty(config.getExposedHeaders())) {
      responseHeaders.setAccessControlExposeHeaders(config.getExposedHeaders());
    }

    if (Boolean.TRUE.equals(config.getAllowCredentials())) {
      responseHeaders.setAccessControlAllowCredentials(true);
    }

    if (preFlightRequest && config.getMaxAge() != null) {
      responseHeaders.setAccessControlMaxAge(config.getMaxAge());
    }

    response.flush();
    return true;
  }

  @Nullable
  protected HttpMethod getMethodToUse(ServerHttpRequest request, boolean isPreFlight) {
    if (isPreFlight) {
      final var headers = request.getHeaders();
      final var method = Optional.ofNullable(headers.getFirst(ACCESS_CONTROL_REQUEST_METHOD))
          .orElse(headers.getFirst(ACCESS_CONTROL_REQUEST_METHOD.toLowerCase()));
      return HttpMethod.resolve(method);
    }

    return request.getMethod();
  }

  protected List<String> getHeadersToUse(ServerHttpRequest request, boolean isPreFlight) {
    final HttpHeaders headers = request.getHeaders();
    if (isPreFlight) {
      final var acrHeaders = headers.getValuesAsList(ACCESS_CONTROL_REQUEST_HEADERS);
      if (!acrHeaders.isEmpty()) {
        return acrHeaders;
      }

      return headers.getValuesAsList(ACCESS_CONTROL_REQUEST_HEADERS.toLowerCase());
    }

    return new ArrayList<>(headers.keySet());
  }
}
