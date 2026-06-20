package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

public class RatPackHasNewKingToAttack extends Communication {
    public static final int message_id = 5;

    public MapLocation new_king_loc;
    public int pack_id;

    public RatPackHasNewKingToAttack(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        int id = mask(decryptedMessage, 15);
        this.new_king_loc = new MapLocation(pos_x, pos_y);
        this.pack_id = id;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (
            robot[0].current_protocol() == RobotProtocol.Attack &&
            robot[0].pack_id() == this.pack_id &&
            robot[0].target_king_loc() != this.new_king_loc
        ) {
            robot[0].set_target_king_loc(this.new_king_loc);
            robot[0].queue_message(this);
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
        return message_id << 27 | Communication.mask(new_king_loc.x, 6) << 21 | Communication.mask(new_king_loc.y, 6) << 15 | Communication.mask(pack_id, 15);
    }
}
