package company.vk.edu.distrib.compute.flighen.urlshortener;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Objects;

public class AuthFilter extends Filter {

    public static final String REALM = "url-shortener";

    private final Dao<String> usersDao;

    public AuthFilter(Dao<String> dao) {
        super();
        usersDao = dao;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if (isAuthorized(exchange)) {
            chain.doFilter(exchange);
        } else {
            returnUnAuthorized(exchange);
        }
    }

    @Override
    public String description() {
        return "Basic auth filter";
    }

    private boolean isAuthorized(HttpExchange exchange) throws IOException {
        String rawAuth = exchange.getRequestHeaders().getFirst("Authorization");

        if (rawAuth == null) {
            return false;
        }

        String[] userInfo = parseCredentials(rawAuth);

        if (userInfo == null) {
            return false;
        }

        String login = userInfo[0];
        String password = userInfo[1];

        if (login.isBlank() || password.isBlank()) {
            return false;
        }

        try {
            String rightPass = usersDao.get(login);

            if (!Objects.equals(rightPass, password)) {
                return false;
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    private String[] parseCredentials(String rawAuth) {
        String strippedAuth = rawAuth.strip();

        String[] args = strippedAuth.split(" ");

        if (args.length != 2 || !Objects.equals(args[0], "Basic")) {
            return null;
        }

        byte[] decoded = Base64.getDecoder().decode(args[1]);

        String userPass = new String(decoded, StandardCharsets.UTF_8);

        if (!userPass.contains(":")) {
            return null;
        }

        return userPass.split(":");
    }

    private void returnUnAuthorized(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders()
                .add("WWW-Authenticate", "Basic REALM=\"%s\", charset=\"UTF-8\"".formatted(REALM));
        exchange.sendResponseHeaders(401, -1);
    }
}
