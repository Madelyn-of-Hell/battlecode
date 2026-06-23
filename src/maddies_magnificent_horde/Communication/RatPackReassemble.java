package maddies_magnificent_horde.Communication;

import battlecode.common.MapLocation;
import maddies_magnificent_horde.RobotPlayer;
import maddies_magnificent_horde.RobotProtocol;

public class RatPackReassemble extends Communication {
    @Override
public int message_id(){return 8;}

    public MapLocation victim_pos;

    public RatPackReassemble(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        this.victim_pos = new MapLocation(pos_x, pos_y);
        this.sender_id = sender_id;
    }

    public RatPackReassemble(MapLocation target, int sender_id) {
        this.victim_pos = target;
        this.sender_id = sender_id;
    }

    @Override
    public void handle(RobotPlayer[] robot) {
        if (
                robot[0].current_protocol() != RobotProtocol.Attack ||
                robot[0].has_message_for_king()
        ) {
            robot[0].set_protocol(RobotProtocol.Attack);
            robot[0].add_pack_member(this.sender_id);
            robot[0].set_target_king_loc(this.victim_pos);
            robot[0].queue_message(this);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].position().distanceSquaredTo(robot[0].king_loc()) < 18;
    }

    @Override
    public int package_message() {
        return message_id() << 27 | Communication.mask(victim_pos.x, 6) << 21 | Communication.mask(victim_pos.y, 6) << 15;
    }
}
