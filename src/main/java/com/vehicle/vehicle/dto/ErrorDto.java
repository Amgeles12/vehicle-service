package com.vehicle.vehicle.dto;

import org.springframework.http.HttpStatus;

public record ErrorDto(String message, HttpStatus httpStatus) {

}