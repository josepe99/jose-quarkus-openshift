package com.sodep.datasources;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class BankDatasource {
    private static final String VERSION_HEADER_VALUE = "V1.0";
    private static final String BEARER_PREFIX = "Bearer ";

    @RestClient
    MiddlewareApiClient middlewareApiClient;

    @ConfigProperty(name = "interfisa.jwt.token")
    String jwtToken;

    public Response fetchCentrosServicios(String nombreODireccion) {
        return middlewareApiClient.getCentrosServicios(VERSION_HEADER_VALUE, nombreODireccion);
    }

    public Response fetchParametrosSipap(String dominio) {
        String authHeader = BEARER_PREFIX + jwtToken;
        return middlewareApiClient.getParametrosSipap(VERSION_HEADER_VALUE, authHeader, dominio);
    }
}

@RegisterRestClient(configKey = "middleware-api")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
interface MiddlewareApiClient {
    @GET
    @Path("/common/centros-servicios")
    Response getCentrosServicios(
            @HeaderParam("X-INTERFISA-BE-VERSION") String version,
            @QueryParam("nombreODireccion") String nombreODireccion);

    @GET
    @Path("/secure/common/parametros")
    Response getParametrosSipap(
            @HeaderParam("X-INTERFISA-BE-VERSION") String version,
            @HeaderParam("Authorization") String authorization,
            @QueryParam("dominio") String dominio);
}
