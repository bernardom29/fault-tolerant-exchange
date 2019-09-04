package Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class JoinResponse extends ProtocolMessage{
    public Vector<ProtocolMessage> replicaLog;
    public boolean end;
    public int chunk;

    public JoinResponse(String event) {
        super(event, -1);
    }

    public JoinResponse(String event, Vector<ProtocolMessage> log, boolean end, int chunk) {
        super(event, -1);
        this.replicaLog = new Vector<>();
        if (log != null)
            this.replicaLog.addAll(log);
        this.end = end;
        this.chunk = chunk;
    }
}
