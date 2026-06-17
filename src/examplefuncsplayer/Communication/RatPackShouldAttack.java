package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class RatPackShouldAttack extends Communication {
    public static final int message_id = 7;

    public MapLocation victim_pos;
    public int pack_id;

    public RatPackShouldAttack(int decryptedMessage) {
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
        return false;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
