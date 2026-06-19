package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class EnemyRatKingFound extends Communication {
    public static final int message_id = 11;

    public MapLocation king_position;
    public int king_id;

    public EnemyRatKingFound(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        int id = mask(decryptedMessage, 15);
        this.king_position = new MapLocation(pos_x, pos_y);
        this.king_id = id;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {

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
        return message_id << 27 | Communication.mask(this.king_position.x, 6) << 21 | Communication.mask(this.king_position.y, 6) << 15 | Communication.mask(king_id, 15);
    }
}
