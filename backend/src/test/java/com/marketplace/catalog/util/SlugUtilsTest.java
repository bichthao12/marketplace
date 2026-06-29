package com.marketplace.catalog.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlugUtilsTest {

    @Test
    void slugifyNormalizesText() {
        assertEquals("iphone-15-case", SlugUtils.slugify("iPhone 15 Case"));
        assertEquals("item", SlugUtils.slugify("   "));
    }
}
