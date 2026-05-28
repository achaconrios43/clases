package com.example.dcim.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.dcim.dao.InventarioRepository;
import com.example.dcim.entity.Inventario;

@Service
@Transactional
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private TemperaturaService temperaturaService;

    // CRUD básico
    public Inventario crear(Inventario inventario) throws Exception {
        // Validar duplicados por número de serie
        if (inventario.getNumeroSerie() != null && 
            inventarioRepository.countByNumeroSerie(inventario.getNumeroSerie()) > 0) {
            throw new Exception("Ya existe un inventario con el número de serie: " + inventario.getNumeroSerie());
        }

        // Validar duplicados por tag
        if (inventario.getTag() != null && 
            inventarioRepository.countByTag(inventario.getTag()) > 0) {
            throw new Exception("Ya existe un inventario con el tag: " + inventario.getTag());
        }

        temperaturaService.vincularInventarioConUbicacion(inventario);

        return inventarioRepository.save(inventario);
    }

    public Inventario actualizar(Inventario inventario) throws Exception {
        if (!inventarioRepository.existsById(inventario.getId())) {
            throw new Exception("Inventario no encontrado");
        }
        temperaturaService.vincularInventarioConUbicacion(inventario);
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) throws Exception {
        if (!inventarioRepository.existsById(id)) {
            throw new Exception("Inventario no encontrado");
        }
        inventarioRepository.deleteById(id);
    }

    public Optional<Inventario> obtenerPorId(Long id) {
        return inventarioRepository.findById(id);
    }

    public List<Inventario> obtenerTodos() {
        return inventarioRepository.findAllByOrderByIdAsc();
    }

    // Búsquedas específicas
    public Optional<Inventario> obtenerPorNumeroSerie(String numeroSerie) {
        return inventarioRepository.findByNumeroSerie(numeroSerie);
    }

    public Optional<Inventario> obtenerPorTag(String tag) {
        return inventarioRepository.findByTag(tag);
    }

    public List<Inventario> obtenerPorCliente(String cliente) {
        return inventarioRepository.findByCliente(cliente);
    }

    public List<Inventario> obtenerPorEstado(String estado) {
        return inventarioRepository.findByEstado(estado);
    }

    public List<Inventario> obtenerPorSala(String sala) {
        return inventarioRepository.findBySala(sala);
    }

    // Búsqueda general
    public List<Inventario> buscar(String search) {
        return inventarioRepository.buscar(search);
    }

    public List<Inventario> obtenerPorSitioSalaYEstado(String sitio, String sala, String estado) {
        return inventarioRepository.findBySitioAndSalaAndEstado(sitio, sala, estado);
    }

    // Importar múltiples sin duplicados — cada ítem en su propia transacción
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void importarLote(List<Inventario> items) throws Exception {
        for (Inventario item : items) {
            try {
                Optional<Inventario> existente = Optional.empty();

                // Solo buscar duplicado si numeroSerie tiene valor real y no es "0" (placeholder sin datos)
                String ns = item.getNumeroSerie();
                if (ns != null && !ns.trim().isEmpty() && !ns.trim().equals("0")) {
                    existente = inventarioRepository.findByNumeroSerie(ns.trim());
                }

                // Si no encontró por serie, intentar por tag (también ignorar "0")
                if (!existente.isPresent()) {
                    String tg = item.getTag();
                    if (tg != null && !tg.trim().isEmpty() && !tg.trim().equals("0")) {
                        existente = inventarioRepository.findByTag(tg.trim());
                    }
                }

                if (existente.isPresent()) {
                    // Actualizar todos los campos del registro existente
                    Inventario inv = existente.get();
                    inv.setSala(item.getSala());
                    inv.setTipo(item.getTipo());
                    inv.setMarca(item.getMarca());
                    inv.setModelo(item.getModelo());
                    inv.setCliente(item.getCliente());
                    inv.setCoordenadas(item.getCoordenadas());
                    inv.setNombreRack(item.getNombreRack());
                    inv.setUbicacionUr(item.getUbicacionUr());
                    inv.setUrUtilizada(item.getUrUtilizada());
                    inv.setNumeroTemporal(item.getNumeroTemporal());
                    inv.setHotname(item.getHotname());
                    inv.setEstado(item.getEstado());
                    inv.setFechaAlarma(item.getFechaAlarma());
                    inv.setAlarmaHardware(item.getAlarmaHardware());
                    inv.setAlarmaVentilador(item.getAlarmaVentilador());
                    inv.setAlarmaFuentePoder(item.getAlarmaFuentePoder());
                    inv.setAlarmaHdd(item.getAlarmaHdd());
                    inv.setComentariosAlarma(item.getComentariosAlarma());
                    inv.setTicketRelacion(item.getTicketRelacion());
                    inv.setObservaciones(item.getObservaciones());
                    inv.setFlujoAire(item.getFlujoAire());
                    inv.setPesoEquipoKg(item.getPesoEquipoKg());
                    inv.setFuentesPoder(item.getFuentesPoder());
                    inv.setTiposEnchufe(item.getTiposEnchufe());
                    inv.setObservacionTipoEnchufe(item.getObservacionTipoEnchufe());
                    inv.setPotenciaConsumoWatts(item.getPotenciaConsumoWatts());
                    inv.setDireccionIp(item.getDireccionIp());
                    inv.setSitio(item.getSitio());
                    inventarioRepository.save(inv);
                } else {
                    inventarioRepository.save(item);
                }
            } catch (Exception e) {
                System.err.println("Error importando fila (serie=" + item.getNumeroSerie() + ", tag=" + item.getTag() + "): " + e.getMessage());
            }
        }
    }

    // Validar si existe por número de serie
    public boolean existePorNumeroSerie(String numeroSerie) {
        return inventarioRepository.countByNumeroSerie(numeroSerie) > 0;
    }

    // Validar si existe por tag
    public boolean existePorTag(String tag) {
        return inventarioRepository.countByTag(tag) > 0;
    }

    // Contar total
    public long contar() {
        return inventarioRepository.count();
    }
}
