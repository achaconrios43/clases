package com.example.dcim.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.dcim.entity.PlanoSala;

public interface PlanoSalaRepository extends JpaRepository<PlanoSala, Long> {

    List<PlanoSala> findBySalaId(Long salaId);

    List<PlanoSala> findAllByOrderByFechaCreacionDesc();

    List<PlanoSala> findByEsPlantillaTrueOrderByNombrePlantillaAsc();

    List<PlanoSala> findByEsPlantillaFalseOrderByFechaCreacionDesc();

    java.util.Optional<PlanoSala> findFirstBySalaIdAndLayoutJsonIsNotNull(Long salaId);

    java.util.Optional<PlanoSala> findFirstBySalaId(Long salaId);
}
