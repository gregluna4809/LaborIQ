package com.gregluna.laboriq.occupation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice(assignableTypes = OccupationController.class)
public class OccupationExceptionHandler {

    @ExceptionHandler(OccupationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleOccupationNotFound(
            OccupationNotFoundException ex,
            HttpServletRequest request
    ) {
        return notFound(ex.getMessage(), request);
    }

    @ExceptionHandler(OccupationDataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleOccupationDataNotFound(
            OccupationDataNotFoundException ex,
            HttpServletRequest request
    ) {
        return notFound(ex.getMessage(), request);
    }

    private ApiErrorResponse notFound(String message, HttpServletRequest request) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }
}
