package com.example.taskmanager.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Map<String,Object> forbidden(ForbiddenException ex, HttpServletRequest req) {
    return Map.of("status",403,"error","Forbidden","message",ex.getMessage(),"path",req.getRequestURI());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Map<String,Object> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
    return Map.of("status",403,"error","Forbidden","message","Access is denied","path",req.getRequestURI());
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String,Object> notFound(NotFoundException ex, HttpServletRequest req) {
    return Map.of("status",404,"error","Not Found","message",ex.getMessage(),"path",req.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String,Object> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField()+": "+fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return Map.of("status",400,"error","Bad Request","message",msg,"path",req.getRequestURI());
  }

  @ExceptionHandler({ ConstraintViolationException.class, HttpMessageNotReadableException.class,
          MethodArgumentTypeMismatchException.class, IllegalArgumentException.class,
          DataIntegrityViolationException.class, TransactionSystemException.class })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String,Object> badRequest(Exception ex, HttpServletRequest req) {
    log.debug("Bad request at {}: {}", req.getRequestURI(), ex.toString());
    String msg = ex.getMessage();
    if (msg == null || msg.isBlank()) msg = "Bad request";
    int nl = msg.indexOf('\n'); if (nl > 0) msg = msg.substring(0, nl);
    return Map.of("status",400,"error","Bad Request","message",msg,"path",req.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String,Object> generic(Exception ex, HttpServletRequest req) {
    log.error("Unhandled error at {}: ", req.getRequestURI(), ex);
    return Map.of("status",500,"error","Internal Server Error","message","Unexpected error","path",req.getRequestURI());
  }
}