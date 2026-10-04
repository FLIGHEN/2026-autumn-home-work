package company.vk.edu.distrib.compute.flighen.kv;

import company.vk.edu.distrib.compute.AbstractHttpServiceFactory;
import company.vk.edu.distrib.compute.kv.KVServiceTest;

import java.io.IOException;

@KVServiceTest
public class FlighenKVServiceFactory extends AbstractHttpServiceFactory<FlighenKVService> {

    @Override
    protected FlighenKVService doCreate(int port) throws IOException {
        return new FlighenKVService(port);
    }
}
