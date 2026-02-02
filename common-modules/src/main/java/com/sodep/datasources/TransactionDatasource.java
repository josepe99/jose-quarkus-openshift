package com.sodep.datasources;

import java.util.List;
import java.util.Optional;

import com.sodep.models.Transaction;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionDatasource implements PanacheRepository<Transaction> {

    /**
     * Finds all transactions that have not been soft-deleted.
     */
    public List<Transaction> listActive() {
        return list("deleted_at is null");
    }

    /**
     * Finds a single active transaction by ID.
     */
    public Optional<Transaction> findActiveById(Long id) {
        return find("id = ?1 and deleted_at is null", id).firstResultOptional();
    }

    /**
     * Logic for soft deleting a transaction.
     */
    public boolean softDelete(Long id) {
        return findActiveById(id).map(transaction -> {
            transaction.softDelete(); // Uses the method in your Transaction model
            return true;
        }).orElse(false);
    }
}
