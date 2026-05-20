package com.example.dcim.entity;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "plano_sala")
public class PlanoSala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    /** Cantidad de columnas (Oeste→Este), cada columna = 60 cm. Etiquetas A, B, C... */
    @Column(name = "cantidad_columnas", nullable = false)
    private int cantidadColumnas;

    /** Cantidad de filas (Norte→Sur), cada fila = 60 cm. Etiquetas 1, 2, 3... */
    @Column(name = "cantidad_filas", nullable = false)
    private int cantidadFilas;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "es_plantilla", nullable = false)
    private boolean esPlantilla = false;

    @Column(name = "nombre_plantilla", length = 100)
    private String nombrePlantilla;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    // Constructors
    public PlanoSala() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }

    public int getCantidadColumnas() { return cantidadColumnas; }
    public void setCantidadColumnas(int cantidadColumnas) { this.cantidadColumnas = cantidadColumnas; }

    public int getCantidadFilas() { return cantidadFilas; }
    public void setCantidadFilas(int cantidadFilas) { this.cantidadFilas = cantidadFilas; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isEsPlantilla() { return esPlantilla; }
    public void setEsPlantilla(boolean esPlantilla) { this.esPlantilla = esPlantilla; }

    public String getNombrePlantilla() { return nombrePlantilla; }
    public void setNombrePlantilla(String nombrePlantilla) { this.nombrePlantilla = nombrePlantilla; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
