package com.sodep.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction extends PanacheEntity {

    @Column(nullable = false)
    public BigDecimal amount;

    @Column(nullable = false)
    public String type; // "INCOME" o "OUTGOING"

    @Column(length = 255)
    public String description;

    // The transaction business date
    @Column(nullable = false)
    public LocalDateTime date;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deleted_at;

    // --- Lifecycle Hooks ---

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Utility Method for Soft Delete ---
    public void softDelete() {
        this.deleted_at = LocalDateTime.now();
    }
}
