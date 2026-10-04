package company.vk.edu.distrib.compute.flighen.kv;

import company.vk.edu.distrib.compute.Dao;
import org.jspecify.annotations.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BytesDao implements Dao<byte[]> {
    private final Path filePath;

    private final Map<String, byte[]> db;

    public BytesDao(Path filePath) throws IOException {
        this.filePath = filePath;
        db = new HashMap<>();

        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        readFile();
    }

    @Override
    public byte[] get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new EmptyKeyException();
        }

        if (!db.containsKey(key)) {
            throw new NoSuchElementException("db does not contain this key: %s".formatted(key));
        }

        return db.get(key);
    }

    @Override
    public void upsert(String key, byte @NonNull [] value) throws IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new EmptyKeyException();
        }

        db.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        if (key.isBlank()) {
            throw new EmptyKeyException();
        }

        db.remove(key);
    }

    @Override
    public void close() throws IOException {
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(filePath))) {
            for (Map.Entry<String, byte[]> entry : db.entrySet()) {
                byte[] keyBytes = entry.getKey()
                        .getBytes(StandardCharsets.UTF_8);

                byte[] value = entry.getValue();

                out.writeInt(keyBytes.length);
                out.write(keyBytes);

                out.writeInt(value.length);
                out.write(value);
            }
        }
    }

    public void readFile() throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(filePath))) {
            while (in.available() > 0) {
                int keyLength = in.readInt();
                byte[] keyBytes = in.readNBytes(keyLength);

                int valueLength = in.readInt();
                byte[] value = in.readNBytes(valueLength);

                String key = new String(
                        keyBytes,
                        StandardCharsets.UTF_8
                );

                db.put(key, value);
            }
        }
    }
}
