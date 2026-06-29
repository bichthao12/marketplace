package com.marketplace.catalog.dto;

import java.util.UUID;

public record CategoryRefResponse(
        UUID id,
        String name,
        String slug
) {
}
