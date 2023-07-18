package io.github.milobotdev.milobot.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.HttpContext;
import org.glassfish.jersey.internal.util.Property;

@Path("/json")
@AuthorizedAPIAnnotation
public class App {

    //@Inject
    //AuthorizedAPI authorizedAPI;

    @Inject
    private DependencyInjectionService depserv;

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context ContainerRequestContext context) {
        /*context.getRequest();
        AccessJwtData data = (AccessJwtData) context.getProperty("accessJwtData");
        return data.accessToken();*/
        return depserv.getAccessJwtData().refreshToken();
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        //depserv.getstg();
        //depserv.num();
        /*SessionData data = new SessionData(33);
        Gson gson = new Gson();
        //gson.toJson(data);
        System.out.println(gson.toJson(data));
        Gson gson2 = new Gson();
        SessionData data2 = gson2.fromJson(gson.toJson(data), SessionData.class);
        System.out.println(data2.userId());*/
        //return new Person();
    }

    @GET
    @Path("/getjwt")
    public Response getjwt(@QueryParam("data") String data) {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        depserv.num();
        System.out.println(data);
        try {
            return Response.ok().entity(JWTManager.generateJWT(data)).build();
        } catch (JWTException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/processjwt")
    public Response processjwt(@QueryParam("jwt") String jwt) {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        depserv.num();
        System.out.println(jwt);
        try {
            return Response.ok().entity(JWTManager.decryptJWT(jwt)).build();
        } catch (JWTException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}