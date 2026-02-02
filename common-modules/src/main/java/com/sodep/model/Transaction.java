package com.sodep.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entidad Transaction que utiliza el patrón Active Record de Panache.
 * Al extender de PanacheEntity, Quarkus genera automáticamente el ID 
 * y los métodos de persistencia (persist, listAll, find, etc.).
 */
@Entity
@Table(name = "transactions") // Nombre de la tabla en la base de datos compartida
public class Transaction extends PanacheEntity {

    @Column(nullable = false)
    public BigDecimal amount;

    @Column(nullable = false)
    public String type; // Sugerido: "INCOME" o "OUTGOING"

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    public String description;

    // Puedes agregar métodos de utilidad aquí si los necesitas
}