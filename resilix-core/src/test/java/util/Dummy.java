package util;

import java.util.concurrent.CompletableFuture;

public class Dummy {
    @DummyAnn
    public String syncMethod() {
        return "ok";
    }

    public CompletableFuture<String> asyncMethod() {
        return CompletableFuture.completedFuture("ok");
    }
}