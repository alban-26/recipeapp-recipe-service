package com.myapp.recipe.domain.model;

import lombok.NonNull;

import java.util.Locale;

public record Tag(@NonNull String value) {
    public Tag {
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Tag must not be empty");
        }
    }
}