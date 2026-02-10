package com.sodep.controllers;

import com.sodep.datasources.BankDatasource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class BankController {
    @Inject
    BankDatasource bankDatasource;

    @GET
    @Path("/common/centros-servicios")
    public Response getCentrosServicios(@QueryParam("nombreODireccion") String nombreODireccion) {
        return bankDatasource.fetchCentrosServicios(nombreODireccion);
    }

    @GET
    @Path("/secure/common/parametros")
    public Response getParametrosSipap(@QueryParam("dominio") String dominio,
            @HeaderParam("Authorization") String authorization) {
        return bankDatasource.fetchParametrosSipap(dominio, authorization);
    }
}
