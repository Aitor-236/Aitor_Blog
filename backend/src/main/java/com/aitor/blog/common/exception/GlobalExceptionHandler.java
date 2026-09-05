package com.aitor.blog.common.exception;

import com.aitor.blog.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j 
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle MethodArgumentNotValidException thrown when validation on an argument annotated with @Valid fails.
     * @param ex the MethodArgumentNotValidException thrown during validation
     * @return a Result object containing the error code and validation error message
     */
    @ExceptionHandler (MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error("Validation error: {}", errorMessage);
        return Result.error(400, errorMessage);
    }

    @ExceptionHandler (Exception.class)
    public Result<?> handleException(Exception ex) {
        log.error("Internal server error: ", ex);
        return Result.error(500, "System busy, please try again later.");
    }
}
