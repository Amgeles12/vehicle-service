package com.vehicle.vehicle.model;

import java.time.LocalDate;

import com.vehicle.vehicle.dto.VehicleDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicle")
public class VehicleModel {

    @Id
    @Column(name = "id", columnDefinition = "varchar(20)")
    private String id;
    @Column(name = "plate", columnDefinition = "varchar(20)", unique = true, nullable = false)
    private String plate;
    @Column(name = "model", columnDefinition = "varchar(80)")
    private String model;
    @Column(name = "battery_status_percentage")
    private Double batteryStatusPercentage;
    @Column(name = "engine_status", columnDefinition = "varchar(30)")
    private String engineStatus;
    @Column(name = "last_latitude")
    private Double lastLatitude;
    @Column(name = "last_longitude")
    private Double lastLongitude;
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    public VehicleDto toDto() {
        return VehicleDto.builder()
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