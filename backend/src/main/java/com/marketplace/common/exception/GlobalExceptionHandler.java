package com.marketplace.common.exception;

import com.marketplace.common.dto.ErrorBody;
import com.marketplace.common.dto.ErrorDetail;
import com.marketplace.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return buildResponse(ex.getCode(), ex.getMessage(), List.of(), ex.getStatus(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return buildResponse("VALIDATION_ERROR", "Validation failed", details, HttpStatus.BAD_REQUEST, request);
    }

    private ErrorDetail toDetail(FieldError fieldError) {
        return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            String code,
            String message,
            List<ErrorDetail> details,
            HttpStatus status,
            HttpServletRequest request
    ) {
        String traceId = (String) request.getAttribute("traceId");
        if (traceId == null) {
            traceId = request.getHeader("X-Request-Id");
        }
        return ResponseEntity.status(status).body(new ErrorResponse(new ErrorBody(code, message, details, traceId)));
    }
}
