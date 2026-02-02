package com.sodep.services;

import java.util.List;

import com.sodep.datasources.TransactionDatasource;
import com.sodep.models.Transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionDatasource datasource;

    public List<Transaction> findAllActive() {
        return datasource.listActive();
    }

    public Transaction findActiveById(Long id) {
        return datasource.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with ID: " + id));
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        // Business Logic Example: Ensure amount isn't zero
        if (transaction.amount == null || transaction.amount.doubleValue() == 0) {
            throw new IllegalArgumentException("Transaction amount must be non-zero");
        }
        datasource.persist(transaction);
        return transaction;
    }

    @Transactional
    public Transaction update(Long id, Transaction transactionData) {
        Transaction existing = findActiveById(id);

        existing.amount = transactionData.amount;
        existing.type = transactionData.type;
        existing.description = transactionData.description;
        existing.date = transactionData.date;

        return existing;
    }

    @Transactional
    public void delete(Long id) {
        boolean wasDeleted = datasource.softDelete(id);
        if (!wasDeleted) {
            throw new NotFoundException("Cannot delete: Transaction not found");
        }
    }
}
