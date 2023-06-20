package io.github.milobotdev.milobot.api;


import jakarta.inject.Singleton;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

public class DependencyInjectionService implements PostConstruct {
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
    }
}
