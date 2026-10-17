package com.vehicle.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vehicle.vehicle.model.VehicleModel;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleModel, String> {

    boolean existsByPlate(String plate);

}