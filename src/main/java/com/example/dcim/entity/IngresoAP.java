package com.example.dcim.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "ingresoap")
public class IngresoAP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("turno")
    @Column(nullable = false, length = 10)
    private String turno;

    @JsonProperty("nombre_usuario")
    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @JsonProperty("fecha_inicio")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @JsonProperty("hora_inicio")
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @JsonProperty("fecha_termino")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_termino")
    private LocalDate fechaTermino;

    @JsonProperty("hora_termino")
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "hora_termino")
    private LocalTime horaTermino;

    @JsonProperty("fecha_fin_ficticia")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_fin_ficticia")
    private LocalDate fechaFinFicticia;

    @JsonProperty("hora_fin_ficticia")
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "hora_fin_ficticia")
    private LocalTime horaFinFicticia;

    @JsonProperty("hora_supervision_media")
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "hora_supervision_media")
    private LocalTime horaSupervisionMedia;

    @JsonProperty("fecha_supervision_media")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_supervision_media")
    private LocalDate fechaSupervisionMedia;

    @JsonProperty("segunda_supervision_realizada")
    @Column(name = "segunda_supervision_realizada")
    private Boolean segundaSupervisionRealizada = false;

    @JsonProperty("fecha_segunda_supervision")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_segunda_supervision")
    private LocalDate fechaSegundaSupervision;

    @JsonProperty("hora_segunda_supervision")
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "hora_segunda_supervision")
    private LocalTime horaSegundaSupervision;

    @JsonProperty("nombre_tecnico")
    @Column(name = "nombre_tecnico", nullable = false, length = 100)
    private String nombreTecnico;

    @JsonProperty("rut_tecnico")
    @Column(name = "rut_tecnico", nullable = false, length = 12)
    private String rutTecnico;

    @JsonProperty("empresa_demandante")
    @Column(name = "empresa_demandante", nullable = false, length = 100)
    private String empresaDemandante;

    @JsonProperty("empresa_contratista")
    @Column(name = "empresa_contratista", nullable = false, length = 100)
    private String empresaContratista;

    @JsonProperty("cargo_tecnico")
    @Column(name = "cargo_tecnico", nullable = false, length = 100)
    private String cargoTecnico;

    @JsonProperty("sala_remedy")
    @Column(name = "sala_remedy", nullable = false, length = 50)
    private String salaRemedy;

    @JsonProperty("tipo_ticket")
    @Column(name = "tipo_ticket", nullable = false, length = 30)
    private String tipoTicket;

    @JsonProperty("numero_ticket")
    @Column(name = "numero_ticket", nullable = false, length = 50)
    private String numeroTicket;

    @JsonProperty("aprobador")
    @Column(name = "aprobador", nullable = false, length = 100)
    private String aprobador;

    @JsonProperty("escolta")
    @Column(name = "escolta", nullable = false, length = 50)
    private String escolta;

    @JsonProperty("motivo_ingreso")
    @Column(name = "motivo_ingreso", nullable = false, length = 100)
    private String motivoIngreso;

    @JsonProperty("guia_despacho")
    @Column(name = "guia_despacho", length = 50)
    private String guiaDespacho;

    @JsonProperty("sitio_ingreso")
    @Column(name = "sitio_ingreso", length = 100)
    private String sitioIngreso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitio_id")
    private Sitio sitioRef;

    @JsonProperty("sala_ingresa")
    @Column(name = "sala_ingresa", nullable = false, columnDefinition = "TEXT")
    private String salaIngresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sala_id")
    private Sala salaRef;

    @JsonProperty("rack_ingresa")
    @Column(name = "rack_ingresa", length = 50)
    private String rackIngresa;

    @JsonProperty("actividad_remedy")
    @Column(name = "actividad_remedy", nullable = false, columnDefinition = "TEXT")
    private String actividadRemedy;

    @JsonProperty("fecha_registro")
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    @JsonProperty("activo")
    @Column(nullable = false)
    private Boolean activo = true;

    @JsonProperty("foto_tecnico")
    @Column(name = "foto_tecnico", columnDefinition = "TEXT")
    private String fotoTecnico;

    @JsonProperty("coordenadas_gps")
    @Column(name = "coordenadas_gps", columnDefinition = "TEXT")
    private String coordenadasGps;

    /** FK al usuario del sistema que registra el ingreso (puede ser null para externos) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_registra_id")
    private Usuario usuarioRegistra;

    /** FK al usuario del sistema que aprueba el ingreso (puede ser null para externos) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aprobador_id")
    private Usuario aprobadorRef;

    // Constructor vacío
    public IngresoAP() {
        this.fechaRegistro = new Date();
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalDate getFechaTermino() { return fechaTermino; }
    public void setFechaTermino(LocalDate fechaTermino) { this.fechaTermino = fechaTermino; }

    public LocalTime getHoraTermino() { return horaTermino; }
    public void setHoraTermino(LocalTime horaTermino) { this.horaTermino = horaTermino; }

    public LocalDate getFechaFinFicticia() { return fechaFinFicticia; }
    public void setFechaFinFicticia(LocalDate fechaFinFicticia) { this.fechaFinFicticia = fechaFinFicticia; }

    public LocalTime getHoraFinFicticia() { return horaFinFicticia; }
    public void setHoraFinFicticia(LocalTime horaFinFicticia) { this.horaFinFicticia = horaFinFicticia; }

    public LocalTime getHoraSupervisionMedia() { return horaSupervisionMedia; }
    public void setHoraSupervisionMedia(LocalTime horaSupervisionMedia) { this.horaSupervisionMedia = horaSupervisionMedia; }

    public LocalDate getFechaSupervisionMedia() { return fechaSupervisionMedia; }
    public void setFechaSupervisionMedia(LocalDate fechaSupervisionMedia) { this.fechaSupervisionMedia = fechaSupervisionMedia; }

    public Boolean getSegundaSupervisionRealizada() { return segundaSupervisionRealizada; }
    public void setSegundaSupervisionRealizada(Boolean segundaSupervisionRealizada) { this.segundaSupervisionRealizada = segundaSupervisionRealizada; }

    public LocalDate getFechaSegundaSupervision() { return fechaSegundaSupervision; }
    public void setFechaSegundaSupervision(LocalDate fechaSegundaSupervision) { this.fechaSegundaSupervision = fechaSegundaSupervision; }

    public LocalTime getHoraSegundaSupervision() { return horaSegundaSupervision; }
    public void setHoraSegundaSupervision(LocalTime horaSegundaSupervision) { this.horaSegundaSupervision = horaSegundaSupervision; }

    public String getNombreTecnico() { return nombreTecnico; }
    public void setNombreTecnico(String nombreTecnico) { this.nombreTecnico = nombreTecnico; }

    public String getRutTecnico() { return rutTecnico; }
    public void setRutTecnico(String rutTecnico) { this.rutTecnico = rutTecnico; }

    public String getEmpresaDemandante() { return empresaDemandante; }
    public void setEmpresaDemandante(String empresaDemandante) { this.empresaDemandante = empresaDemandante; }

    public String getEmpresaContratista() { return empresaContratista; }
    public void setEmpresaContratista(String empresaContratista) { this.empresaContratista = empresaContratista; }

    public String getCargoTecnico() { return cargoTecnico; }
    public void setCargoTecnico(String cargoTecnico) { this.cargoTecnico = cargoTecnico; }

    public String getSalaRemedy() { return salaRemedy; }
    public void setSalaRemedy(String salaRemedy) { this.salaRemedy = salaRemedy; }

    public String getTipoTicket() { return tipoTicket; }
    public void setTipoTicket(String tipoTicket) { this.tipoTicket = tipoTicket; }

    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }

    public String getAprobador() { return aprobador; }
    public void setAprobador(String aprobador) { this.aprobador = aprobador; }

    public String getEscolta() { return escolta; }
    public void setEscolta(String escolta) { this.escolta = escolta; }

    public String getMotivoIngreso() { return motivoIngreso; }
    public void setMotivoIngreso(String motivoIngreso) { this.motivoIngreso = motivoIngreso; }

    public String getGuiaDespacho() { return guiaDespacho; }
    public void setGuiaDespacho(String guiaDespacho) { this.guiaDespacho = guiaDespacho; }

    public String getSitioIngreso() { return sitioIngreso; }
    public void setSitioIngreso(String sitioIngreso) { this.sitioIngreso = sitioIngreso; }

    public Sitio getSitioRef() { return sitioRef; }
    public void setSitioRef(Sitio sitioRef) { this.sitioRef = sitioRef; }

    public String getSalaIngresa() { return salaIngresa; }
    public void setSalaIngresa(String salaIngresa) { this.salaIngresa = salaIngresa; }

    public Sala getSalaRef() { return salaRef; }
    public void setSalaRef(Sala salaRef) { this.salaRef = salaRef; }

    public String getRackIngresa() { return rackIngresa; }
    public void setRackIngresa(String rackIngresa) { this.rackIngresa = rackIngresa; }

    public String getActividadRemedy() { return actividadRemedy; }
    public void setActividadRemedy(String actividadRemedy) { this.actividadRemedy = actividadRemedy; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getFotoTecnico() { return fotoTecnico; }
    public void setFotoTecnico(String fotoTecnico) { this.fotoTecnico = fotoTecnico; }

    public String getCoordenadasGps() { return coordenadasGps; }
    public void setCoordenadasGps(String coordenadasGps) { this.coordenadasGps = coordenadasGps; }

    public Usuario getUsuarioRegistra() { return usuarioRegistra; }
    public void setUsuarioRegistra(Usuario usuarioRegistra) { this.usuarioRegistra = usuarioRegistra; }

    public Usuario getAprobadorRef() { return aprobadorRef; }
    public void setAprobadorRef(Usuario aprobadorRef) { this.aprobadorRef = aprobadorRef; }
}
