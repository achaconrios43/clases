package com.example.dcim.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String sala;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sala_id")
    private Sala salaRef;

    @Column(length = 100)
    private String sitio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitio_id")
    private Sitio sitioRef;

    @Column(length = 100)
    private String tipo;

    @Column(length = 100)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @Column(length = 100)
    private String numeroSerie;

    @Column(length = 100)
    private String tag;

    @Column(length = 100)
    private String cliente;

    @Column(length = 255)
    private String coordenadas;

    @Column(length = 100)
    private String nombreRack;

    @Column(length = 100)
    private String ubicacionUr;

    @Column(length = 50)
    private String urUtilizada;

    @Column(name = "capacidad_ur_rack")
    private Integer capacidadUrRack;

    @Column(length = 50)
    private String numeroTemporal;

    @Column(length = 100)
    private String hotname;

    @Column(length = 50)
    private String estado;

    @Column(name = "fecha_alarma")
    private LocalDate fechaAlarma;

    @Column(name = "alarma_hardware")
    private Boolean alarmaHardware;

    @Column(name = "alarma_ventilador")
    private Boolean alarmaVentilador;

    @Column(name = "alarma_fuente_poder")
    private Boolean alarmaFuentePoder;

    @Column(name = "alarma_hdd")
    private Boolean alarmaHdd;

    @Column(columnDefinition = "TEXT")
    private String comentariosAlarma;

    @Column(length = 100)
    private String ticketRelacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(length = 100)
    private String flujoAire;

    @Column(name = "peso_equipo_kg", precision = 8, scale = 2)
    private BigDecimal pesoEquipoKg;

    @Column(length = 100)
    private String fuentesPoder;

    @Column(length = 100)
    private String tiposEnchufe;

    @Column(columnDefinition = "TEXT")
    private String observacionTipoEnchufe;

    @Column(name = "potencia_consumo_watts", precision = 10, scale = 2)
    private BigDecimal potenciaConsumoWatts;

    @Column(name = "direccion_ip", length = 50)
    private String direccionIp;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    // Constructores
    public Inventario() {
    }

    public Inventario(String numeroSerie, String tag, String tipo, String marca, String modelo) {
        this.numeroSerie = numeroSerie;
        this.tag = tag;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public Sala getSalaRef() {
        return salaRef;
    }

    public void setSalaRef(Sala salaRef) {
        this.salaRef = salaRef;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getNombreRack() {
        return nombreRack;
    }

    public void setNombreRack(String nombreRack) {
        this.nombreRack = nombreRack;
    }

    public String getUbicacionUr() {
        return ubicacionUr;
    }

    public void setUbicacionUr(String ubicacionUr) {
        this.ubicacionUr = ubicacionUr;
    }

    public String getUrUtilizada() {
        return urUtilizada;
    }

    public void setUrUtilizada(String urUtilizada) {
        this.urUtilizada = urUtilizada;
    }

    public Integer getCapacidadUrRack() {
        return capacidadUrRack;
    }

    public void setCapacidadUrRack(Integer capacidadUrRack) {
        this.capacidadUrRack = capacidadUrRack;
    }

    public String getNumeroTemporal() {
        return numeroTemporal;
    }

    public void setNumeroTemporal(String numeroTemporal) {
        this.numeroTemporal = numeroTemporal;
    }

    public String getHotname() {
        return hotname;
    }

    public void setHotname(String hotname) {
        this.hotname = hotname;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaAlarma() {
        return fechaAlarma;
    }

    public void setFechaAlarma(LocalDate fechaAlarma) {
        this.fechaAlarma = fechaAlarma;
    }

    public Boolean getAlarmaHardware() {
        return alarmaHardware;
    }

    public void setAlarmaHardware(Boolean alarmaHardware) {
        this.alarmaHardware = alarmaHardware;
    }

    public Boolean getAlarmaVentilador() {
        return alarmaVentilador;
    }

    public void setAlarmaVentilador(Boolean alarmaVentilador) {
        this.alarmaVentilador = alarmaVentilador;
    }

    public Boolean getAlarmaFuentePoder() {
        return alarmaFuentePoder;
    }

    public void setAlarmaFuentePoder(Boolean alarmaFuentePoder) {
        this.alarmaFuentePoder = alarmaFuentePoder;
    }

    public Boolean getAlarmaHdd() {
        return alarmaHdd;
    }

    public void setAlarmaHdd(Boolean alarmaHdd) {
        this.alarmaHdd = alarmaHdd;
    }

    public String getComentariosAlarma() {
        return comentariosAlarma;
    }

    public void setComentariosAlarma(String comentariosAlarma) {
        this.comentariosAlarma = comentariosAlarma;
    }

    public String getTicketRelacion() {
        return ticketRelacion;
    }

    public void setTicketRelacion(String ticketRelacion) {
        this.ticketRelacion = ticketRelacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFlujoAire() {
        return flujoAire;
    }

    public void setFlujoAire(String flujoAire) {
        this.flujoAire = flujoAire;
    }

    public BigDecimal getPesoEquipoKg() {
        return pesoEquipoKg;
    }

    public void setPesoEquipoKg(BigDecimal pesoEquipoKg) {
        this.pesoEquipoKg = pesoEquipoKg;
    }

    public String getFuentesPoder() {
        return fuentesPoder;
    }

    public void setFuentesPoder(String fuentesPoder) {
        this.fuentesPoder = fuentesPoder;
    }

    public String getTiposEnchufe() {
        return tiposEnchufe;
    }

    public void setTiposEnchufe(String tiposEnchufe) {
        this.tiposEnchufe = tiposEnchufe;
    }

    public String getObservacionTipoEnchufe() {
        return observacionTipoEnchufe;
    }

    public void setObservacionTipoEnchufe(String observacionTipoEnchufe) {
        this.observacionTipoEnchufe = observacionTipoEnchufe;
    }

    public BigDecimal getPotenciaConsumoWatts() {
        return potenciaConsumoWatts;
    }

    public void setPotenciaConsumoWatts(BigDecimal potenciaConsumoWatts) {
        this.potenciaConsumoWatts = potenciaConsumoWatts;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}
