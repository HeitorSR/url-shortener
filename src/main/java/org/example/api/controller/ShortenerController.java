package org.example.api.controller;

import org.example.api.model.ShortenUrlRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class ShortenerController {

    @GET
    public Response health() {
        return Response.ok("healthy").build();
    }

    @GET
    @Path("/urls")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shorten(
            ShortenUrlRequest request,
            @Context UriInfo uriInfo
    ) {
        return Response.ok("asasdf").build();
    }
}

