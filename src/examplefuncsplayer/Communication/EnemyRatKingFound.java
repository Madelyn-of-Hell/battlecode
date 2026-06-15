package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class EnemyRatKingFound extends Communication {
    public static final int message_id = 11;

    public MapLocation king_position;
    public int king_id;

    @Override
    Communication _sub_parse(int message_data) {
        return null;
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
