package com.marketplace.catalog.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Function;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "item";
        }
        String normalized = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "item" : slug;
    }

    public static String uniqueSlug(String base, Function<String, Boolean> existsChecker) {
        String slug = slugify(base);
        if (!existsChecker.apply(slug)) {
            return slug;
        }
        for (int i = 2; i < 1000; i++) {
            String candidate = slug + "-" + i;
            if (!existsChecker.apply(candidate)) {
                return candidate;
            }
        }
        return slug + "-" + System.currentTimeMillis();
    }
}
