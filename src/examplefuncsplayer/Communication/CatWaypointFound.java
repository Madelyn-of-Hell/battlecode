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
    public void handle(RobotPlayer[] robot) {
        if (!robot[0].cat_waypoints.contains(this.waypoint_position)) {
            robot[0].add_cat_waypoint(this.waypoint_position);
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
        return message_id << 27 | mask(waypoint_position.x, 6)  << 21 | mask(waypoint_position.y, 6) << 15;
    }
}
