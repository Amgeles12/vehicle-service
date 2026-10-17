package com.vehicle.vehicle.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicle.vehicle.dto.VehicleDto;
import com.vehicle.vehicle.dto.VehicleRsDto;
import com.vehicle.vehicle.exception.BusinessException;
import com.vehicle.vehicle.service.VehicleService;
import com.vehicle.vehicle.util.ValidateUtil;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ValidateUtil validateUtil;

    public VehicleController(VehicleService vehicleService, ValidateUtil validateUtil) {
        this.vehicleService = vehicleService;
        this.validateUtil = validateUtil;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> findAll() {
        return new ResponseEntity<>(vehicleService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> findById(@PathVariable("id") String id) throws BusinessException {
        return new ResponseEntity<>(vehicleService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<VehicleRsDto> create(@RequestBody VehicleDto vehicleDto) throws BusinessException {
        validateUtil.validateRequest(vehicleDto);
        return new ResponseEntity<>(vehicleService.create(vehicleDto), HttpStatus.CREATED);
    }

}