package com.example.dcim.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLADOR TEMPORAL - Eliminar después de hacer seed en producción
 * Endpoint: POST /admin/seed?token=dcim-seed-2026
 */
@RestController
@RequestMapping("/api/admin")
public class SeedController {

    private static final String SEED_TOKEN = "dcim-seed-2026";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam String token) {
        if (!SEED_TOKEN.equals(token)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        List<String> results = new ArrayList<>();

        try {
            // Limpiar datos existentes
            jdbcTemplate.execute("TRUNCATE TABLE sitio CASCADE");
            results.add("OK: TRUNCATE sitio CASCADE");

            jdbcTemplate.execute("TRUNCATE TABLE usuario CASCADE");
            results.add("OK: TRUNCATE usuario CASCADE");

            // SITIO
            jdbcTemplate.execute(
                "INSERT INTO sitio (id, activo, descripcion, fecha_creacion, fecha_modificacion, nombre) VALUES " +
                "(2, true, 'Data Center',  '2026-05-13 04:32:28.113825', '2026-05-13 04:32:28.113825', 'DC San Martin')," +
                "(3, true, '',              '2026-05-13 23:10:47.621609', '2026-05-13 23:10:47.621609', 'DC Apoquindo')"
            );
            results.add("OK: INSERT sitio (2 rows)");

            // SALA
            jdbcTemplate.execute(
                "INSERT INTO sala (id, activo, descripcion, fecha_creacion, fecha_modificacion, nombre, sitio_id, tipo) VALUES " +
                "(2, true, '', '2026-05-13 05:05:15.737882', '2026-05-13 05:05:15.737882', 'CPD',       2, 'Sala TI')," +
                "(3, true, '', '2026-05-13 23:11:30.405338', '2026-05-13 23:11:30.405338', 'Mainframe', 3, 'Sala TI')"
            );
            results.add("OK: INSERT sala (2 rows)");

            // PUNTO_MEDICION
            jdbcTemplate.execute(
                "INSERT INTO punto_medicion (id, activo, codigo, nombre, temperatura_maxima, temperatura_minima, sala_id, fecha_creacion, fecha_modificacion) VALUES " +
                "(4, true, '01', 'r4', 34.00, 17.00, 2, '2026-05-14 04:00:00', '2026-05-14 04:00:00')," +
                "(5, true, '02', 'j6', 34.00, 17.00, 2, '2026-05-14 04:00:00', '2026-05-14 04:00:00')"
            );
            results.add("OK: INSERT punto_medicion (2 rows)");

            // USUARIO
            jdbcTemplate.execute(
                "INSERT INTO usuario (id, apellido, creat_at, email, nombre, password, rol, rut, update_at, modulos_permitidos) VALUES " +
                "(4, 'Chacón Ríos', '2026-05-01 19:25:57.000000', 'achaconrios@gmail.com', 'Arturo', " +
                "'$2a$10$0DfDlXv6hrXU.4MDUa7Reu6j36ISK8QzgRPDtazOmOBE6eKfDyZWq', " +
                "'ADMIN', '15.441.473-8', '2026-05-01 19:25:57.000000', '')"
            );
            results.add("OK: INSERT usuario (1 row)");

            // INGRESOAP
            jdbcTemplate.execute(
                "INSERT INTO ingresoap (" +
                "  id, actividad_remedy, activo, aprobador, cargo_tecnico, coordenadas_gps," +
                "  empresa_contratista, empresa_demandante, escolta," +
                "  fecha_fin_ficticia, fecha_inicio, fecha_registro," +
                "  fecha_segunda_supervision, fecha_supervision_media, fecha_termino," +
                "  foto_tecnico, guia_despacho," +
                "  hora_fin_ficticia, hora_inicio, hora_segunda_supervision, hora_supervision_media, hora_termino," +
                "  motivo_ingreso, nombre_tecnico, nombre_usuario, numero_ticket," +
                "  rack_ingresa, rut_tecnico, sala_ingresa, sala_remedy," +
                "  segunda_supervision_realizada, sitio_ingreso, tipo_ticket, turno," +
                "  sala_id, sitio_id, usuario_registra_id, aprobador_id" +
                ") VALUES " +
                "(8, 'Revision Operativa', false, 'Operador Turno', 'Tecnico en Redes', NULL," +
                " 'Zener', 'Telefonica', 'Operador de Turno'," +
                " NULL, '2026-05-13', '2026-05-13 06:56:13.431000'," +
                " '2026-05-13', NULL, '2026-05-13'," +
                " NULL, ''," +
                " NULL, '01:05:00', '02:50:00', NULL, '02:56:00'," +
                " 'Inspectiva', 'judtih linco', 'Arturo Chacón Ríos', 'Visita Inspectiva'," +
                " 'rack 4J', '18.052.030-9', 'CPD', 'Salas TI'," +
                " true, 'DC San Martin', 'Visita Inspectiva', 'AM'," +
                " NULL, NULL, NULL, NULL)," +
                "(9, 'revision de temepraturas', false, 'Arturo Chacón', 'Técnico', NULL," +
                " 'inelcom', 'telefonca', 'Guardia'," +
                " NULL, '2026-05-13', '2026-05-13 20:34:51.347000'," +
                " '2026-05-13', '2026-05-13', '2026-05-13'," +
                " NULL, ''," +
                " NULL, '03:00:00', '16:34:00', '04:00:00', '16:34:00'," +
                " 'Actividad Rutinaria', 'judith linco', 'Arturo Chacón Ríos', 'N/A'," +
                " 'rj', '180520309', 'CPD', 'Salas TI'," +
                " true, NULL, 'Visita Inspectiva', 'AM'," +
                " NULL, NULL, NULL, NULL)"
            );
            results.add("OK: INSERT ingresoap (2 rows)");

            // INVENTARIO
            jdbcTemplate.execute(
                "INSERT INTO inventario (" +
                "  id, sala, sitio, tipo, marca, modelo, numero_serie, tag, cliente, coordenadas," +
                "  nombre_rack, ubicacion_ur, ur_utilizada, capacidad_ur_rack," +
                "  numero_temporal, hotname, estado," +
                "  fecha_alarma, alarma_hardware, alarma_ventilador, alarma_fuente_poder, alarma_hdd," +
                "  comentarios_alarma, ticket_relacion, observaciones, flujo_aire," +
                "  peso_equipo_kg, fuentes_poder, tipos_enchufe, observacion_tipo_enchufe," +
                "  potencia_consumo_watts, direccion_ip," +
                "  fecha_creacion, fecha_modificacion," +
                "  sala_id, sitio_id" +
                ") VALUES " +
                "(1,'CPD','DC San Martin','RACK','IBM','9306-420','23-A2791','TE107525','TELEFONICA','S4'," +
                " '9','','',42,'','','Operativo'," +
                " NULL,NULL,NULL,NULL,NULL,'','','',''," +
                " NULL,'','','',16280.00,''," +
                " '2026-05-14 04:46:15','2026-05-14 04:46:15',2,2)," +
                "(2,'CPD','DC San Martin','UR OCUPADA','N/A','N/A','N/A','N/A','N/A','S4'," +
                " '9','42','1',NULL,'N/A','N/A','UR Ocupada'," +
                " NULL,NULL,NULL,NULL,NULL,'','','UR OCUPADA NO SE PUEDEN INSTALAR EQUIPOS PORQUE PASAN CABLES EN SU INTERIOR','N/A'," +
                " NULL,'N/A','N/A','N/A',NULL,'N/A'," +
                " '2026-05-14 04:50:34','2026-05-14 04:50:34',2,2)," +
                "(3,'CPD','DC San Martin','PATCH PANNEL','PANDUIT','','0','0','','S4'," +
                " '9','41','1',NULL,'','','Operativo'," +
                " NULL,NULL,NULL,NULL,NULL,'','','',''," +
                " 2.00,'','','',NULL,''," +
                " '2026-05-14 04:55:28','2026-05-14 04:55:28',2,2)"
            );
            results.add("OK: INSERT inventario (3 rows)");

            // Resetear secuencias
            jdbcTemplate.execute("SELECT setval('punto_medicion_id_seq', COALESCE((SELECT MAX(id) FROM punto_medicion), 1))");
            jdbcTemplate.execute("SELECT setval('sitio_id_seq',         COALESCE((SELECT MAX(id) FROM sitio), 1))");
            jdbcTemplate.execute("SELECT setval('sala_id_seq',          COALESCE((SELECT MAX(id) FROM sala), 1))");
            jdbcTemplate.execute("SELECT setval('usuario_id_seq',       COALESCE((SELECT MAX(id) FROM usuario), 1))");
            jdbcTemplate.execute("SELECT setval('gestion_acceso_id_seq', 8)");
            results.add("OK: Sequences reset");

            return ResponseEntity.ok("SEED COMPLETADO:\n" + String.join("\n", results));

        } catch (Exception e) {
            results.add("ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body("SEED FALLIDO:\n" + String.join("\n", results) + "\n\nEXCEPTION: " + e.getMessage());
        }
    }
}
