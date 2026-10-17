package com.vehicle.vehicle.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.vehicle.vehicle.constants.Constants;
import com.vehicle.vehicle.dto.VehicleDto;
import com.vehicle.vehicle.exception.BusinessException;

@Component
public class ValidateUtil {

    public void validateRequest(VehicleDto vehicleDto) throws BusinessException {
        if (isBlank(vehicleDto.getPlate())) {
            throw new BusinessException(Constants.EMPTY_PLATE, HttpStatus.BAD_REQUEST);
        }
        if (isBlank(vehicleDto.getModel())) {
            throw new BusinessException(Constants.EMPTY_MODEL, HttpStatus.BAD_REQUEST);
        }
        Double battery = vehicleDto.getBatteryStatusPercentage();
        if (battery != null && (battery < 0 || battery > 100)) {
            throw new BusinessException(Constants.INVALID_BATTERY, HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}