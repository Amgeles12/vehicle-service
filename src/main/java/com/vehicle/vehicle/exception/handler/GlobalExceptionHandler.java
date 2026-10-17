package com.vehicle.vehicle.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.vehicle.vehicle.dto.ErrorDto;
import com.vehicle.vehicle.exception.BusinessException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> handleBusinessException(BusinessException businessException) {
        ErrorDto errorDto = new ErrorDto(businessException.getMessage(), businessException.getHttpStatus());
        return new ResponseEntity<>(errorDto, errorDto.httpStatus());
    }

}