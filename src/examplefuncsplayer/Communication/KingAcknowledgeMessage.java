package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class KingAcknowledgeMessage extends Communication {
    public static final int message_id = 1;

    public int acknowledged_message_type; // just the normie id of the message
    public int target_rat_id;

    public KingAcknowledgeMessage(int decryptedMessage) {
        this.acknowledged_message_type = mask(decryptedMessage >>> 22, 5);
        this.target_rat_id = mask(decryptedMessage, 22);
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
