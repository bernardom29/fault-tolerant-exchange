package Server;

import Common.CommDaemon;
import Protocol.OperationResponse;
import Protocol.*;
import spread.SpreadException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Protocol.StockProtocol.*;

public class StockServer {
    private String id;
    private int port;
    private StockImpl stock;
    private CommDaemon<ProtocolMessage> commDaemon;
    public Vector<ProtocolMessage> log;
    public AtomicInteger members;

    public StockServer(String id, int port) throws IOException {
        this.id = id;
        this.log = new Vector<>();
        this.commDaemon = new CommDaemon<>(
                "Server" + id,
                "StockServer",
                "StockClient",
                StockProtocol.newSerializer(),
                port
        );
        this.port = port;
        this.stock = new StockImpl();
        this.members = new AtomicInteger(0);
    }

    public static void main(String[] args) {
        StockServer ss = null;
        try {
            ss = new StockServer(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ss.setup();
    }

    public Vector<ProtocolMessage> joinGroup() {
        JoinRequest joinRequest = new JoinRequest(JoinRequestEvent, this.port);
        Vector<ProtocolMessage> log = new Vector<>();
        int segment = 0;
        try {
            this.commDaemon.multicast(joinRequest, "StockServer");
            JoinResponse joinResponse;
            while (true) {
                ProtocolMessage message = this.commDaemon.deliveryRep();
                if (message instanceof JoinResponse && message != null) {
                    joinResponse = (JoinResponse) message;
                    boolean end = joinResponse.end;
                    if (((JoinResponse) message).chunk == -1) {
                        return null;
                    }
                    if (segment == ((JoinResponse) message).chunk) {
                        segment = ((JoinResponse) message).chunk + 1000;
                        log.addAll(joinResponse.replicaLog);
                        if (joinResponse.end) {
                            System.out.println(log.size());
                            return log;
                        }
                    }

                }
            }
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processJoin(ProtocolMessage message) {
        int size = this.log.size();
        Vector<ProtocolMessage> pm;
        pm = new Vector<>();
        boolean end = false;
        if (size == 0) {
            JoinResponse joinResponse = new JoinResponse(JoinResponseEvent, null, true, -1);
            try {
                this.commDaemon.multicast(joinResponse, message.getSender());
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < size; i += 1000) {
            if (i + 1000 < size) {
                pm.addAll(this.log.subList(i, i + 1000));
            } else {
                pm.addAll(this.log.subList(i, size));
                end = true;
            }
            JoinResponse joinResponse = new JoinResponse(JoinResponseEvent, pm, end, i);
            try {
                this.commDaemon.multicast(joinResponse, message.getSender());
            } catch (SpreadException e) {
                e.printStackTrace();
            }
            pm.clear();
        }
    }

    private boolean processSale(ProtocolMessage message) {
        Operation op = (Operation) message;
        boolean result = this.stock.sale(op.getCompany(), op.getQuantity(), op.getEvent());
        OperationResponse oprep = new OperationResponse(op.getLocalId(), StockProtocol.OperationResponse, op.getUser(), op.getPort(), result, this.members.intValue());
        try {
            this.commDaemon.multicast(oprep, message.getSender());
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean processMessage(ProtocolMessage message) {
        boolean result;
        switch (message.getEvent()) {
            case BuyEvent:
                result = processSale(message);
                break;
            case SellEvent:
                result = processSale(message);
                break;
            case JoinRequestEvent:
                processJoin(message);
                result = true;
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    private void processor() {
        for (ProtocolMessage item : this.log) {
            if (item.getEvent().equals(BuyEvent) || item.getEvent().equals(SellEvent)) {
                Operation op = (Operation) item;
                this.stock.sale(op.getCompany(), op.getQuantity(), op.getEvent());
            }
        }
        System.out.println(this.stock.stocks.toString());
        while (true) {
            ProtocolMessage message;
            try {
                message = this.commDaemon.delivery();
                boolean result = this.processMessage(message);
                if ((message.getEvent().equals(BuyEvent) || message.getEvent().equals(SellEvent)) && result) {
                    log.add(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMembers(int members) {
        this.members.set(members);
    }

    public void setup() {
        StockReceiver sr = new StockReceiver(this.commDaemon, this);
        new Thread(sr).start();
        while (this.members.intValue() < 1) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (this.members.intValue() > 1) {
            if ((this.log = this.joinGroup()) == null) {
                this.log = new Vector<>();
            }
        }else{
            commDaemon.setDiscard(false);
        }


        this.processor();
    }
}

class StockReceiver implements Runnable {
    private CommDaemon<ProtocolMessage> commDaemon;
    private StockServer ss;

    public StockReceiver(CommDaemon<ProtocolMessage> commDaemon, StockServer ss) {
        this.commDaemon = commDaemon;
        this.ss = ss;
    }

    @Override
    public void run() {
        boolean notify = true;
        try {
            while (true) {
                int members = this.commDaemon.receive();
                if (members != -1) {
                    ss.setMembers(members);
                    if (notify) {
                        notify = false;
                        synchronized (ss) {
                            ss.notify();
                        }
                    }

                }
            }
        } catch (SpreadException | InterruptedIOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}