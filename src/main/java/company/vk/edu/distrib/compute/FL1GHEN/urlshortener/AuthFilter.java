package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Objects;

public class AuthFilter extends Filter {

    public static final String realm = "url-shortener";

    private final Dao<String> usersDao;

    public AuthFilter(Dao<String> dao){
        usersDao = dao;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        Headers requestHeaders = exchange.getRequestHeaders();

        String rawAuth = requestHeaders.getFirst("Authorization");

        if (rawAuth == null) {
            returnUnAuthorized(exchange);
            return;
        }

        rawAuth = rawAuth.strip();

        String[] args = rawAuth.split(" ");

        if(args.length != 2 || !Objects.equals(args[0], "Basic")){
            returnUnAuthorized(exchange);
            return;
        }

        byte[] decoded = Base64.getDecoder().decode(args[1]);

        String userPass = new String(decoded, StandardCharsets.UTF_8);

        if(!userPass.contains(":")){
            returnUnAuthorized(exchange);
            return;
        }

        String[] userInfo = userPass.split(":");

        String login = userInfo[0];
        String password = userInfo[1];

        if(login.isBlank() || password.isBlank()){
            returnUnAuthorized(exchange);
            return;
        }

        try {
            String rightPass = usersDao.get(login);

            if(!Objects.equals(rightPass, password)){
                returnUnAuthorized(exchange);
                return;
            }
        }
        catch (NoSuchElementException | IllegalArgumentException e){
            returnUnAuthorized(exchange);
            return;
        }

        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Basic auth filter";
    }

    private void returnUnAuthorized(HttpExchange exchange) throws IOException{
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("WWW-Authenticate", "Basic realm=\"%s\", charset=\"UTF-8\"".formatted(realm));
        exchange.sendResponseHeaders(401, -1);
    }
}
