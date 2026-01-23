package com.example.isogateway.config;

import com.example.isogateway.core.domain.MerchantEntity;
import com.example.isogateway.service.MerchantAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String MERCHANT_ATTRIBUTE = "merchant";

    private final MerchantAuthService merchantAuthService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (!requiresAuthentication(path)) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = httpRequest.getHeader(API_KEY_HEADER);
        Optional<MerchantEntity> merchant = merchantAuthService.authenticate(apiKey);

        if (merchant.isEmpty()) {
            log.warn("Invalid API key from IP: {}", httpRequest.getRemoteAddr());
            sendError(httpResponse, HttpStatus.UNAUTHORIZED, "Invalid API key");
            return;
        }

        String signature = httpRequest.getHeader(SIGNATURE_HEADER);
        if (signature != null && httpRequest.getMethod().equals("POST")) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
            String body = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (!body.isEmpty() && !merchantAuthService.validateSignature(merchant.get(), body, signature)) {
                log.warn("Invalid signature from merchant: {}", merchant.get().getName());
                sendError(httpResponse, HttpStatus.FORBIDDEN, "Invalid signature");
                return;
            }
            httpRequest = wrappedRequest;
        }

        httpRequest.setAttribute(MERCHANT_ATTRIBUTE, merchant.get());
        chain.doFilter(httpRequest, response);
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/api/v1/payments");
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\":\"%s\",\"code\":%d}", message, status.value()));
    }
}
