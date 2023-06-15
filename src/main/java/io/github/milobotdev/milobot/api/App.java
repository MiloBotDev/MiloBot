package io.github.milobotdev.milobot.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/json")
public class App {

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Person hello() {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        return new Person();
    }

}