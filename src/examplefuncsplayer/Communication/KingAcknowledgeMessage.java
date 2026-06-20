package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class KingAcknowledgeMessage extends Communication {
    public static final int message_id = 1;

    public int acknowledged_message_type; // just the normie id of the message
    public int target_rat_id;

    public KingAcknowledgeMessage(int decryptedMessage, int sender_id) {
        this.acknowledged_message_type = mask(decryptedMessage >>> 22, 5);
        this.target_rat_id = mask(decryptedMessage, 22);
        this.sender_id = sender_id;
    }
    public KingAcknowledgeMessage(int message_type, int target_rat_id, int sender_id) {
        this.acknowledged_message_type = message_type;
        this.target_rat_id = target_rat_id;
        this.sender_id = sender_id;
    }

    @Override
    public void handle(RobotPlayer[] robot) {
        if (compare_id(robot[0].id(), this.target_rat_id)) {
            robot[0].queue_terminus(new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, acknowledged_message_type));
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public int package_message() {
        return message_id << 27 | this.acknowledged_message_type << 22 | Communication.mask(this.target_rat_id, 22);
    }
}
