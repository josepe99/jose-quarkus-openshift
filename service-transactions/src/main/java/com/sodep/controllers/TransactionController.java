package com.sodep.controllers;

import java.util.List;
import com.sodep.models.Transaction;
import com.sodep.services.TransactionService;

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
    public List<Transaction> getAll() {
        return service.findAllActive();
    }

    @GET
    @Path("/{id}")
    public Transaction getOne(@PathParam("id") Long id) {
        return service.findActiveById(id);
    }

    @POST
    public Response save(Transaction transaction) {
        Transaction created = service.create(transaction);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Transaction update(@PathParam("id") Long id, Transaction transaction) {
        return service.update(id, transaction);
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
