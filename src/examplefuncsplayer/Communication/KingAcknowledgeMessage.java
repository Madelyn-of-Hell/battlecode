package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class KingAcknowledgeMessage extends Communication {
    public static final int message_id = 1;

    public int acknowledged_message_type;
    public int target_rat_id;

    public KingAcknowledgeMessage(int decryptedMessage) {
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
        return true;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
