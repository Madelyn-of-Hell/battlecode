package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;

public class CheeseMineFound extends Communication {
    public static final int message_id = 12;

    public MapLocation mine_position;

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
