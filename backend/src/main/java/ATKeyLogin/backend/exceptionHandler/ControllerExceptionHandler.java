package ATKeyLogin.backend.exceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.security.InvalidParameterException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import ATKeyLogin.backend.domain.Response.ResponseHandler;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;


import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    
    @ExceptionHandler(AuthFiServiceException.class)
    public ResponseEntity<Map> AuthFiServiceExceptionHandler(AuthFiServiceException ex, WebRequest request) {
        
        return ResponseHandler.getFailRes(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "failed");
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Map> businessLogicExceptionHandler(BusinessLogicException ex, WebRequest request) {
        
        return ResponseHandler.getFailRes(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "failed");
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map> parameterExceptionHandler(InvalidParameterException ex, WebRequest request) {
        
        return ResponseHandler.getFailRes(HttpStatus.BAD_REQUEST, ex.getMessage(), "failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> parameterExceptionHandler(MethodArgumentNotValidException ex, WebRequest request) {
        
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        String errs = "";
        
        for (FieldError fieldError: fieldErrors) {
            errs += fieldError.getDefaultMessage();
            errs += ";";
        }
        
        return ResponseHandler.getFailRes(HttpStatus.BAD_REQUEST, errs, "failed");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> globalExceptionHandler(Exception ex, WebRequest request) {
        return ResponseHandler.getFailRes(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "failed");

    }

}
