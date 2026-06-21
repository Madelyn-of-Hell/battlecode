package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.RobotPlayer;
import maddies_magnificent_horde.RobotProtocol;

public class NewRatProtocolAcknowledge extends Communication {
    @Override
public int message_id(){return 2;}

    public RobotProtocol protocol;

    public NewRatProtocolAcknowledge(int decryptedMessage, int sender_id) {
        int acknowledged_protocol = mask(decryptedMessage >>> 25, 2);
        this.protocol = RobotProtocol.values()[acknowledged_protocol];
        this.sender_id = sender_id;
    }
    public NewRatProtocolAcknowledge(RobotProtocol protocol, int sender_id) {
        this.protocol = protocol;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        // Thankfully, we know this sender ID is lossless so we don't need to do comparisons and the like to determine if the ids r the same
        robot[0].queue_terminus(new TerminusMessage(TerminusMessageType.NewRatProtocolAcknowledge, this.sender_id));
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
        return message_id() << 27 | this.protocol.value << 25;
    }
}
