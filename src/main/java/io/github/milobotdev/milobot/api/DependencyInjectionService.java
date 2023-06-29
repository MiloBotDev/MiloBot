package io.github.milobotdev.milobot.api;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;

public class DependencyInjectionService implements PostConstruct {
    @Context
    private HttpHeaders httpHeaders;

    @Context
    ContainerRequestContext cc;

    public String getstg() {
        return "stg";
    }

    private int num = 0;
    public void num() {
        System.out.println(num++);
    }

    @Override
    public void postConstruct() {
        System.out.println("Helloagain");
        System.out.println(httpHeaders.getHeaderString("Authorization"));

        cc.setProperty("hello", "world");
    }
}
