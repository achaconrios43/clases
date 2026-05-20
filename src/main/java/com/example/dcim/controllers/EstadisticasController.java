package com.example.dcim.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dcim.dao.IUsuarioDao;
import com.example.dcim.dao.SitioRepository;
import com.example.dcim.dto.RegistroActivoDto;
import com.example.dcim.entity.GestionAcceso;
import com.example.dcim.entity.IngresoAP;
import com.example.dcim.entity.MedicionTemperatura;
import com.example.dcim.entity.Usuario;
import com.example.dcim.service.GestionAccesoService;
import com.example.dcim.service.IngresoAPService;
import com.example.dcim.service.TemperaturaService;

/**
 * Controlador del módulo unificado de Estadísticas.
 * Accesible por CLIENTE, ADMIN y VIEWER.
 * Centraliza en una sola página las estadísticas diarias y mensuales de:
 *   - Ingresos técnicos (Ingreso AP)
 *   - Gestión de accesos
 *   - Temperatura
 */
@Controller
@RequestMapping("/estadisticas")
public class EstadisticasController {

    @Autowired private IngresoAPService ingresoAPService;
    @Autowired private GestionAccesoService gestionAccesoService;
    @Autowired private TemperaturaService temperaturaService;
    @Autowired private IUsuarioDao usuarioDao;
    @Autowired private SitioRepository sitioRepository;

    @GetMapping
    public String mostrar(
            @RequestParam(required = false) String sitio,
            @RequestParam(required = false, defaultValue = "mensual") String modo,
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioDao.findByEmail(email).orElse(null);
        if (usuario == null) return "redirect:/login";

        String rol = usuario.getRol();
        // Solo ADMIN, VIEWER y CLIENTE pueden acceder
        if (!"ADMIN".equals(rol) && !"VIEWER".equals(rol) && !"CLIENTE".equals(rol)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("usuarioLogueado", usuario);
        model.addAttribute("sitiosDisponibles", sitioRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("sitioSeleccionado", sitio);
        model.addAttribute("modo", modo);

        boolean conSitio = sitio != null && !sitio.trim().isEmpty();
        LocalDate hoy = LocalDate.now();

        model.addAttribute("fechaActual", hoy);
        model.addAttribute("mesActual", hoy.getMonth().toString());
        model.addAttribute("anioActual", hoy.getYear());
        model.addAttribute("diaSemana", hoy.getDayOfWeek().toString());

        if (!conSitio) {
            // Solo mostrar el selector, sin calcular nada
            return "estadisticas";
        }

        // ═══════════════════════════════════════════════════════
        //  PESTAÑA DIARIA
        // ═══════════════════════════════════════════════════════
        // --- Ingresos del día ---
        Long ingresosHoy         = ingresoAPService.contarIngresosPorSitio(sitio, hoy, hoy);
        Long ticketsUnicosHoy    = ingresoAPService.contarTicketsUnicosPorSitio(sitio, hoy, hoy);
        Long cantidadCRQHoy      = ingresoAPService.contarTicketsUnicosPorTipoYSitio("CRQ",              sitio, hoy, hoy);
        Long cantidadINCHoy      = ingresoAPService.contarTicketsUnicosPorTipoYSitio("INC",              sitio, hoy, hoy);
        Long visitaHoy           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("Visita Inspectiva",sitio, hoy, hoy);
        Long rondaHoy            = ingresoAPService.contarTicketsUnicosPorTipoYSitio("Ronda Rutinaria",  sitio, hoy, hoy);
        Long inspeccionHoy       = nvl(visitaHoy) + nvl(rondaHoy);
        Long salasTIRawHoy       = ingresoAPService.contarPorSalaRemedyYSitio("Salas TI",     sitio, hoy, hoy);
        Long salasREDRawHoy      = ingresoAPService.contarPorSalaRemedyYSitio("Salas de RED", sitio, hoy, hoy);
        Long salasTIyREDHoy      = ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED", sitio, hoy, hoy);
        Long salasTIHoy          = nvl(salasTIRawHoy)  + nvl(salasTIyREDHoy);
        Long salasREDHoy         = nvl(salasREDRawHoy) + nvl(salasTIyREDHoy);

        List<IngresoAP> rawHoy = ingresoAPService.obtenerRegistrosActivosRecientesPorSitio(sitio, 50);
        List<RegistroActivoDto> registrosHoy = rawHoy.stream().map(ingreso -> {
            List<GestionAcceso> gestiones = gestionAccesoService.listarPorNumeroTicket(ingreso.getNumeroTicket());
            GestionAcceso g = (gestiones != null && !gestiones.isEmpty()) ? gestiones.get(0) : null;
            return RegistroActivoDto.of(ingreso, g);
        }).collect(Collectors.toList());

        model.addAttribute("ingresosHoy",            nvl(ingresosHoy));
        model.addAttribute("ticketsUnicosHoy",       nvl(ticketsUnicosHoy));
        model.addAttribute("cantidadCRQHoy",         nvl(cantidadCRQHoy));
        model.addAttribute("cantidadINCHoy",         nvl(cantidadINCHoy));
        model.addAttribute("cantidadInspeccionHoy",  inspeccionHoy);
        model.addAttribute("salasTIHoy",             salasTIHoy);
        model.addAttribute("salasREDHoy",            salasREDHoy);
        model.addAttribute("registrosActivosHoy",    registrosHoy);  // lista de RegistroActivoDto

        // --- Gestión del día ---
        Long gestionesDelDia            = gestionAccesoService.contarGestionesDelDia(hoy, sitio);
        Long ticketsAprobadosHoy        = gestionAccesoService.contarTicketsPorEstadoYFecha("Aprobada", hoy, sitio);
        Long ticketsPendientesAprobHoy  = gestionAccesoService.contarTicketsPorEstado("Pendiente", sitio);
        Long ticketsRechazadosHoy       = gestionAccesoService.contarTicketsRechazadosHoy(hoy, sitio);
        Long ticketsDevueltosHoy        = gestionAccesoService.contarTicketsDevueltosHoy(hoy, sitio);
        Long ticketsPendCierreHoy       = gestionAccesoService.contarTicketsPendientesCierre(sitio);

        model.addAttribute("gestionesDelDia",            nvl(gestionesDelDia));
        model.addAttribute("ticketsAprobadosHoy",        nvl(ticketsAprobadosHoy));
        model.addAttribute("ticketsPendientesAprobacion",nvl(ticketsPendientesAprobHoy));
        model.addAttribute("ticketsRechazadosHoy",       nvl(ticketsRechazadosHoy));
        model.addAttribute("ticketsDevueltosHoy",        nvl(ticketsDevueltosHoy));
        model.addAttribute("ticketsPendientesCierre",    nvl(ticketsPendCierreHoy));

        // --- Temperatura del día ---
        TemperaturaService.TemperaturaResumen resumenTempDiario = temperaturaService.obtenerResumenDiario(sitio, hoy);
        List<MedicionTemperatura> ultimasMediciones = temperaturaService.ultimasMedicionesDiarias(sitio, hoy);
        model.addAttribute("resumenTempDiario",    resumenTempDiario);
        model.addAttribute("ultimasMedicionesTemp",ultimasMediciones);

        // ═══════════════════════════════════════════════════════
        //  PESTAÑA MENSUAL
        // ═══════════════════════════════════════════════════════
        LocalDate primerDia = hoy.withDayOfMonth(1);
        LocalDate ultimoDia = hoy.withDayOfMonth(hoy.lengthOfMonth());

        // --- Ingresos del mes ---
        Long ingresosTotalesMes  = ingresoAPService.contarIngresosPorSitio(sitio, primerDia, ultimoDia);
        Long ticketsUnicosMes    = ingresoAPService.contarTicketsUnicosPorSitio(sitio, primerDia, ultimoDia);
        Long cantidadCRQ         = ingresoAPService.contarTicketsUnicosPorTipoYSitio("CRQ",              sitio, primerDia, ultimoDia);
        Long cantidadINC         = ingresoAPService.contarTicketsUnicosPorTipoYSitio("INC",              sitio, primerDia, ultimoDia);
        Long visitaMes           = ingresoAPService.contarTicketsUnicosPorTipoYSitio("Visita Inspectiva",sitio, primerDia, ultimoDia);
        Long rondaMes            = ingresoAPService.contarTicketsUnicosPorTipoYSitio("Ronda Rutinaria",  sitio, primerDia, ultimoDia);
        Long inspeccionRonda     = nvl(visitaMes) + nvl(rondaMes);
        Long salasTIRaw          = ingresoAPService.contarPorSalaRemedyYSitio("Salas TI",      sitio, primerDia, ultimoDia);
        Long salasREDRaw         = ingresoAPService.contarPorSalaRemedyYSitio("Salas de RED",  sitio, primerDia, ultimoDia);
        Long salasTIyRED         = ingresoAPService.contarPorSalaRemedyYSitio("Salas TI & RED",sitio, primerDia, ultimoDia);
        Long salasTI             = nvl(salasTIRaw)  + nvl(salasTIyRED);
        Long salasRED            = nvl(salasREDRaw) + nvl(salasTIyRED);

        model.addAttribute("ingresosTotalesMes",   nvl(ingresosTotalesMes));
        model.addAttribute("ticketsUnicos",         nvl(ticketsUnicosMes));
        model.addAttribute("cantidadCRQ",           nvl(cantidadCRQ));
        model.addAttribute("cantidadINC",           nvl(cantidadINC));
        model.addAttribute("cantidadInspeccionRonda",inspeccionRonda);
        model.addAttribute("salasTI",               salasTI);
        model.addAttribute("salasRED",              salasRED);

        // --- Gestión del mes ---
        long gestionesTotalesMes   = 0L;
        long ticketsAprobadosMes   = 0L;
        long ticketsRechazadosMes  = 0L;
        long ticketsDevueltosMes   = 0L;
        LocalDate cursor = primerDia;
        while (!cursor.isAfter(ultimoDia)) {
            gestionesTotalesMes  += nvl(gestionAccesoService.contarGestionesDelDia(cursor, sitio));
            ticketsAprobadosMes  += nvl(gestionAccesoService.contarTicketsPorEstadoYFecha("Aprobada", cursor, sitio));
            ticketsRechazadosMes += nvl(gestionAccesoService.contarTicketsRechazadosHoy(cursor, sitio));
            ticketsDevueltosMes  += nvl(gestionAccesoService.contarTicketsDevueltosHoy(cursor, sitio));
            cursor = cursor.plusDays(1);
        }
        Long pendientesAprobMes = gestionAccesoService.contarTicketsPorEstado("Pendiente", sitio);
        Long pendientesCierreMes = gestionAccesoService.contarTicketsPendientesCierre(sitio);

        model.addAttribute("gestionesTotalesMes",        gestionesTotalesMes);
        model.addAttribute("ticketsAprobadosMes",        ticketsAprobadosMes);
        model.addAttribute("ticketsRechazadosMes",       ticketsRechazadosMes);
        model.addAttribute("ticketsDevueltosMes",        ticketsDevueltosMes);
        model.addAttribute("ticketsPendientesAprobacion",nvl(pendientesAprobMes));
        model.addAttribute("ticketsPendientesCierre",    nvl(pendientesCierreMes));

        // --- Temperatura del mes ---
        TemperaturaService.TemperaturaResumen resumenTempMensual = temperaturaService.obtenerResumenMensual(sitio, hoy);
        Map<String, Double> promediosDiarios = temperaturaService.promediosDiariosDelMes(sitio, primerDia, ultimoDia);
        List<String> tempLabels  = new ArrayList<>(promediosDiarios.keySet());
        List<String> tempValores = new ArrayList<>();
        for (Double v : promediosDiarios.values()) {
            tempValores.add(v != null ? String.format("%.1f", v).replace(',', '.') : "null");
        }
        model.addAttribute("resumenTempMensual", resumenTempMensual);
        model.addAttribute("tempLabels",  tempLabels);
        model.addAttribute("tempValores", tempValores);

        return "estadisticas";
    }

    private long nvl(Long v) {
        return v != null ? v : 0L;
    }
}
