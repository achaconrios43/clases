-- ============================================================
-- CLON EXACTO DE MySQL → PostgreSQL (estado: 14 mayo 2026)
-- Fuente: mysqldump en vivo desde localhost:3307/dcimdb
-- ============================================================

BEGIN;

-- ---- Limpiar TODO (CASCADE elimina dependientes automáticamente) ----
TRUNCATE TABLE sitio CASCADE;
TRUNCATE TABLE usuario CASCADE;

-- ---- SITIO (2 registros actuales en MySQL) ----
INSERT INTO sitio (id, activo, descripcion, fecha_creacion, fecha_modificacion, nombre) VALUES
(2, true, 'Data Center',  '2026-05-13 04:32:28.113825', '2026-05-13 04:32:28.113825', 'DC San Martin'),
(3, true, '',              '2026-05-13 23:10:47.621609', '2026-05-13 23:10:47.621609', 'DC Apoquindo');

-- ---- SALA (2 registros actuales en MySQL) ----
INSERT INTO sala (id, activo, descripcion, fecha_creacion, fecha_modificacion, nombre, sitio_id, tipo) VALUES
(2, true, '', '2026-05-13 05:05:15.737882', '2026-05-13 05:05:15.737882', 'CPD',      2, 'Sala TI'),
(3, true, '', '2026-05-13 23:11:30.405338', '2026-05-13 23:11:30.405338', 'Mainframe', 3, 'Sala TI');

-- ---- PUNTO_MEDICION (2 puntos activos para sala CPD, sala_id=2) ----
INSERT INTO punto_medicion (id, activo, codigo, nombre, temperatura_maxima, temperatura_minima, sala_id, fecha_creacion, fecha_modificacion) VALUES
(4, true, '01', 'r4', 34.00, 17.00, 2, '2026-05-14 04:00:00', '2026-05-14 04:00:00'),
(5, true, '02', 'j6', 34.00, 17.00, 2, '2026-05-14 04:00:00', '2026-05-14 04:00:00');

-- ---- USUARIO (1 registro – los otros fueron eliminados en MySQL) ----
INSERT INTO usuario (id, apellido, creat_at, email, nombre, password, rol, rut, update_at, modulos_permitidos) VALUES
(4, 'Chacón Ríos', '2026-05-01 19:25:57.000000', 'achaconrios@gmail.com', 'Arturo',
 '$2a$10$0DfDlXv6hrXU.4MDUa7Reu6j36ISK8QzgRPDtazOmOBE6eKfDyZWq',
 'ADMIN', '15.441.473-8', '2026-05-01 19:25:57.000000', '');

-- ---- GESTION_ACCESO: vacía en MySQL actual (0 filas) ----

-- ---- INGRESOAP (2 registros actuales en MySQL, ids 8 y 9) ----
INSERT INTO ingresoap (
  id, actividad_remedy, activo, aprobador, cargo_tecnico, coordenadas_gps,
  empresa_contratista, empresa_demandante, escolta,
  fecha_fin_ficticia, fecha_inicio, fecha_registro,
  fecha_segunda_supervision, fecha_supervision_media, fecha_termino,
  foto_tecnico, guia_despacho,
  hora_fin_ficticia, hora_inicio, hora_segunda_supervision, hora_supervision_media, hora_termino,
  motivo_ingreso, nombre_tecnico, nombre_usuario, numero_ticket,
  rack_ingresa, rut_tecnico, sala_ingresa, sala_remedy,
  segunda_supervision_realizada, sitio_ingreso, tipo_ticket, turno,
  sala_id, sitio_id, usuario_registra_id, aprobador_id
) VALUES
(8,
 'Revision Operativa',
 false,
 'Operador Turno', 'Tecnico en Redes', NULL,
 'Zener', 'Telefonica', 'Operador de Turno',
 NULL, '2026-05-13', '2026-05-13 06:56:13.431000',
 '2026-05-13', NULL, '2026-05-13',
 NULL, '',
 NULL, '01:05:00', '02:50:00', NULL, '02:56:00',
 'Inspectiva', 'judtih linco', 'Arturo Chacón Ríos', 'Visita Inspectiva',
 'rack 4J', '18.052.030-9', 'CPD', 'Salas TI',
 true, 'DC San Martin', 'Visita Inspectiva', 'AM',
 NULL, NULL, NULL, NULL),
(9,
 'revision de temepraturas',
 false,
 'Arturo Chacón', 'Técnico', NULL,
 'inelcom', 'telefonca', 'Guardia',
 NULL, '2026-05-13', '2026-05-13 20:34:51.347000',
 '2026-05-13', '2026-05-13', '2026-05-13',
 NULL, '',
 NULL, '03:00:00', '16:34:00', '04:00:00', '16:34:00',
 'Actividad Rutinaria', 'judith linco', 'Arturo Chacón Ríos', 'N/A',
 'rj', '180520309', 'CPD', 'Salas TI',
 true, NULL, 'Visita Inspectiva', 'AM',
 NULL, NULL, NULL, NULL);

-- ---- INVENTARIO (3 registros creados el 14 mayo 2026 en MySQL) ----
INSERT INTO inventario (
  id, sala, sitio, tipo, marca, modelo, numero_serie, tag, cliente, coordenadas,
  nombre_rack, ubicacion_ur, ur_utilizada, capacidad_ur_rack,
  numero_temporal, hotname, estado,
  fecha_alarma, alarma_hardware, alarma_ventilador, alarma_fuente_poder, alarma_hdd,
  comentarios_alarma, ticket_relacion, observaciones, flujo_aire,
  peso_equipo_kg, fuentes_poder, tipos_enchufe, observacion_tipo_enchufe,
  potencia_consumo_watts, direccion_ip,
  fecha_creacion, fecha_modificacion,
  sala_id, sitio_id
) VALUES
(1,
 'CPD', 'DC San Martin', 'RACK', 'IBM', '9306-420', '23-A2791', 'TE107525', 'TELEFONICA', 'S4',
 '9', '', '', 42,
 '', '', 'Operativo',
 NULL, NULL, NULL, NULL, NULL,
 '', '', '', '',
 NULL, '', '', '', 16280.00, '',
 '2026-05-14 04:46:15', '2026-05-14 04:46:15',
 2, 2),
(2,
 'CPD', 'DC San Martin', 'UR OCUPADA', 'N/A', 'N/A', 'N/A', 'N/A', 'N/A', 'S4',
 '9', '42', '1', NULL,
 'N/A', 'N/A', 'UR Ocupada',
 NULL, NULL, NULL, NULL, NULL,
 '', '', 'UR OCUPADA NO SE PUEDEN INSTALAR EQUIPOS PORQUE PASAN CABLES EN SU INTERIOR', 'N/A',
 NULL, 'N/A', 'N/A', 'N/A', NULL, 'N/A',
 '2026-05-14 04:50:34', '2026-05-14 04:50:34',
 2, 2),
(3,
 'CPD', 'DC San Martin', 'PATCH PANNEL', 'PANDUIT', '', '0', '0', '', 'S4',
 '9', '41', '1', NULL,
 '', '', 'Operativo',
 NULL, NULL, NULL, NULL, NULL,
 '', '', '', '',
 2.00, '', '', '', NULL, '',
 '2026-05-14 04:55:28', '2026-05-14 04:55:28',
 2, 2);

-- ---- Resetear secuencias al valor correcto ----
SELECT setval('punto_medicion_id_seq', COALESCE((SELECT MAX(id) FROM punto_medicion), 1));
SELECT setval('sitio_id_seq',         COALESCE((SELECT MAX(id) FROM sitio), 1));
SELECT setval('sala_id_seq',          COALESCE((SELECT MAX(id) FROM sala), 1));
SELECT setval('usuario_id_seq',       COALESCE((SELECT MAX(id) FROM usuario), 1));
SELECT setval('gestion_acceso_id_seq', 8);   -- MySQL AUTO_INCREMENT=9
SELECT setval('ingresoap_id_seq',     COALESCE((SELECT MAX(id) FROM ingresoap), 1));
SELECT setval('inventario_id_seq',    COALESCE((SELECT MAX(id) FROM inventario), 1));

COMMIT;
