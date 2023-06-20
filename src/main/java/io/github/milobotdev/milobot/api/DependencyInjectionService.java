package io.github.milobotdev.milobot.api;


import jakarta.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

public class DependencyInjectionService {
    public String getstg() {
        return "stg";
    }

    private int num = 0;
    public void num() {
        System.out.println(num++);
    }
}
