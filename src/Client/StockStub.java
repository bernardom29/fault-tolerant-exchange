package Client;

import Common.CommDaemon;
import Protocol.Operation;
import Protocol.OperationResponse;
import Protocol.ProtocolMessage;
import Protocol.StockProtocol;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StockStub implements StockInterface {
    private final Serializer serializer;
    private final CommDaemon<ProtocolMessage> commDaemon;
    private final int id;
    private final int port;
    private AtomicInteger localOpId;
    private StockUpdateReceive stockUpdate;

    public StockStub(int id, int port) throws IOException {
        this.serializer = StockProtocol.newSerializer();
        this.id = id;
        this.port = port;
        this.commDaemon = new CommDaemon<>("Client" + id, "StockClient", "StockServer", this.serializer, this.port);
        this.localOpId = new AtomicInteger(0);
        this.stockUpdate = new StockUpdateReceive(commDaemon);
        new Thread(this.stockUpdate).start();
    }

    @Override
    public CompletableFuture<Boolean> buy(String company, int quantity) {
        return operation(company, quantity, StockProtocol.BuyEvent);
    }

    @Override
    public CompletableFuture<Boolean> sell(String company, int quantity) {
        return operation(company, quantity, StockProtocol.SellEvent);
    }

    private CompletableFuture<Boolean> operation(String company, int quantity, String event) {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        int idOp = localOpId.getAndIncrement();
        Operation operation = new Operation(idOp, event,  this.id, this.port, company, quantity);
        try {
            this.commDaemon.multicast(operation, "StockServer");
            this.stockUpdate.setCompletableFuture(idOp,cf);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        return cf;
    }

}

class StockUpdateReceive implements Runnable {
    private final CommDaemon<ProtocolMessage> commDaemon;
    private Map<Integer,CompletableFuture<Boolean>> cfs;
    private Map<Integer,Integer> reps;
    private Map<Integer,Integer> acks;
    private Map<Integer,Boolean> resp;
    private Map<Integer, Long> timeout;

    public StockUpdateReceive(CommDaemon<ProtocolMessage> commDaemon) {
        this.commDaemon = commDaemon;
        this.cfs = new ConcurrentHashMap<>();
        this.reps = new ConcurrentHashMap<>();
        this.acks = new ConcurrentHashMap<>();
        this.resp = new ConcurrentHashMap<>();
        this.timeout = new ConcurrentHashMap<>();
        Runnable runnableTask = () -> {
            for (Map.Entry<Integer, CompletableFuture<Boolean>> entry : this.cfs.entrySet()) {
                int id = entry.getKey();
                long time = this.timeout.get(id);
                long currentTime = new Date().getTime();
                if (time < currentTime) {
                    entry.getValue().complete(this.resp.getOrDefault(id, false));
                    cfs.remove(id);
                    reps.remove(id);
                    acks.remove(id);
                    resp.remove(id);
                    timeout.remove(id);
                }
            }
        };

        ScheduledExecutorService executorService = Executors
                .newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(runnableTask, 500, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        while (true) {
            OperationResponse response = null;
            try {
                this.commDaemon.receive();
                response = (OperationResponse) this.commDaemon.delivery();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InterruptedIOException e) {
                e.printStackTrace();
            } catch (SpreadException e) {
                e.printStackTrace();
            }
            if (response == null)
                continue;
            int actual_reps = response.getMembers();
            int id = response.getLocalId();
            // TODO Decidir valor do factor
            double factor = 0.5;
            if(cfs.containsKey(id)) {
                int old_reps = reps.get(id);
                if (old_reps==-1) {
                    reps.put(id,actual_reps);
                    resp.put(id,response.isSuccess());
                }
                else if (old_reps>actual_reps) {
                    reps.put(id,actual_reps);
                }
                if(resp.get(id)==response.isSuccess()) {
                    acks.put(id,acks.get(id)+1);
                    if(acks.get(id)>= reps.get(id)*factor) {
                        cfs.get(id).complete(response.isSuccess());
                        cfs.remove(id);
                        reps.remove(id);
                        acks.remove(id);
                        resp.remove(id);
                        timeout.remove(id);
                    }
                }
                else {
                    System.out.println("Estado inconsistente!");
                }
            }
            else {
                System.out.println("Ack Received from Old Request " + id);
            }
        }
    }

    public void setCompletableFuture(int id, CompletableFuture<Boolean> cf) {
        cfs.put(id,cf);
        reps.put(id,-1);
        acks.put(id,0);
        timeout.put(id, new Date().getTime() + 5000);
    }
}