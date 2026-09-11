package com.adquisiciones.recurso;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class HelloRecurso {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String saludo() {
        return "Jersey está funcionando";
    }
}