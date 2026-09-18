package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class RedirectHandler implements HttpHandler {
    private Dao<String> dao;

    public RedirectHandler(Dao<String> dao){
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final var method = exchange.getRequestMethod();

        if(Objects.equals(method, "GET")){

            String[] path = exchange.getRequestURI().getPath().split("/");

            String id = path[path.length-1];

            if (id == null || !IdGenerator.validate(id)){
                exchange.sendResponseHeaders(422, 0);
                exchange.close();
                return;
            }

            try{
                String longLink = dao.get(id);

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.add("Location", longLink);

                exchange.sendResponseHeaders(301, 0);
            }
            catch(NoSuchElementException e){
                exchange.sendResponseHeaders(404, 0);
            }
            catch(IllegalArgumentException e){
                exchange.sendResponseHeaders(422, 0);
            }
        }
        else{
            exchange.sendResponseHeaders(403, 0);
        }

        exchange.close();
    }
}
