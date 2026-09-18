package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import company.vk.edu.distrib.compute.Dao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;

public class LinkHandler implements HttpHandler {
    public static final int ID_LENGTH = 10;

    private Dao<String> dao;

    private int port;

    public LinkHandler(Dao<String> dao, int port){
        this.dao = dao;
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final var method = exchange.getRequestMethod();

        switch(method){
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
                exchange.sendResponseHeaders(403, 0);
                break;
        }

        exchange.close();
    }

    private void handlePut(HttpExchange exchange) throws IOException{
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length-1];

        if (id == null || !IdGenerator.validate(id)){
            exchange.sendResponseHeaders(422, 0);
            return;
        }

        if(!validateHeaders(exchange)) {
            exchange.sendResponseHeaders(415, 0);
            return;
        }

        try{
            dao.get(id);

            InputStream input = exchange.getRequestBody();

            String newLongLink = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            if(!(newLongLink.contains("https") || newLongLink.contains("http"))){
                exchange.sendResponseHeaders(422, 0);
                input.close();
                return;
            }

            dao.upsert(id, newLongLink);

            exchange.sendResponseHeaders(200, 0);

            input.close();
        }
        catch(NoSuchElementException e){
            exchange.sendResponseHeaders(404, 0);
        }
        catch(IllegalArgumentException | IOException e){
            exchange.sendResponseHeaders(422, 0);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException{
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length-1];

        if (id == null || !IdGenerator.validate(id)){
            exchange.sendResponseHeaders(422, 0);
            return;
        }

        try{
            dao.delete(id);

            exchange.sendResponseHeaders(202, 0);
        }
        catch (IllegalArgumentException e){
            exchange.sendResponseHeaders(422, 0);
        }
    }

    private void handeGet(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/");

        String id = path[path.length-1];

        if (id == null || !IdGenerator.validate(id)){
            System.out.println("Invalid id");
            exchange.sendResponseHeaders(422, 0);
            return;
        }

        try{
            String link = dao.get(id);

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Content-Type", "text/html; charset=utf-8");

            exchange.sendResponseHeaders(200, link.length());

            exchange.getResponseBody().write(link.getBytes(StandardCharsets.UTF_8));
        }
        catch (IllegalArgumentException e){
            exchange.sendResponseHeaders(422, 0);
        }
        catch(NoSuchElementException e){
            exchange.sendResponseHeaders(404, 0);
        }

    }

    private void handlePost(HttpExchange exchange) throws IOException{
        if(!validateHeaders(exchange)) {
            exchange.sendResponseHeaders(415, 0);
            return;
        }
        //main logic

        try (InputStream input = exchange.getRequestBody()) {
            String longUrl = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            if(!(longUrl.contains("https") || longUrl.contains("http"))){
                exchange.sendResponseHeaders(422, 0);
                return;
            }

            String generatedID = IdGenerator.GetId(ID_LENGTH);

            dao.upsert(generatedID, longUrl);

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Content-Type", "text/html; charset=utf-8");

            String responseUrl = "http://localhost:" + port + "/" + generatedID;

            exchange.sendResponseHeaders(201, responseUrl.length());
            exchange.getResponseBody().write(responseUrl.getBytes(StandardCharsets.UTF_8));
        }
        catch(IOException e){
            exchange.sendResponseHeaders(422, 0);
        }
    }

    private boolean validateHeaders(HttpExchange exchange) throws IOException{
        Headers headers = exchange.getRequestHeaders();

        String rawContentType = headers.getFirst("Content-Type");

        if (rawContentType == null) {
            return false;
        }

        String[] parts = rawContentType.split(";");

        String mediaType = parts[0].trim();

        if (!mediaType.equalsIgnoreCase("text/html")) {
            return false;
        }

        String charset = null;

        for (int i = 1; i < parts.length; i++) {
            String[] parameter = parts[i].trim().split("=", 2);

            if (parameter.length == 2 && parameter[0].trim().equalsIgnoreCase("charset")) {
                charset = parameter[1].trim().replace("\"", "");
            }
        }

        if(charset == null){
            return false;
        }

        if (!"utf-8".equalsIgnoreCase(charset)) {
            return false;
        }

        return true;
    }
}
