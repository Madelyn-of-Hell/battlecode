package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import examplefuncsplayer.RobotPlayer;

public class RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs extends Communication {
    public static final int message_id = 3;
    public MapLocation corpse_pos;
    public int corpse_id;

    public RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        int id = mask(decryptedMessage, 15);
        this.corpse_pos = new MapLocation(pos_x, pos_y);
        this.corpse_id = id;
        this.sender_id = sender_id;
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
