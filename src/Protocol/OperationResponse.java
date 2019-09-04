package Protocol;

public class OperationResponse extends ProtocolMessage {
    private final boolean success;
    private int localId;
    private int members;

    public OperationResponse(int localId, String event, int user, int port, boolean success, int members) {
        super(event, user, port);
        this.localId = localId;
        this.success = success;
        this.members = members;
    }

    public OperationResponse(int localId, String event, int user, boolean success, int members) {
        super(event, user);
        this.localId = localId;
        this.success = success;
        this.members = members;
    }

    public OperationResponse(int localId, int user, boolean success, int members) {
        super(StockProtocol.OperationResponse, user);
        this.localId = localId;
        this.success = success;
        this.members = members;
    }

    public OperationResponse(int localId, boolean success, int members) {
        super(StockProtocol.OperationResponse);
        this.localId = localId;
        this.success = success;
        this.members = members;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getLocalId() {
        return localId;
    }

    public int getMembers() {
        return members;
    }
}
