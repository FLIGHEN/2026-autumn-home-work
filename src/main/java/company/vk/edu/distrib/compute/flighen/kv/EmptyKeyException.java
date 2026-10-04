package company.vk.edu.distrib.compute.flighen.kv;

public class EmptyKeyException extends IllegalArgumentException {
    public EmptyKeyException() {
        super("Key must not be blank");
    }
}
