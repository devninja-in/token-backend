package gs.app.token.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gs.app.token.exception.TokenAppException;

import java.util.Map;

@RestControllerAdvice
public class TokenAppExceptionHandler {

    @ExceptionHandler(value = TokenAppException.class)
    public ResponseEntity<Map<String, Object>> exception(TokenAppException exception) {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<Map<String, Object>>(
            exception.toMap(), HttpStatus.valueOf(exception.getResponseCode()));
        return responseEntity;
    }
}
