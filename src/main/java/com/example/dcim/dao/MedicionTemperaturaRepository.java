package com.example.dcim.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.dcim.entity.MedicionTemperatura;

public interface MedicionTemperaturaRepository extends JpaRepository<MedicionTemperatura, Long> {

    Optional<MedicionTemperatura> findTopByPuntoIdOrderByFechaRegistroDesc(Long puntoId);

    @Query("SELECT AVG(m.temperaturaCelsius) FROM MedicionTemperatura m WHERE UPPER(m.punto.sala.sitio.nombre) = UPPER(:sitio) AND m.fechaMedicion = :fecha")
    Double promedioDiarioPorSitio(@Param("sitio") String sitio, @Param("fecha") LocalDate fecha);

    @Query("SELECT AVG(m.temperaturaCelsius) FROM MedicionTemperatura m WHERE UPPER(m.punto.sala.sitio.nombre) = UPPER(:sitio) AND m.fechaMedicion BETWEEN :inicio AND :fin")
    Double promedioMensualPorSitio(@Param("sitio") String sitio, @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT AVG(m.temperaturaCelsius) FROM MedicionTemperatura m WHERE m.punto.sala.id = :salaId AND m.fechaMedicion = :fecha")
    Double promedioDiarioPorSala(@Param("salaId") Long salaId, @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(DISTINCT m.punto.id) FROM MedicionTemperatura m WHERE m.punto.sala.id = :salaId AND m.fechaMedicion = :fecha")
    Long puntosMedidosDiariosPorSala(@Param("salaId") Long salaId, @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(m) FROM MedicionTemperatura m WHERE m.punto.sala.id = :salaId AND m.fechaMedicion = :fecha AND m.estado <> 'OK'")
    Long alertasDiariasPorSala(@Param("salaId") Long salaId, @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(m) FROM MedicionTemperatura m WHERE UPPER(m.punto.sala.sitio.nombre) = UPPER(:sitio) AND m.fechaMedicion = :fecha AND m.estado <> 'OK'")
    Long alertasDiariasPorSitio(@Param("sitio") String sitio, @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(m) FROM MedicionTemperatura m WHERE UPPER(m.punto.sala.sitio.nombre) = UPPER(:sitio) AND m.fechaMedicion BETWEEN :inicio AND :fin AND m.estado <> 'OK'")
    Long alertasMensualesPorSitio(@Param("sitio") String sitio, @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    List<MedicionTemperatura> findByPuntoSalaSitioNombreIgnoreCaseAndFechaMedicionOrderByFechaRegistroDesc(String sitio,
            LocalDate fecha);

    List<MedicionTemperatura> findTop20ByPuntoSalaIdAndFechaMedicionOrderByFechaRegistroDesc(Long salaId,
            LocalDate fecha);

    @Query("SELECT DISTINCT m.punto.id FROM MedicionTemperatura m WHERE m.punto.sala.id = :salaId AND m.fechaMedicion = :fecha")
    List<Long> findPuntoIdsRegistradosEnFecha(@Param("salaId") Long salaId, @Param("fecha") LocalDate fecha);

    boolean existsByPuntoIdAndFechaMedicion(Long puntoId, LocalDate fecha);

    @Query("SELECT AVG(m.temperaturaCelsius) FROM MedicionTemperatura m WHERE m.punto.sala.sitio.id = :sitioId AND m.fechaMedicion = :fecha")
    Double promedioDiarioPorSitioId(@Param("sitioId") Long sitioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT DISTINCT m.punto.id FROM MedicionTemperatura m WHERE m.punto.sala.id = :salaId AND m.fechaMedicion = :fecha AND m.horario = :horario")
    List<Long> findPuntoIdsRegistradosEnFechaYHorario(@Param("salaId") Long salaId, @Param("fecha") LocalDate fecha, @Param("horario") String horario);

    @Query("SELECT m FROM MedicionTemperatura m WHERE m.punto.sala.sitio.id = :sitioId AND m.fechaMedicion BETWEEN :inicio AND :fin ORDER BY m.fechaMedicion, m.punto.id")
    List<MedicionTemperatura> findBySitioIdAndFechaRange(@Param("sitioId") Long sitioId, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
