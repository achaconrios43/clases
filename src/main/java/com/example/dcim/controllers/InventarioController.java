package com.example.dcim.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dcim.entity.Inventario;
import com.example.dcim.service.InventarioService;
import com.example.dcim.service.TemperaturaService;
import com.example.dcim.utils.ExcelImportUtil;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private TemperaturaService temperaturaService;

        private void prepararVistaInventario(Model model,
                         List<Inventario> items,
                         String sitio,
                         String sala,
                         String estado,
                         boolean filtroAplicado,
                         String filtroTexto) {
        model.addAttribute("inventarios", items);
        model.addAttribute("total", items.size());
        model.addAttribute("sitiosCatalogo", temperaturaService.listarSitiosActivos());
        model.addAttribute("salasPorSitio", temperaturaService.obtenerMapaSalasPorSitio());
        model.addAttribute("sitioSeleccionado", sitio);
        model.addAttribute("salaSeleccionada", sala);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("filtroAplicado", filtroAplicado);
        model.addAttribute("filtro", filtroTexto);
        }

    // Listar todos
    @GetMapping
    public String listar(Model model) {
        prepararVistaInventario(model, List.of(), null, null, null, false,
            "Seleccione sitio y sala para consultar inventario.");
        return "inventario";
    }

        // Filtrar por sitio + sala (+ estado opcional)
        @GetMapping("/filtrar")
        public String filtrarPorUbicacion(@RequestParam(required = false) String sitio,
                          @RequestParam(required = false) String sala,
                          @RequestParam(required = false) String estado,
                          Model model) {
        String sitioLimpio = sitio != null ? sitio.trim() : "";
        String salaLimpia = sala != null ? sala.trim() : "";
        String estadoLimpio = estado != null ? estado.trim() : "";

        if (sitioLimpio.isEmpty() || salaLimpia.isEmpty()) {
            prepararVistaInventario(model, List.of(), sitioLimpio, salaLimpia, estadoLimpio, false,
                "Debe seleccionar sitio y sala para ver el inventario.");
            return "inventario";
        }

        List<Inventario> items = inventarioService.obtenerPorSitioSalaYEstado(
            sitioLimpio,
            salaLimpia,
            estadoLimpio.isEmpty() ? null : estadoLimpio);

        String filtroTexto = "Sitio: " + sitioLimpio + " | Sala: " + salaLimpia
            + (estadoLimpio.isEmpty() ? "" : " | Estado: " + estadoLimpio);

        prepararVistaInventario(model, items, sitioLimpio, salaLimpia,
            estadoLimpio, true, filtroTexto);
        return "inventario";
        }

    // Buscar
    @GetMapping("/buscar")
    public String buscar(@RequestParam("search") String search, Model model) {
        List<Inventario> items = inventarioService.buscar(search);
        model.addAttribute("search", search);
        prepararVistaInventario(model, items, null, null, null, true, "Búsqueda: " + search);
        return "inventario";
    }

    // Mostrar formulario crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("inventario", new Inventario());
        model.addAttribute("sitiosCatalogo", temperaturaService.listarSitiosActivos());
        model.addAttribute("salasCatalogo", temperaturaService.listarTodasLasSalas());
        return "inventario-form";
    }

    // Guardar nuevo
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Inventario inventario, RedirectAttributes redirectAttributes) {
        try {
            inventarioService.crear(inventario);
            redirectAttributes.addFlashAttribute("success", "Inventario creado correctamente");
            return "redirect:/inventario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/inventario/crear";
        }
    }

    // Mostrar formulario editar
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Inventario> inventario = inventarioService.obtenerPorId(id);
        if (inventario.isPresent()) {
            model.addAttribute("inventario", inventario.get());
            model.addAttribute("editar", true);
            model.addAttribute("sitiosCatalogo", temperaturaService.listarSitiosActivos());
            model.addAttribute("salasCatalogo", temperaturaService.listarTodasLasSalas());
            return "inventario-form";
        }
        redirectAttributes.addFlashAttribute("error", "Inventario no encontrado");
        return "redirect:/inventario";
    }

    // Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Inventario inventario, RedirectAttributes redirectAttributes) {
        try {
            inventario.setId(id);
            inventarioService.actualizar(inventario);
            redirectAttributes.addFlashAttribute("success", "Inventario actualizado correctamente");
            return "redirect:/inventario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/inventario/editar/" + id;
        }
    }

    // Ver detalles
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Inventario> inventario = inventarioService.obtenerPorId(id);
        if (inventario.isPresent()) {
            model.addAttribute("inventario", inventario.get());
            return "inventario-view";
        }
        redirectAttributes.addFlashAttribute("error", "Inventario no encontrado");
        return "redirect:/inventario";
    }

    // Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Inventario eliminado correctamente");
            return "redirect:/inventario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/inventario";
        }
    }

    // Filtrar por estado
    @GetMapping("/filtrar/estado/{estado}")
    public String filtrarPorEstado(@PathVariable String estado, Model model) {
        List<Inventario> items = inventarioService.obtenerPorEstado(estado);
        prepararVistaInventario(model, items, null, null, estado, true, "Estado: " + estado);
        return "inventario";
    }

    // Filtrar por cliente
    @GetMapping("/filtrar/cliente/{cliente}")
    public String filtrarPorCliente(@PathVariable String cliente, Model model) {
        List<Inventario> items = inventarioService.obtenerPorCliente(cliente);
        prepararVistaInventario(model, items, null, null, null, true, "Cliente: " + cliente);
        return "inventario";
    }

    // Filtrar por sala
    @GetMapping("/filtrar/sala/{sala}")
    public String filtrarPorSala(@PathVariable String sala, Model model) {
        List<Inventario> items = inventarioService.obtenerPorSala(sala);
        prepararVistaInventario(model, items, null, sala, null, true, "Sala: " + sala);
        return "inventario";
    }

    // Mostrar formulario importar Excel
    @GetMapping("/importar")
    public String mostrarFormularioImportar() {
        return "inventario-import";
    }

    // Importar desde Excel
    @PostMapping("/importar")
    public String importarExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor selecciona un archivo");
                return "redirect:/inventario/importar";
            }

            // Validar que sea Excel
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "El archivo debe ser Excel (.xlsx o .xls)");
                return "redirect:/inventario/importar";
            }

            // Importar datos del Excel con diagnóstico
            ExcelImportUtil.ImportResult result = ExcelImportUtil.importarExcelConDiagnostico(file);
            List<Inventario> items = result.items;

            if (items.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                    "No se encontraron filas con datos. "
                    + "Filas en hoja: " + result.totalFilasHoja
                    + " | Encabezados detectados: " + result.encabezadosDetectados
                    + " | Columnas mapeadas: " + result.columnasMapeadas);
                return "redirect:/inventario/importar";
            }

            // Importar sin duplicados
            inventarioService.importarLote(items);

            redirectAttributes.addFlashAttribute("success",
                "✓ Guardados " + items.size() + " registros. "
                + "(Hoja tenía " + result.totalFilasHoja + " filas, "
                + result.filasConDatos + " con datos. "
                + "Encabezados: " + (result.encabezadosDetectados ? "detectados automáticamente" : "índices fijos") + ")");
            return "redirect:/inventario";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el archivo: " + e.getMessage());
            return "redirect:/inventario/importar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/inventario/importar";
        }
    }
}
