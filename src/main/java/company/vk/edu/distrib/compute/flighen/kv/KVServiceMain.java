package company.vk.edu.distrib.compute.flighen.kv;

public class KVSeriveMain {
    private KVServiceMain() {
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;

        boolean multithreaded =
                Boolean.parseBoolean(System.getenv("KV_MULTITHREADED"));

        int threads = multithreaded ? 8 : 1;

        FlighenKVService service =
                new FlighenKVService(port, threads);

        service.start();

        Runtime.getRuntime().addShutdownHook(
                new Thread(service::stop)
        );

        Thread.currentThread().join();
    }
}
