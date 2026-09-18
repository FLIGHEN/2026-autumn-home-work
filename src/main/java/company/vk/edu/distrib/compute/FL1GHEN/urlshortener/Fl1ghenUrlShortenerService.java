package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Fl1ghenUrlShortenerService implements UrlShortenerService {
    private final HttpServer server;
    private final Dao<String> dao;

    public Fl1ghenUrlShortenerService(int port) throws IOException {
        dao = new Fl1ghenDao();

        server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext ctxGetStatus = server.createContext("/v0/status", new GetStatusHandler());
        HttpContext ctxLink = server.createContext("/v0/links", new LinkHandler(dao, port));
        HttpContext ctxRedirect = server.createContext("/", new RedirectHandler(dao));

    }

    @Override
    public void start() {
        server.start(); // read
    }

    @Override
    public void stop() {
        server.stop(1);
    }
}
