package company.vk.edu.distrib.compute.flighen.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class RedirectHandler implements HttpHandler {
    private final Dao<String> dao;

    public RedirectHandler(Dao<String> dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final var method = exchange.getRequestMethod();

        if (Objects.equals(method, "GET")) {

            String[] path = exchange.getRequestURI().getPath().split("/");

            String id = path[path.length - 1];

            if (id == null || !IdUtils.isValidId(id)) {
                exchange.sendResponseHeaders(422, -1);
                exchange.close();
                return;
            }

            try {
                String longLink = dao.get(id);

                exchange.getResponseHeaders()
                        .add("Location", longLink);

                exchange.sendResponseHeaders(301, -1);
            } catch (NoSuchElementException e) {
                exchange.sendResponseHeaders(404, -1);
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(422, -1);
            }
        } else {
            exchange.sendResponseHeaders(403, -1);
        }

        exchange.close();
    }
}
