package org.example.api.controller;

import org.example.api.exceptions.AliasAlreadyExistsException;
import org.example.api.exceptions.UrlNotFoundException;
import org.example.api.model.ShortUrl;
import org.example.api.model.ShortenUrlRequest;
import org.example.api.model.ShortenUrlResponse;
import org.example.api.service.UrlShortenerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UrlShortenerController {

    private static final Logger LOGGER = Logger.getLogger(UrlShortenerController.class.getName());

    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    @POST
    @Path("urls")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shorten(
            ShortenUrlRequest request,
            @Context UriInfo uriInfo
    ) {
        if (request == null) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "O corpo da requisição é obrigatório"
            );
        }

        try {
            ShortUrl created = service.shorten(
                    request.getUrl(),
                    request.getAlias()
            );

            URI shortUri = uriInfo
                    .getBaseUriBuilder()
                    .path(created.getAlias())
                    .build();

            ShortenUrlResponse response =
                    new ShortenUrlResponse(
                            created.getAlias(),
                            created.getTargetUrl(),
                            shortUri.toString(),
                            created.getCreatedAt().toString()
                    );

            return Response
                    .created(shortUri)
                    .entity(response)
                    .build();

        } catch (IllegalArgumentException exception) {
            return error(
                    Response.Status.BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (AliasAlreadyExistsException exception) {
            return error(
                    Response.Status.CONFLICT,
                    exception.getMessage()
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE, "Erro no banco. SQLState=" +
                            exception.getSQLState() + ", errorCode=" +
                            exception.getErrorCode() + ", message=" +
                            exception.getMessage(), exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao acessar o banco de dados"
            );
        }
    }

    @GET
    @Path("{alias}")
    public Response redirect(@PathParam("alias") String alias) {
        try {
            Optional<String> targetUrl =
                    service.findTargetUrl(alias);

            if (!targetUrl.isPresent()) {
                return error(
                        Response.Status.NOT_FOUND,
                        "URL encurtada não encontrada"
                );
            }

            return Response
                    .status(Response.Status.FOUND)
                    .location(URI.create(targetUrl.get()))
                    .build();

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro no banco. SQLState=" + exception.getSQLState() +
                            ", errorCode=" + exception.getErrorCode() +
                            ", message=" + exception.getMessage(),
                    exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao acessar o banco de dados"
            );
        }
    }

    @GET
    @Path("urls")
    public Response findAll(@Context UriInfo uriInfo) {
        try {
            List<ShortUrl> urls = service.findAll();
            List<ShortenUrlResponse> response =
                    new ArrayList<ShortenUrlResponse>();

            for (ShortUrl url : urls) {
                response.add(toResponse(url, uriInfo));
            }

            return Response.ok(response).build();

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro no banco. SQLState=" + exception.getSQLState() +
                            ", errorCode=" + exception.getErrorCode() +
                            ", message=" + exception.getMessage(),
                    exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao consultar o banco de dados"
            );
        }
    }

    @PUT
    @Path("urls/{alias}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("alias") String alias,
            ShortenUrlRequest request,
            @Context UriInfo uriInfo
    ) {
        if (request == null) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "O corpo da requisição é obrigatório"
            );
        }

        try {
            ShortUrl updated = service.update(
                    alias,
                    request.getUrl()
            );

            return Response
                    .ok(toResponse(updated, uriInfo))
                    .build();

        } catch (IllegalArgumentException exception) {

            return error(
                    Response.Status.BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (UrlNotFoundException exception) {
            return error(
                    Response.Status.NOT_FOUND,
                    exception.getMessage()
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro no banco. SQLState=" + exception.getSQLState() +
                            ", errorCode=" + exception.getErrorCode() +
                            ", message=" + exception.getMessage(),
                    exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao acessar o banco de dados"
            );
        }
    }

    @DELETE
    @Path("urls/{alias}")
    public Response delete(
            @PathParam("alias") String alias
    ) {
        try {
            boolean deleted = service.delete(alias);

            if (!deleted) {
                return error(
                        Response.Status.NOT_FOUND,
                        "URL não encontrada"
                );
            }

            return Response.noContent().build();

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro no banco. SQLState=" + exception.getSQLState() +
                            ", errorCode=" + exception.getErrorCode() +
                            ", message=" + exception.getMessage(),
                    exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao acessar o banco de dados"
            );
        }
    }

    @DELETE
    @Path("urls/expired")
    public Response deleteExpired() {
        try {
            int deleted = service.deleteExpired();

            Map<String, Object> response =
                    new LinkedHashMap<String, Object>();

            response.put("deleted", deleted);
            response.put(
                    "expirationMinutes",
                    service.getExpirationMinutes()
            );

            return Response.ok(response).build();

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro no banco. SQLState=" + exception.getSQLState() +
                            ", errorCode=" + exception.getErrorCode() +
                            ", message=" + exception.getMessage(),
                    exception);
            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Erro ao excluir URLs expiradas"
            );
        }
    }

    private ShortenUrlResponse toResponse(
            ShortUrl url,
            UriInfo uriInfo
    ) {
        String shortUrl = uriInfo
                .getBaseUriBuilder()
                .path(url.getAlias())
                .build()
                .toString();

        return new ShortenUrlResponse(
                url.getAlias(),
                url.getTargetUrl(),
                shortUrl,
                url.getCreatedAt().toString()
        );
    }

    private Response error(
            Response.Status status,
            String message
    ) {
        return Response
                .status(status)
                .entity(Collections.singletonMap("error", message))
                .build();
    }

}
