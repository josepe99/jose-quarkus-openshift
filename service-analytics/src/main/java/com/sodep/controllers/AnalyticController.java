package com.sodep.controllers;

import java.math.BigDecimal;
import java.util.List;

import com.sodep.datasources.TransactionDatasource;
import com.sodep.models.Transaction;
import com.sodep.types.ApiResponse;

import jakarta.ws.rs.Path;

@Path("/api")
public class AnalyticController {
    private final TransactionDatasource transactionDatasource;

    public AnalyticController(TransactionDatasource transactionDatasource) {
        this.transactionDatasource = transactionDatasource;
    }

    @Path("/balance")
    public ApiResponse<BigDecimal> getBalance() {
        List<Transaction> transactions = this.transactionDatasource.listActive();
        BigDecimal incomingTotal = BigDecimal.ZERO;
        BigDecimal outgoingTotal = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction == null || transaction.amount == null || transaction.type == null) {
                continue;
            }

            String type = transaction.type.trim().toUpperCase();
            if ("INCOME".equals(type) || "INCOMING".equals(type)) {
                incomingTotal = incomingTotal.add(transaction.amount);
            } else if ("OUTGOING".equals(type)) {
                outgoingTotal = outgoingTotal.add(transaction.amount);
            }
        }

        BigDecimal balance = incomingTotal.subtract(outgoingTotal);
        return new ApiResponse<>(balance, "Balance calculated successfully");
    }
}

