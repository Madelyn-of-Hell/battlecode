package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

import java.awt.*;

public class RatPackReassemble extends Communication {
    public static final int message_id = 8;

    public MapLocation victim_pos;
    public int victim_id;

    public RatPackReassemble(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        int id = mask(decryptedMessage, 15);
        this.victim_pos = new MapLocation(pos_x, pos_y);
        this.victim_id = id;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (
                robot[0].current_protocol() != RobotProtocol.Attack ||
                robot[0].has_message_for_king()
        ) {
            robot[0].set_protocol(RobotProtocol.Attack);
            robot[0].set_target_king_loc(this.victim_pos);
            robot[0].set_target_king_id(this.victim_id);
            robot[0].queue_message(this);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].terminus_messages().contains(new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, message_id));
    }

    @Override
    public int package_message() {
        return message_id << 27 | Communication.mask(victim_pos.x, 6) << 21 | Communication.mask(victim_pos.y, 6) << 15 | Communication.mask(victim_id, 15);
    }
}
