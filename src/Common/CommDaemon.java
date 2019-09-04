package Common;

import Protocol.JoinRequest;
import Protocol.JoinResponse;
import Protocol.ProtocolMessage;
import io.atomix.utils.serializer.Serializer;
import io.github.classgraph.utils.Join;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class CommDaemon<T> {
    private SpreadConnection connection;
    private SpreadGroup group;
    private Serializer serializer;
    private LinkedBlockingQueue<T> queue;
    private LinkedBlockingQueue<T> queueRep;
    private String defaultSendGroup;
    private String name;
    private AtomicBoolean discard;
    private int members;

    public CommDaemon(String conn, String group_name, String send_group, Serializer serializer, int port) throws IOException {
        this.members = 1;
        this.connection = new SpreadConnection();
        this.group = new SpreadGroup();
        this.serializer = serializer;
        this.queue = new LinkedBlockingQueue<>();
        this.queueRep = new LinkedBlockingQueue<>();
        this.discard = new AtomicBoolean(true);
        this.name = "#"+conn+"#localhost";
        this.defaultSendGroup = send_group;
        try {
            this.connection.connect(InetAddress.getByName("localhost"), 4803, conn, false, true);
            this.group.join(this.connection, group_name);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public void multicast(T data) throws SpreadException {
        multicastImpl(data, this.defaultSendGroup);
    }

    public void multicast(T data, String send_group) throws SpreadException {
        multicastImpl(data, send_group);
    }

    private void multicastImpl(T data, String send_group) throws SpreadException {
        byte[] packet = this.serializer.encode(data);
        SpreadMessage message = new SpreadMessage();
        message.setData(packet);
        message.setAgreed();
        message.setReliable();
        message.addGroup(send_group);
        this.connection.multicast(message);
    }

    public int receive() throws SpreadException, InterruptedIOException, InterruptedException {
        SpreadMessage message = this.connection.receive();
        if (message.getSender().toString().equals(this.name)){
            T protocolMessage = serializer.decode(message.getData());
            if (protocolMessage instanceof JoinRequest) {
                discard.getAndSet(false);
            }
        }
        if (message.isRegular()) {
            T protocolMessage = serializer.decode(message.getData());
            ((ProtocolMessage) protocolMessage).addSender(message.getSender().toString());
            if (protocolMessage instanceof JoinResponse){
                this.queueRep.put(protocolMessage);
            }else if (!discard.get()){
                this.queue.put(protocolMessage);
            }
            return -1;
        } else {
            members = message.getMembershipInfo().getMembers().length;
            return members;
        }
    }

    public T delivery() throws InterruptedException {
        return this.queue.take();
    }

    public T deliveryRep()  {
        try {
            return this.queueRep.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setDiscard(boolean b) {
        discard.getAndSet(b);
    }
}