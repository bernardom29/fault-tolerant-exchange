package Protocol;

public class Operation extends ProtocolMessage{
    private final Integer quantity; //4
    private final String company; //50
    public String address; //50
    private int localId; //5

    public Operation(int localId, String event, int user, int port, String company, Integer quantity) {
        super(event, user, port);
        this.localId = localId;
        this.quantity = quantity;
        this.company = company;
    }
    public Operation(int localId, String event, int user, String company, Integer quantity) {
        super(event, user);
        this.localId = localId;
        this.quantity = quantity;
        this.company = company;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getCompany() {
        return company;
    }

    public String getAddress() {
        return address;
    }

    public int getLocalId() {
        return localId;
    }
}
