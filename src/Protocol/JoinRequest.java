package Protocol;

public class JoinRequest extends ProtocolMessage{
    public JoinRequest(String event, int port) {
        super(event, -1, port);
    }
}
