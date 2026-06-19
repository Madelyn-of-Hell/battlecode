package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class CheeseMineFound extends Communication {
    public static final int message_id = 12;

    public MapLocation mine_position;

    public CheeseMineFound(int decryptedMessage) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        this.mine_position = new MapLocation(pos_x, pos_y);
    }

    @Override
    public void handle(RobotPlayer[] robot) {
        if (!robot[0].cheese_mines.contains(this.mine_position)) {
            robot[0].add_cheese_mine(this.mine_position);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].terminus_messages.contains(new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, message_id));
    }

    @Override
    public int package_message() {
        return message_id << 27 | Communication.mask(this.mine_position.x, 6) << 21 | Communication.mask(this.mine_position.y, 6) << 15;
    }
}
