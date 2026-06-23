package maddies_magnificent_horde.Communication;

import battlecode.common.MapLocation;
import maddies_magnificent_horde.RobotPlayer;

public class RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs extends Communication {
    @Override
public int message_id(){return 3;}
    public MapLocation corpse_position;

    public RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        this.corpse_position = new MapLocation(pos_x, pos_y);
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (robot[0].is_king()) {
            robot[0].remove_enemy_rat_king_dead(this.corpse_position);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return robot[0].predicate_messages().contains(new PredicateMessage(PredicateMessageType.KingInSqueakRadius, 0));
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].position().distanceSquaredTo(robot[0].king_loc()) < 18;
    }

    @Override
    public int package_message() {
        return message_id() << 27 | Communication.mask(corpse_position.x, 6) << 21 | Communication.mask(corpse_position.y, 6) << 15;
    }
}
