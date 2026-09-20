package company.vk.edu.distrib.compute.flighen.urlshortener;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class Fl1ghenUrlShortenerService implements UrlShortenerService {
    private final HttpServer server;
    private final Dao<String> shortLinksDao;
    private final Dao<String> usersDao;
    private final AuthFilter authFilter;

    public Fl1ghenUrlShortenerService(int port) throws IOException {
        shortLinksDao = new PersistentDao(Path.of("shortLinks.txt"));
        usersDao = new PersistentDao(Path.of("usersDb.txt"));

        authFilter = new AuthFilter(usersDao);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/v0/status", new GetStatusHandler());
        server.createContext("/", new RedirectHandler(shortLinksDao));
        server.createContext("/internal/users", new UsersHandler(usersDao));
        HttpContext ctxLink = server.createContext("/v0/links", new LinkHandler(shortLinksDao, port));

        ctxLink.getFilters().add(authFilter);
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(1);

        IOException exception = null;

        try {
            shortLinksDao.close();
        } catch (IOException e) {
            exception = e;
        }

        try {
            usersDao.close();
        } catch (IOException e) {
            if (exception == null) {
                exception = e;
            } else {
                exception.addSuppressed(e);
            }
        }

        if (exception != null) {
            throw new UncheckedIOException("Failed to close DAO", exception);
        }
    }
}
