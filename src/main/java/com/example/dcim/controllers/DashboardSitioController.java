package com.example.dcim.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dcim.dao.IUsuarioDao;
import com.example.dcim.dao.InventarioRepository;
import com.example.dcim.dao.SitioRepository;
import com.example.dcim.dto.RegistroActivoDto;
import com.example.dcim.entity.GestionAcceso;
import com.example.dcim.entity.IngresoAP;
import com.example.dcim.entity.Inventario;
import com.example.dcim.entity.Sala;
import com.example.dcim.entity.Sitio;
import com.example.dcim.entity.Usuario;
import com.example.dcim.service.GestionAccesoService;
import com.example.dcim.service.IngresoAPService;
import com.example.dcim.service.TemperaturaService;
import com.example.dcim.service.TemperaturaService.ResumenSalaDiario;
import com.example.dcim.service.TemperaturaService.TemperaturaResumen;

/**
 * Dashboard unificado por sitio — reemplaza los 3 dashboards anteriores.
 * Consolida Acceso (diario+mensual), Temperaturas, Inventario y KPI en una
 * sola vista con 4 pestañas.
 *
 * Ruta: GET /dashboard/sitio?sitio=NombreSitio
 * Acceso: ADMIN, USER, VIEWER, CLIENTE
 */
@Controller
@RequestMapping("/dashboard/sitio")
public class DashboardSitioController {

    @Autowired private IngresoAPService ingresoAPService;
    @Autowired private GestionAccesoService gestionAccesoService;
    @Autowired private TemperaturaService temperaturaService;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private SitioRepository sitioRepository;
    @Autowired private IUsuarioDao usuarioDao;

    @GetMapping
    public String mostrar(@RequestParam(required = false) String sitio,
                          @RequestParam(required = false) String sala,
                          Authentication authentication,
                          Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        Usuario usuarioLogueado = usuarioDao.findByEmail(email).orElse(null);
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("sitiosDisponibles", sitioRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("sitioSeleccionado", sitio);
        model.addAttribute("salaSeleccionada", sala);

        // Sin sitio seleccionado → mostrar sólo el selector
        if (sitio == null || sitio.trim().isEmpty()) {
            return "dashboard-sitio";
        }

        LocalDate hoy           = LocalDate.now();
        LocalDate primerDiaMes  = hoy.withDayOfMonth(1);
        LocalDate ultimoDiaMes  = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<Sala> salasDisponibles = temperaturaService.listarSalasPorNombreSitio(sitio);
        if (salasDisponibles == null) {
            salasDisponibles = Collections.emptyList();
        }
        model.addAttribute("salasDisponibles", salasDisponibles);

        model.addAttribute("mesActual", hoy.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL")));
        model.addAttribute("anioActual", hoy.getYear());
        model.addAttribute("fechaActual", hoy);

        // ================================================================
        //  TAB 1 — ACCESO DIARIO
        // ================================================================
        Long ingresosHoy      = ingresoAPService.contarIngresosPorSitio(sitio, hoy, hoy);
        Long ticketsUnicosHoy = ingresoAPService.contarTicketsUnicosPorSitio(sitio, hoy, hoy);
        Long crqHoy           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("CRQ", sitio, hoy, hoy);
        Long incHoy           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("INC", sitio, hoy, hoy);
        long inspeccionHoy    = safe(ingresoAPService.contarTicketsUnicosPorTipoYSitio("Visita Inspectiva", sitio, hoy, hoy))
                              + safe(ingresoAPService.contarTicketsUnicosPorTipoYSitio("Ronda Rutinaria", sitio, hoy, hoy));

        long salasTIHoy  = safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI",       sitio, hoy, hoy))
                         + safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED", sitio, hoy, hoy));
        long salasREDHoy = safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas de RED",   sitio, hoy, hoy))
                         + safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED", sitio, hoy, hoy));

        Long gestionesHoy        = gestionAccesoService.contarGestionesDelDia(hoy, sitio);
        Long aprobadosHoy        = gestionAccesoService.contarTicketsPorEstadoYFecha("Aprobada", hoy, sitio);
        Long rechazadosHoy       = gestionAccesoService.contarTicketsRechazadosHoy(hoy, sitio);
        Long devueltosHoy        = gestionAccesoService.contarTicketsDevueltosHoy(hoy, sitio);
        Long pendientesAprobacion = gestionAccesoService.contarTicketsPorEstado("Pendiente", sitio);
        Long pendientesCierre    = gestionAccesoService.contarTicketsPendientesCierre(sitio);

        // Registros activos en el sitio hoy
        List<IngresoAP> registrosRaw = ingresoAPService.obtenerRegistrosActivosRecientesPorSitio(sitio, 50);
        List<RegistroActivoDto> registrosActivosHoy = registrosRaw.stream().map(ingreso -> {
            List<GestionAcceso> gestiones = gestionAccesoService.listarPorNumeroTicket(ingreso.getNumeroTicket());
            GestionAcceso gestion = (gestiones != null && !gestiones.isEmpty()) ? gestiones.get(0) : null;
            return RegistroActivoDto.of(ingreso, gestion);
        }).collect(Collectors.toList());

        model.addAttribute("ingresosHoy",           safe(ingresosHoy));
        model.addAttribute("ticketsUnicosHoy",      safe(ticketsUnicosHoy));
        model.addAttribute("cantidadCRQHoy",        safe(crqHoy));
        model.addAttribute("cantidadINCHoy",        safe(incHoy));
        model.addAttribute("cantidadInspeccionHoy", inspeccionHoy);
        model.addAttribute("salasTIHoy",            salasTIHoy);
        model.addAttribute("salasREDHoy",           salasREDHoy);
        model.addAttribute("gestionesHoy",          safe(gestionesHoy));
        model.addAttribute("aprobadosHoy",          safe(aprobadosHoy));
        model.addAttribute("rechazadosHoy",         safe(rechazadosHoy));
        model.addAttribute("devueltosHoy",          safe(devueltosHoy));
        model.addAttribute("pendientesAprobacion",  safe(pendientesAprobacion));
        model.addAttribute("pendientesCierre",      safe(pendientesCierre));
        model.addAttribute("registrosActivosHoy",   registrosActivosHoy);

        // ================================================================
        //  TAB 1 — ACCESO MENSUAL
        // ================================================================
        Long ingresosMes      = ingresoAPService.contarIngresosPorSitio(sitio, primerDiaMes, ultimoDiaMes);
        Long ticketsUnicosMes = ingresoAPService.contarTicketsUnicosPorSitio(sitio, primerDiaMes, ultimoDiaMes);
        Long crqMes           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("CRQ", sitio, primerDiaMes, ultimoDiaMes);
        Long incMes           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("INC", sitio, primerDiaMes, ultimoDiaMes);
        long inspeccionMes    = safe(ingresoAPService.contarTicketsUnicosPorTipoYSitio("Visita Inspectiva", sitio, primerDiaMes, ultimoDiaMes))
                              + safe(ingresoAPService.contarTicketsUnicosPorTipoYSitio("Ronda Rutinaria", sitio, primerDiaMes, ultimoDiaMes));

        long salasTIMes  = safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI",       sitio, primerDiaMes, ultimoDiaMes))
                         + safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED", sitio, primerDiaMes, ultimoDiaMes));
        long salasREDMes = safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas de RED",   sitio, primerDiaMes, ultimoDiaMes))
                         + safe(ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED", sitio, primerDiaMes, ultimoDiaMes));

        // Gestiones mensuales (acumulado diario)
        long gestionesTotalesMes = 0, aprobadosMes = 0, rechazadosMes = 0, devueltosMes = 0;
        for (LocalDate dia = primerDiaMes; !dia.isAfter(ultimoDiaMes); dia = dia.plusDays(1)) {
            gestionesTotalesMes += safe(gestionAccesoService.contarGestionesDelDia(dia, sitio));
            aprobadosMes        += safe(gestionAccesoService.contarTicketsPorEstadoYFecha("Aprobada", dia, sitio));
            rechazadosMes       += safe(gestionAccesoService.contarTicketsRechazadosHoy(dia, sitio));
            devueltosMes        += safe(gestionAccesoService.contarTicketsDevueltosHoy(dia, sitio));
        }

        model.addAttribute("ingresosMes",          safe(ingresosMes));
        model.addAttribute("ticketsUnicosMes",     safe(ticketsUnicosMes));
        model.addAttribute("cantidadCRQMes",       safe(crqMes));
        model.addAttribute("cantidadINCMes",       safe(incMes));
        model.addAttribute("cantidadInspeccionMes", inspeccionMes);
        model.addAttribute("salasTIMes",           salasTIMes);
        model.addAttribute("salasREDMes",          salasREDMes);
        model.addAttribute("gestionesTotalesMes",  gestionesTotalesMes);
        model.addAttribute("aprobadosMes",         aprobadosMes);
        model.addAttribute("rechazadosMes",        rechazadosMes);
        model.addAttribute("devueltosMes",         devueltosMes);

        // ================================================================
        //  TAB 2 — TEMPERATURAS
        // ================================================================
        TemperaturaResumen resumenTempDiario   = temperaturaService.obtenerResumenDiario(sitio, hoy);
        TemperaturaResumen resumenTempMensual  = temperaturaService.obtenerResumenMensual(sitio, hoy);
        List<?> ultimasMediciones              = temperaturaService.ultimasMedicionesDiarias(sitio, hoy);

        List<ResumenSalaDiario> resumenSalasDiario = new ArrayList<>();
        Optional<Sitio> sitioOpt = sitioRepository.findByNombreIgnoreCase(sitio);
        if (sitioOpt.isPresent()) {
            resumenSalasDiario = temperaturaService.obtenerResumenDiarioPorSitioId(sitioOpt.get().getId(), hoy);
        }

        Optional<ResumenSalaDiario> resumenSalaSeleccionada = Optional.empty();
        if (sala != null && !sala.isBlank()) {
            resumenSalaSeleccionada = resumenSalasDiario.stream()
                    .filter(r -> r.getSalaNombre() != null && r.getSalaNombre().equalsIgnoreCase(sala))
                    .findFirst();
        }

        // Chart lineal mensual — por sitio o por sala seleccionada
        List<String> tempLabels;
        List<String> tempValores;
        String tempSerieTitulo = "Promedio general del sitio";

        Optional<Sala> salaSeleccionadaOpt = Optional.empty();
        if (sala != null && !sala.isBlank()) {
            salaSeleccionadaOpt = salasDisponibles.stream()
                    .filter(s -> s.getNombre() != null && s.getNombre().equalsIgnoreCase(sala))
                    .findFirst();
        }

        if (salaSeleccionadaOpt.isPresent()) {
            Sala salaEntidad = salaSeleccionadaOpt.get();
            tempSerieTitulo = "Promedio sala: " + salaEntidad.getNombre();
            tempLabels = new ArrayList<>();
            tempValores = new ArrayList<>();
            for (LocalDate dia = primerDiaMes; !dia.isAfter(ultimoDiaMes); dia = dia.plusDays(1)) {
                tempLabels.add(String.valueOf(dia.getDayOfMonth()));
                var promedioSala = temperaturaService.obtenerPromedioDiarioSala(salaEntidad.getId(), dia);
                tempValores.add(promedioSala != null ? String.format(Locale.US, "%.2f", promedioSala) : "null");
            }
        } else {
            Map<String, Double> promediosDiarios = temperaturaService.promediosDiariosDelMes(sitio, primerDiaMes, ultimoDiaMes);
            tempLabels = new ArrayList<>(promediosDiarios.keySet());
            tempValores = promediosDiarios.values().stream()
                    .map(v -> v != null ? String.format(Locale.US, "%.2f", v) : "null")
                    .collect(Collectors.toList());
        }

        int tempPuntosDiario = resumenSalaSeleccionada
            .map(ResumenSalaDiario::getTotalPuntos)
            .orElse(resumenTempDiario != null ? resumenTempDiario.getTotalPuntos() : 0);

        int tempPuntosMensual = resumenSalaSeleccionada
            .map(ResumenSalaDiario::getTotalPuntos)
            .orElse(resumenTempMensual != null ? resumenTempMensual.getTotalPuntos() : 0);

        model.addAttribute("resumenTempDiario",   resumenTempDiario);
        model.addAttribute("resumenTempMensual",  resumenTempMensual);
        model.addAttribute("ultimasMediciones",   ultimasMediciones);
        model.addAttribute("resumenSalasDiario",  resumenSalasDiario);
        model.addAttribute("tempLabels",          tempLabels);
        model.addAttribute("tempValores",         tempValores);
        model.addAttribute("tempSerieTitulo",     tempSerieTitulo);
        model.addAttribute("tempPuntosDiario",    tempPuntosDiario);
        model.addAttribute("tempPuntosMensual",   tempPuntosMensual);

        // ================================================================
        //  TAB 3 — INVENTARIO
        // ================================================================
        List<Inventario> inventarioSitio = inventarioRepository.findAllBySitioNombre(sitio);
        List<Inventario> inventarioFiltrado = inventarioSitio;
        if (sala != null && !sala.isBlank()) {
            inventarioFiltrado = inventarioSitio.stream()
                .filter(i -> i.getSala() != null && i.getSala().equalsIgnoreCase(sala))
                .collect(Collectors.toList());
        }

        // "UR Ocupada" representa ocupacion de espacio, no un equipo fisico.
        // "Cable" tampoco se considera equipo para estos indicadores.
        List<Inventario> inventarioEquipos = inventarioFiltrado.stream()
            .filter(i -> i.getEstado() == null || !i.getEstado().toLowerCase().contains("ur ocupada"))
            .filter(i -> i.getTipo() == null || !i.getTipo().toLowerCase().contains("cable"))
            .collect(Collectors.toList());

        long totalEquipos    = inventarioEquipos.size();
        long totalRacks      = inventarioEquipos.stream().filter(i -> "Rack".equalsIgnoreCase(i.getTipo())).count();
        long equiposApagados = inventarioEquipos.stream().filter(i -> "Apagado".equalsIgnoreCase(i.getEstado())).count();
        long equiposEnAlerta = inventarioEquipos.stream().filter(i ->
                Boolean.TRUE.equals(i.getAlarmaHardware())   ||
                Boolean.TRUE.equals(i.getAlarmaVentilador()) ||
                Boolean.TRUE.equals(i.getAlarmaFuentePoder()) ||
                Boolean.TRUE.equals(i.getAlarmaHdd())).count();

        // Separacion visual por categoria: Rack | Batidor/Bastidor | Equipo (resto)
        long invCatRack = inventarioEquipos.stream()
            .filter(i -> i.getTipo() != null && i.getTipo().toLowerCase().contains("rack"))
            .count();
        long invCatBatidor = inventarioEquipos.stream()
            .filter(i -> i.getTipo() != null)
            .map(i -> i.getTipo().toLowerCase())
            .filter(t -> t.contains("batidor") || t.contains("bastidor"))
            .count();
        long invCatEquipo = inventarioEquipos.stream()
            .filter(i -> {
                if (i.getTipo() == null) return true;
                String t = i.getTipo().toLowerCase();
                return !t.contains("rack") && !t.contains("batidor") && !t.contains("bastidor");
            })
            .count();

        long alarmasHw     = inventarioEquipos.stream().filter(i -> Boolean.TRUE.equals(i.getAlarmaHardware())).count();
        long alarmasVent   = inventarioEquipos.stream().filter(i -> Boolean.TRUE.equals(i.getAlarmaVentilador())).count();
        long alarmasFuente = inventarioEquipos.stream().filter(i -> Boolean.TRUE.equals(i.getAlarmaFuentePoder())).count();
        long alarmasHdd    = inventarioEquipos.stream().filter(i -> Boolean.TRUE.equals(i.getAlarmaHdd())).count();

        long monofuentes = inventarioEquipos.stream().filter(i -> {
            String fp = i.getFuentesPoder();
            if (fp == null) return false;
            String lower = fp.toLowerCase().trim();
            return lower.equals("1") || lower.startsWith("1 ") ||
                   lower.contains("mono") || lower.contains("single") || lower.contains("una fuente");
        }).count();

        // Unidades de rack (UR)
        long urOcupadas = 0, urTotales = 0;
        for (Inventario inv : inventarioFiltrado) {
            if ("Rack".equalsIgnoreCase(inv.getTipo()) && inv.getCapacidadUrRack() != null) {
                urTotales += inv.getCapacidadUrRack();
            }

            if (inv.getEstado() != null && inv.getEstado().toLowerCase().contains("ur ocupada")) {
                urOcupadas += 1;
                continue;
            }

            String urStr = inv.getUrUtilizada();
            if (urStr != null && urStr.trim().matches("\\d+")) {
                urOcupadas += Long.parseLong(urStr.trim());
            }
        }
        long urLibres = Math.max(0, urTotales - urOcupadas);

        // Equipos por tipo (ordenado de mayor a menor)
        Map<String, Long> equiposPorTipo = inventarioEquipos.stream()
                .filter(i -> i.getTipo() != null && !i.getTipo().isBlank())
                .collect(Collectors.groupingBy(Inventario::getTipo, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        // Equipos por sala
        Map<String, Long> equiposPorSala = inventarioEquipos.stream()
                .filter(i -> i.getSala() != null && !i.getSala().isBlank())
                .collect(Collectors.groupingBy(Inventario::getSala,
                        LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> equiposPorArea = inventarioEquipos.stream()
            .collect(Collectors.groupingBy(i -> clasificarAreaInventario(i.getSala()),
                LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> racksPorArea = inventarioEquipos.stream()
            .filter(i -> "Rack".equalsIgnoreCase(i.getTipo()))
            .collect(Collectors.groupingBy(i -> clasificarAreaInventario(i.getSala()),
                LinkedHashMap::new, Collectors.counting()));

        Map<String, Double> kWattsPorArea = new LinkedHashMap<>();
        for (Inventario inv : inventarioEquipos) {
            String area = clasificarAreaInventario(inv.getSala());
            if (inv.getPotenciaConsumoWatts() != null) {
            kWattsPorArea.merge(area, inv.getPotenciaConsumoWatts().doubleValue(), (a, b) -> a + b);
            }
        }

        // kWatts por sala
        Map<String, Double> kWattsPorSala = new LinkedHashMap<>();
        for (Inventario inv : inventarioEquipos) {
            if (inv.getSala() != null && !inv.getSala().isBlank() && inv.getPotenciaConsumoWatts() != null) {
                kWattsPorSala.merge(inv.getSala(), inv.getPotenciaConsumoWatts().doubleValue(), (a, b) -> a + b);
            }
        }

        // Inventario diario/mensual
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime inicioMes = primerDiaMes.atStartOfDay();

        long ingresadosHoy = inventarioEquipos.stream()
            .filter(i -> i.getFechaCreacion() != null && !i.getFechaCreacion().isBefore(inicioHoy))
            .count();
        long ingresadosMes = inventarioEquipos.stream()
                .filter(i -> i.getFechaCreacion() != null &&
                             !i.getFechaCreacion().toLocalDate().isBefore(primerDiaMes))
                .count();

        long retiradosHoy = inventarioEquipos.stream()
            .filter(i -> i.getEstado() != null && i.getEstado().toLowerCase().contains("retir"))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioHoy))
            .count();
        long retiradosMes = inventarioEquipos.stream()
            .filter(i -> i.getEstado() != null && i.getEstado().toLowerCase().contains("retir"))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioMes))
            .count();

        long apagadosHoy = inventarioEquipos.stream()
            .filter(i -> "Apagado".equalsIgnoreCase(i.getEstado()))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioHoy))
            .count();
        long apagadosMes = inventarioEquipos.stream()
            .filter(i -> "Apagado".equalsIgnoreCase(i.getEstado()))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioMes))
            .count();

        long alertasHoy = inventarioEquipos.stream()
            .filter(i -> (Boolean.TRUE.equals(i.getAlarmaHardware()) || Boolean.TRUE.equals(i.getAlarmaVentilador())
                   || Boolean.TRUE.equals(i.getAlarmaFuentePoder()) || Boolean.TRUE.equals(i.getAlarmaHdd())))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioHoy))
            .count();
        long alertasMes = inventarioEquipos.stream()
            .filter(i -> (Boolean.TRUE.equals(i.getAlarmaHardware()) || Boolean.TRUE.equals(i.getAlarmaVentilador())
                   || Boolean.TRUE.equals(i.getAlarmaFuentePoder()) || Boolean.TRUE.equals(i.getAlarmaHdd())))
            .filter(i -> i.getFechaModificacion() != null && !i.getFechaModificacion().isBefore(inicioMes))
            .count();

        String invScopeTitulo = (sala != null && !sala.isBlank()) ? (sitio + " / " + sala) : sitio;

        model.addAttribute("invTotalEquipos",    totalEquipos);
        model.addAttribute("invTotalRacks",      totalRacks);
        model.addAttribute("invCatRack",         invCatRack);
        model.addAttribute("invCatBatidor",      invCatBatidor);
        model.addAttribute("invCatEquipo",       invCatEquipo);
        model.addAttribute("invEquiposApagados", equiposApagados);
        model.addAttribute("invEquiposEnAlerta", equiposEnAlerta);
        model.addAttribute("invAlarmasHw",       alarmasHw);
        model.addAttribute("invAlarmasVent",     alarmasVent);
        model.addAttribute("invAlarmasFuente",   alarmasFuente);
        model.addAttribute("invAlarmasHdd",      alarmasHdd);
        model.addAttribute("invMonofuentes",     monofuentes);
        model.addAttribute("invUrOcupadas",      urOcupadas);
        model.addAttribute("invUrLibres",        urLibres);
        model.addAttribute("invUrTotales",       urTotales);
        model.addAttribute("invEquiposPorTipo",  equiposPorTipo);
        model.addAttribute("invEquiposPorSala",  equiposPorSala);
        model.addAttribute("invKWattsPorSala",   kWattsPorSala);
        model.addAttribute("invEquiposPorArea",  equiposPorArea);
        model.addAttribute("invRacksPorArea",    racksPorArea);
        model.addAttribute("invKWattsPorArea",   kWattsPorArea);
        model.addAttribute("invIngresadosHoy",   ingresadosHoy);
        model.addAttribute("invIngresadosMes",   ingresadosMes);
        model.addAttribute("invRetiradosHoy",    retiradosHoy);
        model.addAttribute("invRetiradosMes",    retiradosMes);
        model.addAttribute("invApagadosHoy",     apagadosHoy);
        model.addAttribute("invApagadosMes",     apagadosMes);
        model.addAttribute("invAlertasHoy",      alertasHoy);
        model.addAttribute("invAlertasMes",      alertasMes);
        model.addAttribute("invScopeTitulo",     invScopeTitulo);

        // ================================================================
        //  TAB 4 — KPI MENSUAL
        // ================================================================
        double kpiPctAprobados = gestionesTotalesMes > 0
                ? (aprobadosMes * 100.0 / gestionesTotalesMes) : 0;
        double kpiPctRechazados = gestionesTotalesMes > 0
                ? (rechazadosMes * 100.0 / gestionesTotalesMes) : 0;
        double kpiPctDevueltos = gestionesTotalesMes > 0
                ? (devueltosMes * 100.0 / gestionesTotalesMes) : 0;

        double kpiPctAprobadosRound = Math.round(kpiPctAprobados * 10.0) / 10.0;
        double kpiPctRechazadosRound = Math.round(kpiPctRechazados * 10.0) / 10.0;
        double kpiPctDevueltosRound = Math.round(kpiPctDevueltos * 10.0) / 10.0;

        String kpiClassAprobacion;
        if (kpiPctAprobadosRound <= 0) {
            kpiClassAprobacion = "bg-gradient-to-br from-gray-400 to-gray-600";
        } else if (kpiPctAprobadosRound >= 80) {
            kpiClassAprobacion = "semaforo-ok";
        } else if (kpiPctAprobadosRound >= 50) {
            kpiClassAprobacion = "semaforo-warn";
        } else {
            kpiClassAprobacion = "semaforo-bad";
        }

        String kpiBarAprobadosWidth = String.format(Locale.US, "%.1f%%", Math.max(0.0, Math.min(100.0, kpiPctAprobadosRound)));
        String kpiBarRechazadosWidth = String.format(Locale.US, "%.1f%%", Math.max(0.0, Math.min(100.0, kpiPctRechazadosRound)));
        String kpiBarDevueltosWidth = String.format(Locale.US, "%.1f%%", Math.max(0.0, Math.min(100.0, kpiPctDevueltosRound)));

        model.addAttribute("kpiGestionesTotalesMes",  gestionesTotalesMes);
        model.addAttribute("kpiAprobadosMes",         aprobadosMes);
        model.addAttribute("kpiRechazadosMes",        rechazadosMes);
        model.addAttribute("kpiDevueltosMes",         devueltosMes);
        model.addAttribute("kpiPctAprobados",         kpiPctAprobadosRound);
        model.addAttribute("kpiPctRechazados",        kpiPctRechazadosRound);
        model.addAttribute("kpiPctDevueltos",         kpiPctDevueltosRound);
        model.addAttribute("kpiClassAprobacion",      kpiClassAprobacion);
        model.addAttribute("kpiBarAprobadosWidth",    kpiBarAprobadosWidth);
        model.addAttribute("kpiBarRechazadosWidth",   kpiBarRechazadosWidth);
        model.addAttribute("kpiBarDevueltosWidth",    kpiBarDevueltosWidth);
        model.addAttribute("kpiPendientesAprobacion", safe(pendientesAprobacion));
        model.addAttribute("kpiPendientesCierre",     safe(pendientesCierre));
        model.addAttribute("kpiIngresosMes",          safe(ingresosMes));

        return "dashboard-sitio";
    }

    private long safe(Long v) {
        return v != null ? v : 0L;
    }

    private String clasificarAreaInventario(String sala) {
        if (sala == null) {
            return "OTRO";
        }
        String valor = sala.toLowerCase();
        if (valor.contains("red")) {
            return "RED";
        }
        if (valor.contains("ti")) {
            return "TI";
        }
        if (valor.contains("empresa")) {
            return "EMPRESA";
        }
        if (valor.contains("noc")) {
            return "NOC";
        }
        return "OTRO";
    }
}
