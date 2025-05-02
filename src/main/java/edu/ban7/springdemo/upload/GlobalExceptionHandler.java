package edu.ban7.springdemo.upload;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Attrape les exceptions due à l'annotation @Valid (et @Validated) native à Spring Validation
     * HandlerMethodValidationException
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Attrape les exceptions due à l'annotation @ValidFile qui a été ajoutée
     * HandlerMethodValidationException
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidationException(HandlerMethodValidationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        for (ParameterValidationResult p : ex.getParameterValidationResults()) {
            List<MessageSourceResolvable> resolvableErrors = p.getResolvableErrors();
            for (MessageSourceResolvable e : resolvableErrors) {
                String fieldName = ((DefaultMessageSourceResolvable) Objects.requireNonNull(e.getArguments())[0]).getDefaultMessage();
                String errorMessage = e.getDefaultMessage();
                errors.put(fieldName != null ? fieldName : "global", errorMessage);
            }
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalExceptions(Exception ex, WebRequest request) {
        // Log the exception type and message
        System.err.println("Exception occurred: " + ex.getClass().getName() + " - " + ex.getMessage());
        return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
