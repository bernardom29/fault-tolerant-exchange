package Client;

import java.util.concurrent.CompletableFuture;

public interface StockInterface {
    CompletableFuture<Boolean> buy(String company, int quantity);
    CompletableFuture<Boolean> sell(String company, int quantity);
}
