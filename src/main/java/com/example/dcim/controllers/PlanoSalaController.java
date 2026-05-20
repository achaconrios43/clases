package com.example.dcim.controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dcim.dao.InventarioRepository;
import com.example.dcim.dao.PlanoSalaElementoRepository;
import com.example.dcim.dao.PlanoSalaRepository;
import com.example.dcim.dao.SalaRepository;
import com.example.dcim.dao.SitioRepository;
import com.example.dcim.entity.Inventario;
import com.example.dcim.entity.PlanoSala;
import com.example.dcim.entity.PlanoSalaElemento;
import com.example.dcim.entity.Sala;

@Controller
@RequestMapping("/plano-sala")
public class PlanoSalaController {

    @Autowired
    private PlanoSalaRepository planoSalaRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private PlanoSalaElementoRepository planoSalaElementoRepository;

    @Autowired
    private SitioRepository sitioRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("planos", planoSalaRepository.findByEsPlantillaFalseOrderByFechaCreacionDesc());
        model.addAttribute("sitios", sitioRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("salas", salaRepository.findAll());
        return "plano-sala";
    }

    @GetMapping("/layout")
    public String layout() {
        return "redirect:/plano-sala";
    }

    /**
     * GET /plano-sala/layout/{salaId}/datos — devuelve JSON del layout guardado
     */
    @GetMapping("/layout/{salaId}/datos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLayoutDatos(@PathVariable Long salaId) {
        return planoSalaRepository.findFirstBySalaIdAndLayoutJsonIsNotNull(salaId)
                .map(p -> ResponseEntity.ok(Map.<String, Object>of(
                "gridJson", p.getLayoutJson() == null ? "{}" : p.getLayoutJson(),
                "cols", p.getCantidadColumnas(),
                "rows", p.getCantidadFilas()
        )))
                .orElse(ResponseEntity.ok(Map.<String, Object>of("gridJson", "{}", "cols", 0, "rows", 0)));
    }

    /**
     * GET /plano-sala/api/racks-inventario/{salaId}
     * Devuelve los racks del inventario que tienen coordenadas asignadas para la sala dada.
     * Usado por el editor y visor de planos para mostrar posiciones de racks.
     */
    @GetMapping("/api/racks-inventario/{salaId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRacksInventario(@PathVariable Long salaId) {
        List<Inventario> items = inventarioRepository.findBySalaRefIdConCoordenadas(salaId);

        // Agrupar por coordenada+nombreRack (una entrada por posición de rack)
        Map<String, List<Inventario>> byCoord = new LinkedHashMap<>();
        for (Inventario i : items) {
            String coord = i.getCoordenadas() == null ? "" : i.getCoordenadas().trim().toUpperCase();
            if (coord.isEmpty()) continue;
            byCoord.computeIfAbsent(coord, k -> new ArrayList<>()).add(i);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Inventario>> entry : byCoord.entrySet()) {
            String coord = entry.getKey();
            List<Inventario> inv = entry.getValue();

            // Nombre del rack: primero no nulo / no vacío / no "N/A"
            String rackName = coord;
            for (Inventario i : inv) {
                String n = i.getNombreRack();
                if (n != null && !n.isBlank() && !"N/A".equalsIgnoreCase(n)) {
                    rackName = n;
                    break;
                }
            }

            // Convertir coord "A3" → gridKey "A-3"
            String gridKey = parseCoordToGridKey(coord);

            List<Map<String, Object>> equipos = new ArrayList<>();
            for (Inventario i : inv) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("tipo",   i.getTipo()   != null ? i.getTipo()   : "");
                e.put("marca",  i.getMarca()  != null ? i.getMarca()  : "");
                e.put("modelo", i.getModelo() != null ? i.getModelo() : "");
                e.put("tag",    i.getTag()    != null ? i.getTag()    : "");
                e.put("estado", i.getEstado() != null ? i.getEstado() : "");
                equipos.add(e);
            }

            Map<String, Object> rack = new LinkedHashMap<>();
            rack.put("coordenadas",   coord);
            rack.put("gridKey",       gridKey);
            rack.put("nombreRack",    rackName);
            rack.put("totalEquipos",  equipos.size());
            rack.put("equipos",       equipos);
            result.add(rack);
        }

        return ResponseEntity.ok(result);
    }

    /** Convierte coordenada de inventario ("A3", "AB12") a clave de grilla ("A-3", "AB-12"). */
    private String parseCoordToGridKey(String coord) {
        StringBuilder letters = new StringBuilder();
        StringBuilder numbers = new StringBuilder();
        for (char c : coord.toCharArray()) {
            if (Character.isLetter(c)) letters.append(c);
            else if (Character.isDigit(c)) numbers.append(c);
        }
        if (letters.length() > 0 && numbers.length() > 0) {
            return letters.toString() + "-" + numbers.toString();
        }
        return coord;
    }

    /**
     * POST /plano-sala/layout/guardar — guarda o actualiza layout en BD
     */
    @PostMapping("/layout/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarLayout(@RequestBody Map<String, Object> body) {
        Object salaIdObj = body.get("salaId");
        if (salaIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "salaId requerido"));
        }
        Long salaId = Long.valueOf(salaIdObj.toString());
        Sala sala = salaRepository.findById(salaId).orElse(null);
        if (sala == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Sala no encontrada"));
        }
        String gridJson = body.getOrDefault("gridJson", "{}").toString();
        int cols = Integer.parseInt(body.getOrDefault("cols", "10").toString());
        int rows = Integer.parseInt(body.getOrDefault("rows", "10").toString());

        PlanoSala plano = planoSalaRepository.findFirstBySalaIdAndLayoutJsonIsNotNull(salaId)
                .orElse(new PlanoSala());
        plano.setSala(sala);
        plano.setCantidadColumnas(cols);
        plano.setCantidadFilas(rows);
        plano.setLayoutJson(gridJson);
        plano.setDescripcion("Layout " + sala.getNombre());
        planoSalaRepository.save(plano);

        return ResponseEntity.ok(Map.of("ok", true, "id", plano.getId()));
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Long salaId,
            @RequestParam int cantidadColumnas,
            @RequestParam int cantidadFilas,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String layoutJson,
            RedirectAttributes ra) {

        if (cantidadColumnas < 1 || cantidadColumnas > 100) {
            ra.addFlashAttribute("error", "Las columnas deben estar entre 1 y 100");
            return "redirect:/plano-sala";
        }
        if (cantidadFilas < 1 || cantidadFilas > 100) {
            ra.addFlashAttribute("error", "Las filas deben estar entre 1 y 100");
            return "redirect:/plano-sala";
        }

        Sala sala = salaRepository.findById(salaId).orElse(null);
        if (sala == null) {
            ra.addFlashAttribute("error", "Sala no encontrada");
            return "redirect:/plano-sala";
        }

        // Upsert: actualiza el plano existente de esa sala o crea uno nuevo
        PlanoSala plano = planoSalaRepository.findFirstBySalaId(salaId).orElse(new PlanoSala());
        plano.setSala(sala);
        plano.setCantidadColumnas(cantidadColumnas);
        plano.setCantidadFilas(cantidadFilas);
        plano.setDescripcion(descripcion);
        if (layoutJson != null && !layoutJson.isBlank()) {
            plano.setLayoutJson(layoutJson);
        }
        planoSalaRepository.save(plano);

        ra.addFlashAttribute("success",
                "Plano guardado: " + sala.getNombre() + " — " + cantidadColumnas + "×" + cantidadFilas);
        ra.addFlashAttribute("savedPlanoId", plano.getId());
        return "redirect:/plano-sala";
    }

    @GetMapping("/ver/{id}")
    public String verPlanoCanvas(@PathVariable Long id, Model model) {
        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        if (plano == null) {
            return "redirect:/plano-sala";
        }
        model.addAttribute("plano", plano);
        return "plano-sala-ver";
    }

    @GetMapping("/editar/{id}")
    public String editarPlano(@PathVariable Long id, Model model) {
        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        if (plano == null) {
            return "redirect:/plano-sala";
        }
        model.addAttribute("planos", planoSalaRepository.findAllByOrderByFechaCreacionDesc());
        model.addAttribute("salas", salaRepository.findAll());
        model.addAttribute("planoEditar", plano);
        return "plano-sala";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        planoSalaElementoRepository.deleteByPlanoSalaId(id);
        planoSalaRepository.deleteById(id);
        ra.addFlashAttribute("success", "Plano eliminado");
        return "redirect:/plano-sala/plantillas";
    }

    @PostMapping("/{id}/elementos")
    public String agregarElemento(
            @PathVariable Long id,
            @RequestParam String tipo,
            @RequestParam(required = false) String nombre,
            @RequestParam int columnaInicio,
            @RequestParam int filaInicio,
            @RequestParam int anchoCm,
            @RequestParam int largoCm,
            @RequestParam(defaultValue = "false") boolean rotado90,
            RedirectAttributes ra) {

        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        if (plano == null) {
            ra.addFlashAttribute("error", "Plano no encontrado");
            return "redirect:/plano-sala";
        }

        if (!isTipoValido(tipo)) {
            ra.addFlashAttribute("error", "Tipo de elemento no permitido");
            return "redirect:/plano-sala/" + id;
        }

        if (anchoCm <= 0 || largoCm <= 0) {
            ra.addFlashAttribute("error", "El ancho y largo deben ser mayores a 0");
            return "redirect:/plano-sala/" + id;
        }

        int anchoEfectivo = rotado90 ? largoCm : anchoCm;
        int largoEfectivo = rotado90 ? anchoCm : largoCm;

        if (!entraEnPlano(plano, columnaInicio, filaInicio, anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "El elemento se sale de los limites del plano");
            return "redirect:/plano-sala/" + id;
        }

        if (hayColision(plano, null, columnaInicio, filaInicio, anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "No se puede ubicar: colisiona con otro elemento");
            return "redirect:/plano-sala/" + id;
        }

        PlanoSalaElemento elemento = new PlanoSalaElemento();
        elemento.setPlanoSala(plano);
        elemento.setTipo(tipo);
        elemento.setNombre(nombre);
        elemento.setColumnaInicio(columnaInicio);
        elemento.setFilaInicio(filaInicio);
        elemento.setAnchoCm(anchoCm);
        elemento.setLargoCm(largoCm);
        elemento.setRotado90(rotado90);
        elemento.setColor(colorPorTipo(tipo));
        planoSalaElementoRepository.save(elemento);

        ra.addFlashAttribute("success", "Elemento agregado al layout");
        return "redirect:/plano-sala/" + id;
    }

    @GetMapping("/{id}/elementos/{elementoId}/eliminar")
    public String eliminarElemento(@PathVariable Long id, @PathVariable Long elementoId, RedirectAttributes ra) {
        PlanoSalaElemento elemento = planoSalaElementoRepository.findById(elementoId).orElse(null);
        if (elemento != null && isTipoBloqueado(elemento.getTipo())) {
            ra.addFlashAttribute("error", "Este tipo de elemento esta bloqueado para edicion");
            return "redirect:/plano-sala/" + id;
        }
        planoSalaElementoRepository.deleteById(elementoId);
        ra.addFlashAttribute("success", "Elemento eliminado");
        return "redirect:/plano-sala/" + id;
    }

    @PostMapping("/{id}/elementos/{elementoId}/mover")
    public String moverElemento(
            @PathVariable Long id,
            @PathVariable Long elementoId,
            @RequestParam int columnaInicio,
            @RequestParam int filaInicio,
            RedirectAttributes ra) {

        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        PlanoSalaElemento elemento = planoSalaElementoRepository.findById(elementoId).orElse(null);
        if (plano == null || elemento == null) {
            ra.addFlashAttribute("error", "Plano o elemento no encontrado");
            return "redirect:/plano-sala";
        }

        if (isTipoBloqueado(elemento.getTipo())) {
            ra.addFlashAttribute("error", "Este tipo de elemento esta bloqueado para edicion");
            return "redirect:/plano-sala/" + id;
        }

        int anchoEfectivo = elemento.isRotado90() ? elemento.getLargoCm() : elemento.getAnchoCm();
        int largoEfectivo = elemento.isRotado90() ? elemento.getAnchoCm() : elemento.getLargoCm();

        if (!entraEnPlano(plano, columnaInicio, filaInicio, anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "No se puede mover: se sale de los limites");
            return "redirect:/plano-sala/" + id;
        }

        if (hayColision(plano, elemento.getId(), columnaInicio, filaInicio, anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "No se puede mover: colisiona con otro elemento");
            return "redirect:/plano-sala/" + id;
        }

        elemento.setColumnaInicio(columnaInicio);
        elemento.setFilaInicio(filaInicio);
        planoSalaElementoRepository.save(elemento);
        ra.addFlashAttribute("success", "Elemento movido");
        return "redirect:/plano-sala/" + id;
    }

    @PostMapping("/{id}/elementos/{elementoId}/rotar")
    public String rotarElemento(@PathVariable Long id, @PathVariable Long elementoId, RedirectAttributes ra) {
        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        PlanoSalaElemento elemento = planoSalaElementoRepository.findById(elementoId).orElse(null);
        if (plano == null || elemento == null) {
            ra.addFlashAttribute("error", "Plano o elemento no encontrado");
            return "redirect:/plano-sala";
        }

        if (isTipoBloqueado(elemento.getTipo())) {
            ra.addFlashAttribute("error", "Este tipo de elemento esta bloqueado para edicion");
            return "redirect:/plano-sala/" + id;
        }

        boolean nuevoRotado = !elemento.isRotado90();
        int anchoEfectivo = nuevoRotado ? elemento.getLargoCm() : elemento.getAnchoCm();
        int largoEfectivo = nuevoRotado ? elemento.getAnchoCm() : elemento.getLargoCm();

        if (!entraEnPlano(plano, elemento.getColumnaInicio(), elemento.getFilaInicio(), anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "No se puede rotar: se sale de los limites");
            return "redirect:/plano-sala/" + id;
        }
        if (hayColision(plano, elemento.getId(), elemento.getColumnaInicio(), elemento.getFilaInicio(), anchoEfectivo, largoEfectivo)) {
            ra.addFlashAttribute("error", "No se puede rotar: colisiona con otro elemento");
            return "redirect:/plano-sala/" + id;
        }

        elemento.setRotado90(nuevoRotado);
        planoSalaElementoRepository.save(elemento);
        ra.addFlashAttribute("success", "Elemento rotado 90 grados");
        return "redirect:/plano-sala/" + id;
    }

    @GetMapping("/{id}")
    public String verPlano(@PathVariable Long id) {
        return "redirect:/plano-sala/ver/" + id;
    }

    // ---- PLANTILLAS ----
    @GetMapping("/plantillas")
    public String listarPlantillas(Model model) {
        model.addAttribute("plantillas", planoSalaRepository.findByEsPlantillaTrueOrderByNombrePlantillaAsc());
        model.addAttribute("salas", salaRepository.findAll());
        return "plano-sala-plantillas";
    }

    @GetMapping("/plantillas/{id}")
    public String verPlantilla(@PathVariable Long id, Model model) {
        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        if (plano == null || !plano.isEsPlantilla()) {
            return "redirect:/plano-sala/plantillas";
        }

        List<String> columnLabels = new ArrayList<>();
        for (int i = 0; i < plano.getCantidadColumnas(); i++) {
            columnLabels.add(columnLabel(i));
        }
        List<Integer> rowNumbers = new ArrayList<>();
        for (int r = plano.getCantidadFilas(); r >= 1; r--) {
            rowNumbers.add(r);
        }
        double areaTotalM2 = plano.getCantidadColumnas() * 0.60 * plano.getCantidadFilas() * 0.60;

        model.addAttribute("plano", plano);
        model.addAttribute("elementos", planoSalaElementoRepository.findByPlanoSalaIdOrderByIdAsc(id));
        model.addAttribute("columnLabels", columnLabels);
        model.addAttribute("rowNumbers", rowNumbers);
        model.addAttribute("areaTotalM2", String.format("%.2f", areaTotalM2));
        model.addAttribute("salas", salaRepository.findAll());
        return "plano-sala-plantilla-view";
    }

    @PostMapping("/{id}/guardar-plantilla")
    public String guardarComoPlantilla(
            @PathVariable Long id,
            @RequestParam String nombrePlantilla,
            RedirectAttributes ra) {

        if (nombrePlantilla == null || nombrePlantilla.isBlank()) {
            ra.addFlashAttribute("error", "Debe ingresar un nombre para la plantilla");
            return "redirect:/plano-sala/" + id;
        }
        PlanoSala plano = planoSalaRepository.findById(id).orElse(null);
        if (plano == null) {
            ra.addFlashAttribute("error", "Plano no encontrado");
            return "redirect:/plano-sala";
        }
        plano.setEsPlantilla(true);
        plano.setNombrePlantilla(nombrePlantilla.trim());
        planoSalaRepository.save(plano);
        ra.addFlashAttribute("success", "Plano guardado como plantilla: \"" + nombrePlantilla.trim() + "\"");
        return "redirect:/plano-sala/plantillas";
    }

    @PostMapping("/plantillas/{id}/aplicar")
    public String aplicarPlantilla(
            @PathVariable Long id,
            @RequestParam Long salaId,
            RedirectAttributes ra) {

        PlanoSala plantilla = planoSalaRepository.findById(id).orElse(null);
        if (plantilla == null || !plantilla.isEsPlantilla()) {
            ra.addFlashAttribute("error", "Plantilla no encontrada");
            return "redirect:/plano-sala/plantillas";
        }
        Sala sala = salaRepository.findById(salaId).orElse(null);
        if (sala == null) {
            ra.addFlashAttribute("error", "Sala no encontrada");
            return "redirect:/plano-sala/plantillas";
        }

        PlanoSala nuevo = new PlanoSala();
        nuevo.setSala(sala);
        nuevo.setCantidadColumnas(plantilla.getCantidadColumnas());
        nuevo.setCantidadFilas(plantilla.getCantidadFilas());
        nuevo.setDescripcion("Basado en plantilla: " + plantilla.getNombrePlantilla());
        nuevo.setEsPlantilla(false);
        planoSalaRepository.save(nuevo);

        for (PlanoSalaElemento orig : planoSalaElementoRepository.findByPlanoSalaIdOrderByIdAsc(id)) {
            PlanoSalaElemento copia = new PlanoSalaElemento();
            copia.setPlanoSala(nuevo);
            copia.setTipo(orig.getTipo());
            copia.setNombre(orig.getNombre());
            copia.setColumnaInicio(orig.getColumnaInicio());
            copia.setFilaInicio(orig.getFilaInicio());
            copia.setAnchoCm(orig.getAnchoCm());
            copia.setLargoCm(orig.getLargoCm());
            copia.setRotado90(orig.isRotado90());
            copia.setColor(orig.getColor());
            planoSalaElementoRepository.save(copia);
        }

        ra.addFlashAttribute("success", "Plantilla aplicada a " + sala.getNombre() + ". Puedes editarla ahora.");
        return "redirect:/plano-sala/" + nuevo.getId();
    }

    private static boolean isTipoValido(String tipo) {
        return "RACK".equals(tipo)
                || "BASTIDOR".equals(tipo)
                || "PUERTA".equals(tipo)
                || "MURO".equals(tipo)
                || "TABLERO_ENERGIA".equals(tipo)
                || "EQUIPO_CLIMA".equals(tipo);
    }

    private static boolean isTipoBloqueado(String tipo) {
        return "MURO".equals(tipo) || "PUERTA".equals(tipo);
    }

    private static String colorPorTipo(String tipo) {
        if ("RACK".equals(tipo)) {
            return "#4f46e5";
        }
        if ("BASTIDOR".equals(tipo)) {
            return "#0ea5e9";
        }
        if ("PUERTA".equals(tipo)) {
            return "#22c55e";
        }
        if ("MURO".equals(tipo)) {
            return "#6b7280";
        }
        if ("TABLERO_ENERGIA".equals(tipo)) {
            return "#f59e0b";
        }
        if ("EQUIPO_CLIMA".equals(tipo)) {
            return "#06b6d4";
        }
        return "#64748b";
    }

    private boolean entraEnPlano(PlanoSala plano, int columnaInicio, int filaInicio, int anchoCm, int largoCm) {
        if (columnaInicio < 1 || columnaInicio > plano.getCantidadColumnas()) {
            return false;
        }
        if (filaInicio < 1 || filaInicio > plano.getCantidadFilas()) {
            return false;
        }

        double maxAnchoCmDisponible = (plano.getCantidadColumnas() - columnaInicio + 1) * 60.0;
        double maxLargoCmDisponible = filaInicio * 60.0;
        return anchoCm <= maxAnchoCmDisponible && largoCm <= maxLargoCmDisponible;
    }

    private boolean hayColision(PlanoSala plano, Long elementoExcluirId, int columnaInicio, int filaInicio, int anchoCm, int largoCm) {
        int nuevoX1 = (columnaInicio - 1) * 60;
        int nuevoY1 = (filaInicio - 1) * 60;
        int nuevoX2 = nuevoX1 + anchoCm;
        int nuevoY2 = nuevoY1 + largoCm;

        List<PlanoSalaElemento> existentes = planoSalaElementoRepository.findByPlanoSalaIdOrderByIdAsc(plano.getId());
        for (PlanoSalaElemento actual : existentes) {
            if (elementoExcluirId != null && elementoExcluirId.equals(actual.getId())) {
                continue;
            }

            int actualAncho = actual.isRotado90() ? actual.getLargoCm() : actual.getAnchoCm();
            int actualLargo = actual.isRotado90() ? actual.getAnchoCm() : actual.getLargoCm();

            int ax1 = (actual.getColumnaInicio() - 1) * 60;
            int ay1 = (actual.getFilaInicio() - 1) * 60;
            int ax2 = ax1 + actualAncho;
            int ay2 = ay1 + actualLargo;

            boolean overlap = nuevoX1 < ax2 && nuevoX2 > ax1 && nuevoY1 < ay2 && nuevoY2 > ay1;
            if (overlap) {
                return true;
            }
        }
        return false;
    }

    private static String columnLabel(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            n--;
            sb.insert(0, (char) ('A' + (n % 26)));
            n /= 26;
        }
        return sb.toString();
    }
}
