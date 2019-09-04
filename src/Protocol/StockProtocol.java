package Protocol;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.Vector;

public class StockProtocol {
        public static final String BuyEvent = "BuyEvent";
        public static final String SellEvent = "SellEvent";
        public static final String OperationResponse = "OperationResponse";
        public static final String JoinResponseEvent = "JoinResponseEvent";
        public static final String JoinRequestEvent = "JoinRequestEvent";
        public static final String MembershipEvent = "MembershipEvent";
        public static Serializer newSerializer() {
            return Serializer.builder().withCompatibleSerialization()
                    .withTypes(
                            Operation.class,
                            OperationResponse.class,
                            JoinRequest.class,
                            JoinResponse.class,
                            MembershipMessage.class,
                            ArrayList.class,
                            Vector.class,
                            Integer.class
                    )
                    .build();
        }

    }

