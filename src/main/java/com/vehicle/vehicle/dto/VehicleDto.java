package com.vehicle.vehicle.dto;

import java.time.LocalDate;

import com.vehicle.vehicle.model.VehicleModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDto {

    private String id;
    private String plate;
    private String model;
    private Double batteryStatusPercentage;
    private String engineStatus;
    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDate lastMaintenanceDate;

    public VehicleModel toModel() {
        return VehicleModel.builder()
                .id(this.id)
                .plate(this.plate)
                .model(this.model)
                .batteryStatusPercentage(this.batteryStatusPercentage)
                .engineStatus(this.engineStatus)
                .lastLatitude(this.lastLatitude)
                .lastLongitude(this.lastLongitude)
                .lastMaintenanceDate(this.lastMaintenanceDate)
                .build();
    }

}