package org.example;

import org.example.api.UrlRepository;
import org.example.api.controller.ShortenerController;
import org.example.api.controller.UrlShortenerController;
import org.example.api.service.UrlShortenerService;
import org.example.infra.Database;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public final class Main {

    private static final String BASE_URI =
            System.getProperty("server.url", "http://0.0.0.0:8080/");

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Database.initialize();

        UrlRepository repository = new UrlRepository();

        UrlShortenerService service =
                new UrlShortenerService(
                        repository,
                        10
                );

        UrlShortenerController controller =
                new UrlShortenerController(service);

        ResourceConfig config = new ResourceConfig()
                .register(ShortenerController.class)
                .register(controller)
                .register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://0.0.0.0:8080/"),
                config
        );

        Runtime.getRuntime().addShutdownHook(
                new Thread(server::shutdownNow)
        );

        System.out.println("Aplicação iniciada.");
        System.out.println("Pressione Enter para encerrar.");

        System.in.read();
        server.shutdownNow();
    }
}