package com.vehicle.vehicle.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vehicle.vehicle.constants.Constants;
import com.vehicle.vehicle.dto.VehicleDto;
import com.vehicle.vehicle.dto.VehicleRsDto;
import com.vehicle.vehicle.exception.BusinessException;
import com.vehicle.vehicle.model.VehicleModel;
import com.vehicle.vehicle.repository.VehicleRepository;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehicleDto> findAll() {
        return vehicleRepository.findAll().stream().map(VehicleModel::toDto).toList();
    }

    public VehicleDto findById(String id) throws BusinessException {
        return vehicleRepository.findById(id)
                .map(VehicleModel::toDto)
                .orElseThrow(() -> new BusinessException(Constants.VEHICLE_NOT_FOUND + id, HttpStatus.NOT_FOUND));
    }

    public VehicleRsDto create(VehicleDto vehicleDto) throws BusinessException {
        if (vehicleDto.getId() == null || vehicleDto.getId().isBlank()) {
            vehicleDto.setId(Constants.ID_PREFIX + UUID.randomUUID().toString().substring(0, 8));
        } else if (vehicleRepository.existsById(vehicleDto.getId())) {
            throw new BusinessException(Constants.DUPLICATED_ID + vehicleDto.getId(), HttpStatus.CONFLICT);
        }
        if (vehicleRepository.existsByPlate(vehicleDto.getPlate())) {
            throw new BusinessException(Constants.DUPLICATED_PLATE + vehicleDto.getPlate(), HttpStatus.CONFLICT);
        }
        String id = vehicleRepository.save(vehicleDto.toModel()).getId();
        return new VehicleRsDto(id);
    }

}