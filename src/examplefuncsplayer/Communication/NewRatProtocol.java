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
    public void handle(RobotPlayer[] robot) {
        if (compare_id(robot[0].id(), this.target_rat_id)) {
            robot[0].set_protocol(this.prescribed_protocol);
            robot[0].queue_message(new NewRatProtocolAcknowledge(prescribed_protocol, robot[0].id()));
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].terminus_messages().contains(new TerminusMessage(TerminusMessageType.NewRatProtocolAcknowledge, this.target_rat_id));
    }

    @Override
    public int package_message() {
        return message_id << 27 | prescribed_protocol.value << 25 | Communication.mask(target_rat_id, 25);
    }
}
