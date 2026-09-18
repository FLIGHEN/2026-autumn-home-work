package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpConnectTimeoutException;

public class Fl1ghenUrlShortenerService implements UrlShortenerService {
    private final HttpServer server;
    private final Dao<String> shortLinksDao;
    private final Dao<String> usersDao;

    public Fl1ghenUrlShortenerService(int port) throws IOException {
        shortLinksDao = new InMemoryDao();

        usersDao = new InMemoryDao();

        AuthFilter authFilter = new AuthFilter(usersDao);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext ctxGetStatus = server.createContext("/v0/status", new GetStatusHandler());
        HttpContext ctxLink = server.createContext("/v0/links", new LinkHandler(shortLinksDao, port));
        HttpContext ctxRedirect = server.createContext("/", new RedirectHandler(shortLinksDao));
        HttpContext ctxAppendUser = server.createContext("/internal/users", new UsersHandler(usersDao));

        ctxLink.getFilters().add(authFilter);
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
