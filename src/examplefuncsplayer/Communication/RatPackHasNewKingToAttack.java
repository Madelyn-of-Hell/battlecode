package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class RatPackHasNewKingToAttack extends Communication {
    public static final int message_id = 5;

    public MapLocation new_king_loc;
    public int pack_id;

    public RatPackHasNewKingToAttack(int decryptedMessage) {
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
