package Server;

import Protocol.StockProtocol;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StockImpl {
    public Map<String, Integer> stocks;

    public StockImpl() {
        this.stocks = new ConcurrentHashMap<>();
        this.stocks.put("Apple", 0);
        this.stocks.put("Pear", 0);
        this.stocks.put("Banana", 0);
        ScheduledExecutorService service;
        service = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            System.out.println(this.stocks.toString());
        };
        service.scheduleAtFixedRate(task, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public boolean sale (String company, int quantity, String operation) {
        if (operation.equals(StockProtocol.BuyEvent)) {
            quantity=-quantity;
        }
        String company_upper = company.substring(0, 1).toUpperCase() + company.substring(1);
        if(stocks.containsKey(company_upper)) {
            int available = stocks.get(company_upper);
            if (available + quantity >= 0) {
                stocks.put(company_upper, available + quantity);
                return true;
            }
            else {
                return false;
            }
        } else {
            return false;
        }
    }
}
