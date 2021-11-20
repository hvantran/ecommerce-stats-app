package com.hoatv.controllers;

import com.hoatv.fwk.common.exceptions.AppException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
@ControllerAdvice
public class ExtExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value= {AppException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        String responseMessage = String.format("{\"message\": \"%s\"}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(ex, responseMessage, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}