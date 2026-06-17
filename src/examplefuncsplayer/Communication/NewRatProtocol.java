package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

public class NewRatProtocol extends Communication {
    public static final int message_id = 0;

    public int target_rat_id;
    public RobotProtocol prescribed_protocol;

    public NewRatProtocol(int decryptedMessage) {
        super();
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
        return false;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
