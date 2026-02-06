package com.sodep.datasources;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(BankDatasource.class);
    private static final String VERSION_HEADER_VALUE = "V1.0";
    private static final String BEARER_PREFIX = "Bearer ";

    @RestClient
    MiddlewareApiClient middlewareApiClient;

    @ConfigProperty(name = "JWT_TOKEN")
    String jwtToken;

    @ConfigProperty(name = "quarkus.rest-client.\"middleware-api\".url")
    String middlewareUrl;

    public Response fetchCentrosServicios(String nombreODireccion) {
        return middlewareApiClient.getCentrosServicios(VERSION_HEADER_VALUE, nombreODireccion);
    }

    public Response fetchParametrosSipap(String dominio, String authorizationHeader) {
        String authHeader = buildAuthorizationHeader(authorizationHeader);
        LOG.infof("Authorization (masked)=%s", maskAuthorization(authHeader));
        LOG.infof("curl=%s", buildCurl(authHeader));
        if (authHeader == null) {
            LOG.warn(
                    "No Authorization header and JWT_TOKEN is empty; calling middleware without auth.");
        } else {
            LOG.debugf("Calling middleware with %s auth header.",
                    authHeader.startsWith(BEARER_PREFIX) ? "Bearer" : "custom");
        }
        return middlewareApiClient.getParametrosSipap(VERSION_HEADER_VALUE, authHeader, dominio);
    }

    private String buildAuthorizationHeader(String authorizationHeader) {
        String trimmed = authorizationHeader == null ? null : authorizationHeader.trim();
        if (trimmed != null && !trimmed.isEmpty()) {
            if (trimmed.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                return trimmed;
            }
            // If caller sent a raw JWT, wrap it as Bearer.
            if (trimmed.chars().filter(ch -> ch == '.').count() == 2) {
                return BEARER_PREFIX + trimmed;
            }
            return trimmed;
        }
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        return BEARER_PREFIX + jwtToken.trim();
    }

    private String maskAuthorization(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "<empty>";
        }
        String token = stripBearer(authHeader);
        int len = token.length();
        return "len=" + len + " " + maskToken(token);
    }

    private String buildCurl(String authHeader) {
        String base = middlewareUrl == null ? "" : middlewareUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String fullUrl = base + "/api/secure/common/parametros?dominio=motivos-sipap";
        StringBuilder curl = new StringBuilder();
        curl.append("curl -i -X GET '").append(fullUrl).append("'")
                .append(" -H 'X-INTERFISA-BE-VERSION: ").append(VERSION_HEADER_VALUE).append("'");
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            curl.append(" -H 'Authorization: ").append(maskAuthorizationHeader(authHeader))
                    .append("'");
        }
        return curl.toString();
    }

    private String maskAuthorizationHeader(String authHeader) {
        String token = stripBearer(authHeader);
        if (token.isEmpty()) {
            return "Bearer ****";
        }
        return BEARER_PREFIX + maskToken(token);
    }

    private String stripBearer(String authHeader) {
        String token = authHeader == null ? "" : authHeader.trim();
        if (token.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }
        return token;
    }

    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "****";
        }
        int len = token.length();
        if (len <= 8) {
            return "****";
        }
        String last4 = token.substring(len - 4);
        return "****" + last4;
    }
}


@RegisterRestClient(configKey = "middleware-api")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
interface MiddlewareApiClient {
    @GET
    @Path("/common/centros-servicios")
    Response getCentrosServicios(@HeaderParam("X-INTERFISA-BE-VERSION") String version,
            @QueryParam("nombreODireccion") String nombreODireccion);

    @GET
    @Path("/secure/common/parametros")
    Response getParametrosSipap(@HeaderParam("X-INTERFISA-BE-VERSION") String version,
            @HeaderParam("Authorization") String authorization,
            @QueryParam("dominio") String dominio);
}
