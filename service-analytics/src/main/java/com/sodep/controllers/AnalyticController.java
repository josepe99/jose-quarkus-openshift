package com.sodep.controllers;

import java.math.BigDecimal;
import java.util.List;

import com.sodep.datasources.TransactionDatasource;
import com.sodep.models.Transaction;
import com.sodep.types.ApiResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/api")
public class AnalyticController {
    private static final Logger LOG = Logger.getLogger(AnalyticController.class);
    private final TransactionDatasource transactionDatasource;

    public AnalyticController(TransactionDatasource transactionDatasource) {
        this.transactionDatasource = transactionDatasource;
    }

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResponse<BigDecimal> getBalance() {
        LOG.info("Calculating balance from active transactions");
        List<Transaction> transactions = this.transactionDatasource.listActive();
        LOG.debugf("Fetched %d active transactions", transactions == null ? 0 : transactions.size());
        BigDecimal incomingTotal = BigDecimal.ZERO;
        BigDecimal outgoingTotal = BigDecimal.ZERO;

        if (transactions != null) {
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
        }

        BigDecimal balance = incomingTotal.subtract(outgoingTotal);
        LOG.infof("Balance calculated: %s (incoming=%s, outgoing=%s)", balance, incomingTotal, outgoingTotal);
        return new ApiResponse<>(balance, "Balance calculated successfully");
    }
}
