package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

import java.util.Arrays;

public class CatWaypointFound extends Communication {
    public static final int message_id = 13;
    public MapLocation waypoint_position;

    public CatWaypointFound(int decryptedMessage) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        this.waypoint_position = new MapLocation(pos_x, pos_y);
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
//        return Arrays.stream(interface_array[0].terminusMessages).anyMatch(
//            terminusMessage -> terminusMessage.type == TerminusMessageType.KingAcknowledgeMessage
//        );
    }

    @Override
    public int package_message() {
        return 0;
    }
}
