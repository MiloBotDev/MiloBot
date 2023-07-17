package io.github.milobotdev.milobot.api;

import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.process.internal.RequestScoped;

import java.io.IOException;

@Provider
//@Interceptor
@AuthorizedAPIAnnotation
public class AuthorizedAPI implements ContainerRequestFilter {

    //@AroundInvoke
    //public Object intercept(InvocationContext context) throws Exception {
        /*ContainerRequestContext requestContext = (ContainerRequestContext) context.getParameters()[0];

        // Access and manipulate headers in the request
        String headerValue = requestContext.getHeaderString("Header-Name");
        System.out.println(headerValue);
        // Do something with the header value

        // Proceed with the intercepted method
        return context.proceed();*/
    //    return null;
    //}


    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        System.out.println("Hello");
        System.out.println("stillonline");
        System.out.println(containerRequestContext.getProperty("hello"));
        String authorization = containerRequestContext.getHeaderString("Authorization");
        String[] authParts = authorization.split("\\s+");
        if (authParts.length != 2 || !authParts[0].equals("Bearer")) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            String jwt = authParts[1];
            try {
                JWTManager.decryptJWT(jwt);
            } catch (JWTException e) {
                containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }
}
