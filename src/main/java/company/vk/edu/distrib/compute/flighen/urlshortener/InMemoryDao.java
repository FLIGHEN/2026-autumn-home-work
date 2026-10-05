package company.vk.edu.distrib.compute.flighen.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryDao implements Dao<String> {
    private static final String KEY_MUST_NOT_BE_NULL = "Key must not be null";

    private final Map<String, String> db;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public InMemoryDao() {
        db = new HashMap<>();
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }

        readLock.lock();
        try {
            String value = db.get(key);

            if (value == null) {
                throw new NoSuchElementException("db does not contain this key: %s".formatted(key));
            }

            return db.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }

        writeLock.lock();
        try {
            db.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void delete(String key) throws IllegalArgumentException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }

        writeLock.lock();
        try {
            db.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        writeLock.lock();
        try {
            db.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
