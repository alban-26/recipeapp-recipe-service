package com.myapp.recipe.adapter.restapi;

import com.myapp.recipe.adapter.ErrorResponse;
import com.myapp.recipe.application.RecipeAlreadyExistsException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof RecipeAlreadyExistsException e) {
            log.info("Duplicate recipe name: {}", e.getRecipeName());
            return build(Response.Status.CONFLICT, new ErrorResponse(
                    "RECIPE_NAME_DUPLICATE",
                    "A recipe with this name already exists",
                    Map.of("recipeName", e.getRecipeName())
            ));
        }

        if (exception instanceof NotFoundException) {
            return build(Response.Status.NOT_FOUND,
                    ErrorResponse.of("NOT_FOUND", "Resource not found"));
        }

        if (exception instanceof IllegalArgumentException) {
            log.warn("Invalid input", exception);
            return build(Response.Status.BAD_REQUEST,
                    ErrorResponse.of("INVALID_INPUT", "Invalid input data"));
        }

        if (exception instanceof WebApplicationException e) {
            Response.StatusType status = e.getResponse().getStatusInfo();
            log.warn("WebApplicationException: {} {}", status.getStatusCode(), status.getReasonPhrase());
            return Response.status(status.getStatusCode())
                    .entity(ErrorResponse.of(
                            status.getFamily() == Response.Status.Family.CLIENT_ERROR
                                    ? "CLIENT_ERROR" : "SERVER_ERROR",
                            status.getReasonPhrase()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled exception [traceId={}]", traceId, exception);
        return build(Response.Status.INTERNAL_SERVER_ERROR, new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                Map.of("traceId", traceId)
        ));
    }

    private Response build(Response.Status status, ErrorResponse body) {
        return Response.status(status)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}