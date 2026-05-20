package com.example.dcim.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa una Gestión de Acceso
 * 
 * Gestiona el ciclo completo de tickets de acceso a datacenters y minicenters:
 * - Registro de solicitudes de acceso
 * - Proceso de aprobación por múltiples niveles
 * - Seguimiento del estado de ejecución de actividades
 * - Cierre y documentación de gestiones realizadas
 * 
 * Estados posibles:
 * - Aprobada: Autorizada para ejecución
 * - Pendiente: En espera de aprobación
 * - Rechazada por NXT: No autorizada
 * - Devuelta al cliente por falta de Datos: Requiere más información
 * 
 * @author Arturo Chacón
 */
@Entity
@Table(name = "gestion_acceso")
public class GestionAcceso {

    // ==================== IDENTIFICACIÓN ====================
    
    /** ID único generado automáticamente por la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== DATOS DE REGISTRO ====================
    
    /** Fecha en que se registró la solicitud en el sistema */
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    /** Hora en que se registró la solicitud */
    @Column(name = "hora_registro")
    private LocalTime horaRegistro;

    /** Nombre del usuario que registra la gestión (responsable) */
    @Column(name = "usuario_ingresa")
    private String usuarioIngresa;

    /** Número identificador del ticket (CRQ0012345, INC0008765, etc.) */
    @Column(name = "numero_ticket")
    private String numeroTicket;

    // ==================== PROGRAMACIÓN DE ACTIVIDAD ====================
    
    /** Fecha planificada para iniciar la actividad en el sitio */
    @Column(name = "fecha_inicio_actividad")
    private LocalDate fechaInicioActividad;

    /** Hora planificada de inicio de la actividad */
    @Column(name = "hora_inicio_actividad")
    private LocalTime horaInicioActividad;

    /** Descripción detallada de la actividad a realizar */
    @Column(name = "nombre_actividad", columnDefinition = "TEXT")
    private String nombreActividad;

    /** Fecha planificada para finalizar la actividad */
    @Column(name = "fecha_termino_actividad")
    private LocalDate fechaTerminoActividad;

    /** Hora planificada de término de la actividad */
    @Column(name = "hora_termino_actividad")
    private LocalTime horaTerminoActividad;

    // ==================== PROCESO DE APROBACIÓN ====================
    
    /** Lista de aprobadores que aún deben autorizar (separados por coma) */
    @Column(name = "aprobadores_pendientes", columnDefinition = "TEXT")
    private String aprobadoresPendientes;

    /** Estado de aprobación: Aprobada, Pendiente, Rechazada, Devuelta */
    @Column(name = "estado_aprobacion")
    private String estadoAprobacion; // "Aprobada", "Rechazada por NXT", "Devuelta al cliente por falta de Datos", "Pendiente"

    // ==================== UBICACIÓN Y ESTADO ====================
    
    /** Sitio donde se realizará la actividad (DC SAN MARTIN, MC LA FLORIDA, etc.) */
    @Column(name = "sitio")
    private String sitio; // DC San Martin, DC Apoquindo, MC La Florida, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitio_id")
    private Sitio sitioRef;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sala_id")
    private Sala salaRef;

    /** FK al usuario del sistema que crea/registra la gestión */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_registra_id")
    private Usuario usuarioRegistra;

    /** Indica si la gestión/actividad ya fue realizada físicamente */
    @Column(name = "gestion_realizada")
    private Boolean gestionRealizada;

    /** Indica si el ticket asociado fue cerrado en el sistema */
    @Column(name = "ticket_cerrado")
    private Boolean ticketCerrado;

    // ==================== SEGUIMIENTO Y COMENTARIOS ====================
    
    /** Comentarios iniciales al registrar la gestión */
    @Column(name = "comentario_inicio", columnDefinition = "TEXT")
    private String comentarioInicio;

    /** Comentarios durante la ejecución de la actividad */
    @Column(name = "comentario_intermedio", columnDefinition = "TEXT")
    private String comentarioIntermedio;

    /** Comentarios finales al cerrar la gestión */
    @Column(name = "comentario_final", columnDefinition = "TEXT")
    private String comentarioFinal;

    // ==================== CIERRE DE GESTIÓN ====================
    
    /** Fecha en que se cerró completamente la gestión */
    @Column(name = "fecha_cierre_gestion")
    private LocalDate fechaCierreGestion;

    /** Hora en que se cerró completamente la gestión */
    @Column(name = "hora_cierre_gestion")
    private LocalTime horaCierreGestion;

    // ==================== HORARIOS DE REMEDY ====================
    
    /** Fecha de inicio registrada en Remedy */
    @Column(name = "fecha_inicio_remedy")
    private LocalDate fechaInicioRemedy;

    /** Hora de inicio registrada en Remedy */
    @Column(name = "hora_inicio_remedy")
    private LocalTime horaInicioRemedy;

    /** Fecha de fin registrada en Remedy */
    @Column(name = "fecha_fin_remedy")
    private LocalDate fechaFinRemedy;

    /** Hora de fin registrada en Remedy */
    @Column(name = "hora_fin_remedy")
    private LocalTime horaFinRemedy;

    // ==================== CHECKPOINTS DE PROCESO ====================
    
    /** Indica si la gestión fue enviada a Procesos */
    @Column(name = "enviado_a_procesos")
    private Boolean enviadoAProcesos;

    /** Fecha en que se envió a Procesos */
    @Column(name = "fecha_envio_procesos")
    private LocalDate fechaEnvioProcesos;

    /** Hora en que se envió a Procesos */
    @Column(name = "hora_envio_procesos")
    private LocalTime horaEnvioProcesos;

    /** Indica si se dio respuesta al cliente */
    @Column(name = "respuesta_cliente")
    private Boolean respuestaCliente;

    /** Fecha en que se dio respuesta al cliente */
    @Column(name = "fecha_respuesta_cliente")
    private LocalDate fechaRespuestaCliente;

    /** Hora en que se dio respuesta al cliente */
    @Column(name = "hora_respuesta_cliente")
    private LocalTime horaRespuestaCliente;

    // ==================== CONSTRUCTORES ====================
    
    /** Constructor vacío requerido por JPA */
    public GestionAcceso() {
    }

    /** 
     * Constructor con parámetros principales para crear una gestión
     * Útil para inicializar rápidamente los campos más importantes
     */
    public GestionAcceso(LocalDate fechaRegistro, LocalTime horaRegistro, String usuarioIngresa, 
                        String numeroTicket, LocalDate fechaInicioActividad, LocalTime horaInicioActividad,
                        String nombreActividad, LocalDate fechaTerminoActividad, LocalTime horaTerminoActividad,
                        String aprobadoresPendientes, Boolean gestionRealizada, String estadoAprobacion, String sitio) {
        this.fechaRegistro = fechaRegistro;
        this.horaRegistro = horaRegistro;
        this.usuarioIngresa = usuarioIngresa;
        this.numeroTicket = numeroTicket;
        this.fechaInicioActividad = fechaInicioActividad;
        this.horaInicioActividad = horaInicioActividad;
        this.nombreActividad = nombreActividad;
        this.fechaTerminoActividad = fechaTerminoActividad;
        this.horaTerminoActividad = horaTerminoActividad;
        this.aprobadoresPendientes = aprobadoresPendientes;
        this.gestionRealizada = gestionRealizada;
        this.estadoAprobacion = estadoAprobacion;
        this.sitio = sitio;
    }

    // ==================== GETTERS Y SETTERS ====================
    
    /** Obtiene el ID único de la gestión */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalTime getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(LocalTime horaRegistro) {
        this.horaRegistro = horaRegistro;
    }

    public String getUsuarioIngresa() {
        return usuarioIngresa;
    }

    public void setUsuarioIngresa(String usuarioIngresa) {
        this.usuarioIngresa = usuarioIngresa;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public LocalDate getFechaInicioActividad() {
        return fechaInicioActividad;
    }

    public void setFechaInicioActividad(LocalDate fechaInicioActividad) {
        this.fechaInicioActividad = fechaInicioActividad;
    }

    public LocalTime getHoraInicioActividad() {
        return horaInicioActividad;
    }

    public void setHoraInicioActividad(LocalTime horaInicioActividad) {
        this.horaInicioActividad = horaInicioActividad;
    }

    public String getNombreActividad() {
        return nombreActividad;
    }

    public void setNombreActividad(String nombreActividad) {
        this.nombreActividad = nombreActividad;
    }

    public LocalDate getFechaTerminoActividad() {
        return fechaTerminoActividad;
    }

    public void setFechaTerminoActividad(LocalDate fechaTerminoActividad) {
        this.fechaTerminoActividad = fechaTerminoActividad;
    }

    public LocalTime getHoraTerminoActividad() {
        return horaTerminoActividad;
    }

    public void setHoraTerminoActividad(LocalTime horaTerminoActividad) {
        this.horaTerminoActividad = horaTerminoActividad;
    }

    public String getAprobadoresPendientes() {
        return aprobadoresPendientes;
    }

    public void setAprobadoresPendientes(String aprobadoresPendientes) {
        this.aprobadoresPendientes = aprobadoresPendientes;
    }

    public Boolean getGestionRealizada() {
        return gestionRealizada;
    }

    public void setGestionRealizada(Boolean gestionRealizada) {
        this.gestionRealizada = gestionRealizada;
    }

    public String getEstadoAprobacion() {
        return estadoAprobacion;
    }

    public void setEstadoAprobacion(String estadoAprobacion) {
        this.estadoAprobacion = estadoAprobacion;
    }

    public String getSitio() {
        return sitio;
    }

    public void setSitio(String sitio) {
        this.sitio = sitio;
    }

    public Sitio getSitioRef() {
        return sitioRef;
    }

    public void setSitioRef(Sitio sitioRef) {
        this.sitioRef = sitioRef;
    }

    public Sala getSalaRef() {
        return salaRef;
    }

    public void setSalaRef(Sala salaRef) {
        this.salaRef = salaRef;
    }

    public String getComentarioInicio() {
        return comentarioInicio;
    }

    public void setComentarioInicio(String comentarioInicio) {
        this.comentarioInicio = comentarioInicio;
    }

    public String getComentarioIntermedio() {
        return comentarioIntermedio;
    }

    public void setComentarioIntermedio(String comentarioIntermedio) {
        this.comentarioIntermedio = comentarioIntermedio;
    }

    public String getComentarioFinal() {
        return comentarioFinal;
    }

    public void setComentarioFinal(String comentarioFinal) {
        this.comentarioFinal = comentarioFinal;
    }

    public Boolean getTicketCerrado() {
        return ticketCerrado;
    }

    public void setTicketCerrado(Boolean ticketCerrado) {
        this.ticketCerrado = ticketCerrado;
    }

    public LocalDate getFechaCierreGestion() {
        return fechaCierreGestion;
    }

    public void setFechaCierreGestion(LocalDate fechaCierreGestion) {
        this.fechaCierreGestion = fechaCierreGestion;
    }

    public LocalTime getHoraCierreGestion() {
        return horaCierreGestion;
    }

    public void setHoraCierreGestion(LocalTime horaCierreGestion) {
        this.horaCierreGestion = horaCierreGestion;
    }

    // Horarios de Remedy
    public LocalDate getFechaInicioRemedy() {
        return fechaInicioRemedy;
    }

    public void setFechaInicioRemedy(LocalDate fechaInicioRemedy) {
        this.fechaInicioRemedy = fechaInicioRemedy;
    }

    public LocalTime getHoraInicioRemedy() {
        return horaInicioRemedy;
    }

    public void setHoraInicioRemedy(LocalTime horaInicioRemedy) {
        this.horaInicioRemedy = horaInicioRemedy;
    }

    public LocalDate getFechaFinRemedy() {
        return fechaFinRemedy;
    }

    public void setFechaFinRemedy(LocalDate fechaFinRemedy) {
        this.fechaFinRemedy = fechaFinRemedy;
    }

    public LocalTime getHoraFinRemedy() {
        return horaFinRemedy;
    }

    public void setHoraFinRemedy(LocalTime horaFinRemedy) {
        this.horaFinRemedy = horaFinRemedy;
    }

    // Checkpoints de Proceso
    public Boolean getEnviadoAProcesos() {
        return enviadoAProcesos;
    }

    public void setEnviadoAProcesos(Boolean enviadoAProcesos) {
        this.enviadoAProcesos = enviadoAProcesos;
    }

    public LocalDate getFechaEnvioProcesos() {
        return fechaEnvioProcesos;
    }

    public void setFechaEnvioProcesos(LocalDate fechaEnvioProcesos) {
        this.fechaEnvioProcesos = fechaEnvioProcesos;
    }

    public LocalTime getHoraEnvioProcesos() {
        return horaEnvioProcesos;
    }

    public void setHoraEnvioProcesos(LocalTime horaEnvioProcesos) {
        this.horaEnvioProcesos = horaEnvioProcesos;
    }

    public Boolean getRespuestaCliente() {
        return respuestaCliente;
    }

    public void setRespuestaCliente(Boolean respuestaCliente) {
        this.respuestaCliente = respuestaCliente;
    }

    public LocalDate getFechaRespuestaCliente() {
        return fechaRespuestaCliente;
    }

    public void setFechaRespuestaCliente(LocalDate fechaRespuestaCliente) {
        this.fechaRespuestaCliente = fechaRespuestaCliente;
    }

    public LocalTime getHoraRespuestaCliente() {
        return horaRespuestaCliente;
    }

    public void setHoraRespuestaCliente(LocalTime horaRespuestaCliente) {
        this.horaRespuestaCliente = horaRespuestaCliente;
    }

    public Usuario getUsuarioRegistra() {
        return usuarioRegistra;
    }

    public void setUsuarioRegistra(Usuario usuarioRegistra) {
        this.usuarioRegistra = usuarioRegistra;
    }

    // ==================== MÉTODOS UTILITARIOS ====================
    
    /**
     * Representación en texto de la gestión con sus campos principales
     * Útil para debugging y logging
     */
    @Override
    public String toString() {
        return "GestionAcceso{" +
                "id=" + id +
                ", fechaRegistro=" + fechaRegistro +
                ", horaRegistro=" + horaRegistro +
                ", usuarioIngresa='" + usuarioIngresa + '\'' +
                ", numeroTicket='" + numeroTicket + '\'' +
                ", fechaInicioActividad=" + fechaInicioActividad +
                ", horaInicioActividad=" + horaInicioActividad +
                ", nombreActividad='" + nombreActividad + '\'' +
                ", fechaTerminoActividad=" + fechaTerminoActividad +
                ", horaTerminoActividad=" + horaTerminoActividad +
                ", aprobadoresPendientes='" + aprobadoresPendientes + '\'' +
                ", gestionRealizada=" + gestionRealizada +
                ", estadoAprobacion='" + estadoAprobacion + '\'' +
                ", sitio='" + sitio + '\'' +
                '}';
    }
}
