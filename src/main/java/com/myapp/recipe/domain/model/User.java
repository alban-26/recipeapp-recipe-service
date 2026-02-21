package com.myapp.recipe.domain.model;

import com.my.common.api.UserId;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;


public record User(UserId id, String username, String email) {

    public static User fromToken(JsonWebToken jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT cannot be null");
        }

        return new User(
                new UserId(jwt.getSubject()),
                jwt.getClaim("preferred_username"),
                jwt.getClaim("email")
        );
    }

    public static User fromIdentity(SecurityIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("SecurityIdentity cannot be null");
        }

        return new User(
                new UserId(identity.getPrincipal().getName()),
                identity.getPrincipal().getName(),
                identity.getAttribute("email") // may be null if not in identity attributes
        );
    }

    /**
     * Automatically determine the current user, preferring JWT if available.
     */
    public static User fromContext(JsonWebToken jwt, SecurityIdentity identity) {
        if (jwt != null && jwt.getSubject() != null) {
            return fromToken(jwt);
        }
        if (identity != null && identity.getPrincipal() != null) {
            return fromIdentity(identity);
        }

        throw new IllegalStateException("No authenticated user found in context");
    }

    public String displayName() {
        return username != null ? username : email;
    }
}

