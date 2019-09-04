package Protocol;

public class ProtocolMessage {
    String event; //50
    String sender; //50
    int user; //4
    int port; //4

    public ProtocolMessage(String event, int user, int port) {
        this.event = event;
        this.user = user;
        this.port = port;
    }
    public ProtocolMessage(String event, int user) {
        this.event = event;
        this.user = user;
        this.port = 1000;
    }

    public ProtocolMessage(String event) {
        this.event = event;
        this.user = -1;
        this.port = 1000;
    }

    public void addSender(String group) {
        sender = group;
    }

    public String getSender() {
        return sender;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
