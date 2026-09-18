package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Fl1ghenDao implements Dao<String> {
    private Map<String, String> linksDb;

    public Fl1ghenDao(){
        linksDb = new HashMap<>();
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if(key.isBlank()){
            throw new IllegalArgumentException("Key must not be null");
        }

        if(!linksDb.containsKey(key))
            throw new NoSuchElementException("linksDb does not contain this key: %s".formatted(key));

        return linksDb.get(key);
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        if(key.isBlank()){
            throw new IllegalArgumentException("Key must not be null");
        }

        linksDb.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        if(key.isBlank()){
            throw new IllegalArgumentException("Key must not be null");
        }

        linksDb.remove(key);
    }

    @Override
    public void close() throws IOException {
        linksDb.clear();
    }
}
