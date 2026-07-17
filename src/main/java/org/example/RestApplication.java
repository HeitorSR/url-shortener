package org.example;

import org.example.api.UrlRepository;
import org.example.api.controller.UrlShortenerController;
import org.example.api.service.UrlShortenerService;
import org.example.infra.Database;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class RestApplication extends Application {

    private final Set<Object> singletons;

    private final Integer ExpirationTime = 5;

    public RestApplication() {
        Database.initialize();


        UrlRepository repository =
                new UrlRepository();

        UrlShortenerService service =
                new UrlShortenerService(
                        repository,
                        ExpirationTime
                );

        UrlShortenerController urlController =
                new UrlShortenerController(service);

        Set<Object> resources = new HashSet<Object>();

        resources.add(urlController);

        this.singletons =
                Collections.unmodifiableSet(resources);

        System.out.println(
                "Aplicação inicializada. Expiração: " + ExpirationTime + " horas"
        );
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
