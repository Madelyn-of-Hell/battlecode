package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

public class NewRatProtocolAcknowledge extends Communication {
    public static final int message_id = 2;

    public RobotProtocol protocol;

    public NewRatProtocolAcknowledge(int decryptedMessage) {
        int acknowledged_protocol = mask(decryptedMessage >>> 25, 2);
        this.protocol = RobotProtocol.values()[acknowledged_protocol];
    }


    @Override
    public void handle(RobotPlayer[] interface_array) {

    }

    @Override
    public boolean predicate_met(RobotPlayer[] interface_array) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] interface_array) {
        return true;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
