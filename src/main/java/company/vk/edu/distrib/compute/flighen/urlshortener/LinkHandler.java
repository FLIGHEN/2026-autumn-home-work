package company.vk.edu.distrib.compute.flighen.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import company.vk.edu.distrib.compute.Dao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;

public class LinkHandler implements HttpHandler {
    public static final int ID_LENGTH = 10;

    private final Dao<String> dao;

    private final int port;

    public LinkHandler(Dao<String> dao, int port) {
        this.dao = dao;
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final var method = exchange.getRequestMethod();

        switch (method) {
            case "GET":
                handeGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
            default:
                exchange.sendResponseHeaders(403, -1);
                break;
        }

        exchange.close();
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length - 1];

        if (id == null || !IdUtils.isValidId(id)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        if (!validateHeaders(exchange)) {
            exchange.sendResponseHeaders(415, -1);
            return;
        }

        if (!dao.exists(id)) {
            exchange.sendResponseHeaders(404, -1);
        }

        InputStream input = exchange.getRequestBody();
        String newLongLink = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        input.close();

        if (!(newLongLink.contains("https") || newLongLink.contains("http"))) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        try {
            dao.upsert(id, newLongLink);

            exchange.sendResponseHeaders(200, -1);
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(422, -1);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length - 1];

        if (id == null || !IdUtils.isValidId(id)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        try {
            dao.delete(id);

            exchange.sendResponseHeaders(202, -1);
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(422, -1);
        }
    }

    private void handeGet(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length - 1];

        if (id == null || !IdUtils.isValidId(id)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        try {
            String link = dao.get(id);

            exchange.getResponseHeaders()
                    .add("Content-Type", "text/html; charset=utf-8");

            exchange.sendResponseHeaders(200, link.length());

            exchange.getResponseBody().write(link.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(422, -1);
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!validateHeaders(exchange)) {
            exchange.sendResponseHeaders(415, -1);
            return;
        }

        InputStream input = exchange.getRequestBody();
        String longUrl = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        if (!(longUrl.contains("https") || longUrl.contains("http"))) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        input.close();

        String generatedID = IdUtils.getId(ID_LENGTH);

        try {
            dao.upsert(generatedID, longUrl);
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }

        exchange.getResponseHeaders()
                .add("Content-Type", "text/html; charset=utf-8");

        String responseUrl = "http://localhost:" + port + "/" + generatedID;

        exchange.sendResponseHeaders(201, responseUrl.length());
        exchange.getResponseBody().write(responseUrl.getBytes(StandardCharsets.UTF_8));
    }

    private boolean validateHeaders(HttpExchange exchange) throws IOException {
        String rawContentType = exchange.getRequestHeaders().getFirst("Content-Type");

        if (rawContentType == null) {
            return false;
        }

        String[] parts = rawContentType.split(";");

        String mediaType = parts[0].trim();

        if (!"text/html".equalsIgnoreCase(mediaType)) {
            return false;
        }

        String charset = "";

        for (int i = 1; i < parts.length; i++) {
            String[] parameter = parts[i].trim().split("=", 2);

            if (parameter.length == 2 && "charset".equalsIgnoreCase(parameter[0].trim())) {
                charset = parameter[1].trim().replace("\"", "");
            }
        }

        return "utf-8".equalsIgnoreCase(charset);
    }
}
