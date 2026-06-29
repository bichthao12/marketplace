package com.marketplace.common.dto;

import java.util.List;

public record ErrorBody(
        String code,
        String message,
        List<ErrorDetail> details,
        String traceId
) {
}
