package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

public class NewRatProtocol extends Communication {
    public static final int message_id = 0;

    public int target_rat_id;
    public RobotProtocol prescribed_protocol;

    public NewRatProtocol(int decryptedMessage, int sender_id) {
        int prescribed_protocol = mask(decryptedMessage >>> 25, 2);
        this.prescribed_protocol = RobotProtocol.values()[prescribed_protocol];

        this.target_rat_id = mask(decryptedMessage, 25);
        this.sender_id = sender_id;
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
