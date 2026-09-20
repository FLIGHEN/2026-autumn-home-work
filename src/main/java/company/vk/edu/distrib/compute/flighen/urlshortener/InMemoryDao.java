package company.vk.edu.distrib.compute.flighen.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class InMemoryDao implements Dao<String> {
    private final Map<String, String> db;

    public InMemoryDao() {
        db = new HashMap<>();
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null");
        }

        if (!db.containsKey(key)) {
            throw new NoSuchElementException("db does not contain this key: %s".formatted(key));
        }

        return db.get(key);
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException {
        if (key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null");
        }

        db.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException {
        if (key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null");
        }

        db.remove(key);
    }

    @Override
    public boolean exists(String key) throws IllegalArgumentException {
        if (key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null");
        }

        return db.containsKey(key);
    }

    @Override
    public void close() throws IOException {
        db.clear();
    }
}
