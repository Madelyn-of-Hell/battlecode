package maddies_magnificent_horde.Communication;

import battlecode.common.MapLocation;
import maddies_magnificent_horde.RobotPlayer;

public class CheeseMineFound extends Communication {
    @Override
public int message_id(){return 12;}

    public MapLocation mine_position;

    public CheeseMineFound(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        this.mine_position = new MapLocation(pos_x, pos_y);
        this.sender_id = sender_id;
    }
    public CheeseMineFound(MapLocation mine_position, int sender_id) {
        this.mine_position = mine_position;
        this.sender_id = sender_id;
    }

    @Override
    public void handle(RobotPlayer[] robot) {
        if (!robot[0].cheese_mines().contains(this.mine_position)) {
            robot[0].add_cheese_mine(this.mine_position);
        }
        if (robot[0].is_king()) {
            robot[0].add_cheese_mine(this.mine_position);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].position().distanceSquaredTo(robot[0].king_loc()) < 4;
    }

    @Override
    public int package_message() {
        return message_id() << 27 | Communication.mask(this.mine_position.x, 6) << 21 | Communication.mask(this.mine_position.y, 6) << 15;
    }
}
