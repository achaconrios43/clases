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
@Table(name = "plano_sala_elemento")
public class PlanoSalaElemento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plano_sala_id", nullable = false)
    private PlanoSala planoSala;

    @Column(length = 50, nullable = false)
    private String tipo;

    @Column(length = 100)
    private String nombre;

    /** Columna inicial 1-based (A=1, B=2...). */
    @Column(name = "columna_inicio", nullable = false)
    private int columnaInicio;

    /** Fila inicial 1-based (1 es la fila inferior/sur). */
    @Column(name = "fila_inicio", nullable = false)
    private int filaInicio;

    @Column(name = "ancho_cm", nullable = false)
    private int anchoCm;

    @Column(name = "largo_cm", nullable = false)
    private int largoCm;

    @Column(name = "rotado_90", nullable = false)
    private boolean rotado90;

    @Column(length = 20)
    private String color;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanoSala getPlanoSala() {
        return planoSala;
    }

    public void setPlanoSala(PlanoSala planoSala) {
        this.planoSala = planoSala;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getColumnaInicio() {
        return columnaInicio;
    }

    public void setColumnaInicio(int columnaInicio) {
        this.columnaInicio = columnaInicio;
    }

    public int getFilaInicio() {
        return filaInicio;
    }

    public void setFilaInicio(int filaInicio) {
        this.filaInicio = filaInicio;
    }

    public int getAnchoCm() {
        return anchoCm;
    }

    public void setAnchoCm(int anchoCm) {
        this.anchoCm = anchoCm;
    }

    public int getLargoCm() {
        return largoCm;
    }

    public void setLargoCm(int largoCm) {
        this.largoCm = largoCm;
    }

    public boolean isRotado90() {
        return rotado90;
    }

    public void setRotado90(boolean rotado90) {
        this.rotado90 = rotado90;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
