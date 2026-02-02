package com.sodep.service;

import com.sodep.datasource.TransactionDatasource;
import com.sodep.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped // Hace que esta clase sea un Bean de CDI manejado por Quarkus
public class TransactionService {

    @Inject
    TransactionDatasource datasource; // Tu librería común

    public List<Transaction> listAllTransactions() {
        // Aquí podrías agregar filtros o validaciones en el futuro
        return datasource.findAll();
    }
}
