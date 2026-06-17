package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs extends Communication {
    public static final int message_id = 3;
    public MapLocation corpse_pos;
    public int corpse_id;

    public RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(int decryptedMessage) {
        super();
    }


    @Override
    public void handle(RobotPlayer[] interface_array) {

    }

    @Override
    public boolean predicate_met(RobotPlayer[] interface_array) {
        return false;
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
