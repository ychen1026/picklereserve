package edu.sjsu.cmpe172.picklereserve.error;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "edu.sjsu.cmpe172.picklereserve.controller")
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> badRequest(Exception e, HttpServletRequest r){ return response(HttpStatus.BAD_REQUEST, safe(e,"Invalid request"),r); }
    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ApiError> unauthorized(Exception e,HttpServletRequest r){ return response(HttpStatus.UNAUTHORIZED,e.getMessage(),r); }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> forbidden(Exception e,HttpServletRequest r){ return response(HttpStatus.FORBIDDEN,e.getMessage(),r); }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(Exception e,HttpServletRequest r){ return response(HttpStatus.NOT_FOUND,e.getMessage(),r); }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(Exception e,HttpServletRequest r){ return response(HttpStatus.CONFLICT,e.getMessage(),r); }
    private String safe(Exception e,String fallback){ return e.getMessage()==null?fallback:e.getMessage(); }
    private ResponseEntity<ApiError> response(HttpStatus status,String message,HttpServletRequest r){
        return ResponseEntity.status(status).body(new ApiError(Instant.now(),status.value(),status.getReasonPhrase(),message,r.getRequestURI()));
    }
}
