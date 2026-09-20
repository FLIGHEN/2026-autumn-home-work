package company.vk.edu.distrib.compute.flighen.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistentDao implements Dao<String> {
    private static final String KEYMUSTNOTBENULL = "Key must not be null";

    private final Path filePath;

    private final Map<String, String> db;

    public PersistentDao(Path filePath) throws IOException {
        this.filePath = filePath;
        db = new HashMap<>();

        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        for (String line : lines) {
            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                db.put(parts[0], parts[1]);
            }
        }
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new IllegalArgumentException(KEYMUSTNOTBENULL);
        }

        if (!db.containsKey(key)) {
            throw new NoSuchElementException("db does not contain this key: %s".formatted(key));
        }

        return db.get(key);
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new IllegalArgumentException(KEYMUSTNOTBENULL);
        }

        db.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new IllegalArgumentException(KEYMUSTNOTBENULL);
        }

        db.remove(key);
    }

    @Override
    public boolean exists(String key) throws IllegalArgumentException {
        if (key.isBlank()) {
            throw new IllegalArgumentException(KEYMUSTNOTBENULL);
        }

        return db.containsKey(key);
    }

    @Override
    public void close() throws IOException {
        List<String> lines = new ArrayList<>();

        for (Map.Entry<String, String> entry : db.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }

        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}
