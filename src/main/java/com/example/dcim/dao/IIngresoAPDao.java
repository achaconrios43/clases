package com.example.dcim.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dcim.entity.IngresoAP;

/**
 * Repositorio de datos para IngresoAP usando Spring Data JPA
 */
@Repository
public interface IIngresoAPDao extends JpaRepository<IngresoAP, Long> {
    Optional<IngresoAP> findByNumeroTicket(String numeroTicket);
    List<IngresoAP> findByRutTecnico(String rutTecnico);
    List<IngresoAP> findByNombreTecnicoAndRutTecnico(String nombreTecnico, String rutTecnico);
    List<IngresoAP> findByFechaInicio(LocalDate fechaInicio);
    List<IngresoAP> findByFechaInicioAndFechaTermino(LocalDate fechaInicio, LocalDate fechaTermino);
    List<IngresoAP> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<IngresoAP> findByEmpresaDemandante(String empresaDemandante);
    List<IngresoAP> findByEmpresaContratista(String empresaContratista);
    List<IngresoAP> findByTipoTicket(String tipoTicket);
    List<IngresoAP> findBySalaRemedy(String salaRemedy);
    Long countByFechaInicio(LocalDate fechaInicio);
    
    @Query("SELECT i FROM IngresoAP i WHERE i.fechaInicio = :fechaInicio ORDER BY i.horaInicio")
    List<IngresoAP> findByFechaInicioOrderByHoraInicio(@Param("fechaInicio") LocalDate fechaInicio);
    
    @Query("SELECT i FROM IngresoAP i WHERE i.horaTermino IS NULL OR i.fechaTermino IS NULL")
    List<IngresoAP> findRegistrosSinSalida();
    
    @Query("SELECT i FROM IngresoAP i ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findUltimosRegistros();
    
    @Query("SELECT i FROM IngresoAP i WHERE i.fechaInicio BETWEEN :fechaInicio AND :fechaFin ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findByFechaInicioBetweenOrderByFechaDesc(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT i FROM IngresoAP i WHERE i.activo = true ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findActivosOrdenadosPorFecha();
    
    @Query("SELECT i FROM IngresoAP i ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findAllOrdenadosPorFecha();
    
    // Métodos para estadísticas del dashboard de cliente
    @Query("SELECT i FROM IngresoAP i WHERE i.fechaInicio BETWEEN :fechaInicio AND :fechaFin AND i.activo = true")
    List<IngresoAP> findByFechaInicioBetweenAndActivoTrue(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countByFechaInicioBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(DISTINCT i.numeroTicket) FROM IngresoAP i WHERE i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countDistinctNumeroTicketByFechaInicioBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.tipoTicket = :tipoTicket AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countByTipoTicketAndFechaInicioBetween(@Param("tipoTicket") String tipoTicket, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.salaRemedy = :salaRemedy AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countBySalaRemedyAndFechaInicioBetween(@Param("salaRemedy") String salaRemedy, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT i FROM IngresoAP i WHERE i.activo = true ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findTop10ByActivoTrueOrderByFechaInicioDescHoraInicioDesc();
    
    // Métodos específicos del Dashboard Cliente por sitio
    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.sitioIngreso = :sitio AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countBySitioIngresoAndFechaInicioBetween(@Param("sitio") String sitio, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(DISTINCT i.numeroTicket) FROM IngresoAP i WHERE i.sitioIngreso = :sitio AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countDistinctNumeroTicketBySitioIngresoAndFechaInicioBetween(@Param("sitio") String sitio, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(DISTINCT i.numeroTicket) FROM IngresoAP i WHERE i.tipoTicket = :tipoTicket AND i.sitioIngreso = :sitio AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countDistinctNumeroTicketByTipoTicketAndSitioIngresoAndFechaInicioBetween(@Param("tipoTicket") String tipoTicket, @Param("sitio") String sitio, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.salaRemedy = :salaRemedy AND i.sitioIngreso = :sitio AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countBySalaRemedyAndSitioIngresoAndFechaInicioBetween(@Param("salaRemedy") String salaRemedy, @Param("sitio") String sitio, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT i FROM IngresoAP i WHERE i.activo = true AND i.sitioIngreso = :sitio ORDER BY i.fechaInicio DESC, i.horaInicio DESC")
    List<IngresoAP> findByActivoTrueAndSitioIngresoOrderByFechaInicioDescHoraInicioDesc(@Param("sitio") String sitio);
    
    @Query("SELECT COUNT(DISTINCT i.numeroTicket) FROM IngresoAP i WHERE i.tipoTicket = :tipoTicket AND i.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    Long countDistinctNumeroTicketByTipoTicketAndFechaInicioBetween(@Param("tipoTicket") String tipoTicket, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COUNT(i) FROM IngresoAP i WHERE i.sitioRef.id = :sitioId AND i.fechaInicio = :fecha")
    Long countBySitioRefIdAndFechaInicio(@Param("sitioId") Long sitioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT DISTINCT i.sitioIngreso FROM IngresoAP i WHERE i.sitioIngreso IS NOT NULL ORDER BY i.sitioIngreso")
    List<String> findDistinctSitioIngreso();
}
