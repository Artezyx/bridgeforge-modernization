package dev.bridgeforge.web;

import dev.bridgeforge.service.CustomerConflict;
import dev.bridgeforge.service.CustomerNotFound;
import dev.bridgeforge.service.UpstreamUnavailable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(CustomerNotFound.class)
    public ProblemDetail notFound(CustomerNotFound e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler({CustomerConflict.class, DataIntegrityViolationException.class})
    public ProblemDetail conflict(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Customer identifier or email conflict.");
    }
    @ExceptionHandler(UpstreamUnavailable.class)
    public ProblemDetail upstream(UpstreamUnavailable e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid customer data.");
    }
}
