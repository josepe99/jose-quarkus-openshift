package com.sodep.controllers;

import java.util.List;

import com.sodep.models.Transaction;
import com.sodep.services.TransactionService;
import com.sodep.types.ApiResponse;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService service;

    @GET
    public ApiResponse<List<Transaction>> getAll() {
        List<Transaction> transactions = service.findAllActive();
        return new ApiResponse<>(transactions, "success");
    }

    @GET
    @Path("/{id}")
    public ApiResponse<Transaction> getOne(@PathParam("id") Long id) {
        Transaction transaction = service.findActiveById(id);
        return new ApiResponse<>(transaction, "success");
    }

    @POST
    public Response save(Transaction transaction) {
        Transaction created = service.create(transaction);
        ApiResponse<Transaction> response = new ApiResponse<>(created, "success");
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<Transaction> update(@PathParam("id") Long id, Transaction transaction) {
        Transaction updated = service.update(id, transaction);
        return new ApiResponse<>(updated, "success");
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
