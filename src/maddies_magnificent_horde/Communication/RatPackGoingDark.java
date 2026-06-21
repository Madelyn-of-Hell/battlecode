package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.ExploreTerminus;
import maddies_magnificent_horde.RobotPlayer;
import maddies_magnificent_horde.RobotProtocol;

public class RatPackGoingDark extends Communication {
    @Override
public int message_id(){return 6;}

    public int pack_id;

    public RatPackGoingDark(int decryptedMessage, int sender_id) {
        this.pack_id = mask(decryptedMessage, 27);
        this.sender_id = sender_id;
    }

    @Override
    public void handle(RobotPlayer[] robot) {
        if (robot[0].current_protocol() == RobotProtocol.Attack && compare_id(robot[0].pack_id(),this.pack_id)) {
            robot[0].set_protocol(RobotProtocol.Explore);
            robot[0].add_pack_member(this.sender_id);
            robot[0].set_explore_terminus(ExploreTerminus.EnemyRatKingFound);
            robot[0].queue_message(new RatPackGoingDark(this.pack_id, robot[0].id())); // Let it echo through the pack; no risk of feedback given we only get here if they're in attack mode
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
        return message_id() << 27 | Communication.mask(this.pack_id, 27);
    }
}
