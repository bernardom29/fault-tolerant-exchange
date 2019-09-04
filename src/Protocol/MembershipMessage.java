package Protocol;

public class MembershipMessage extends ProtocolMessage{
    private int member;
    public MembershipMessage(String event, int user, int port, int member) {
        super(event, user, port);
        this.member = member;
    }

    public MembershipMessage(String event, int user, int member) {
        super(event, user);
        this.member = member;
    }

    public MembershipMessage(String event, int member) {
        super(event);
        this.member = member;
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }
}
