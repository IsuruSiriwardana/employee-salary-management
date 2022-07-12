package com.zenika.users.controller;

import com.zenika.users.dto.ResponseMessage;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.exception.InvalidUserDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<SimpleResponseDto> handleRuntimeException(RuntimeException ex) {
    SimpleResponseDto responseDto =
        new SimpleResponseDto(
            ResponseMessage.ERROR_OCCURRED_BAD_INPUT, ex.getMessage() + " " + ex.getCause().getMessage());
    log.error("Error occurred", ex);
    return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(InvalidUserDataException.class)
  protected ResponseEntity<SimpleResponseDto> handleInvalidUserDataException(
      InvalidUserDataException ex) {
    SimpleResponseDto responseDto = new SimpleResponseDto(ResponseMessage.ERROR_OCCURRED_BAD_INPUT, ex.getMessage());
    log.error("Error occurred", ex);
    return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
  }
}
