package maddies_magnificent_horde.Communication;

import battlecode.common.MapLocation;
import maddies_magnificent_horde.RobotPlayer;

import java.util.Optional;
import java.util.Set;

public class EnemyRatKingFound extends Communication {
    public static final int message_id = 11;

    public MapLocation king_position;
    public int king_id;

    public EnemyRatKingFound(int decryptedMessage, int sender_id) {
        int pos_x = mask(decryptedMessage >>> 21, 6);
        int pos_y = mask(decryptedMessage >>> 15, 6);
        int id = mask(decryptedMessage, 15);
        this.king_position = new MapLocation(pos_x, pos_y);
        this.king_id = id;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        Optional<Integer> match_id = this.find_id(robot[0].enemy_rat_kings().keySet());

        if (match_id.isEmpty()) {
            robot[0].add_enemy_rat_king(this.king_position, this.king_id);
        } else {
            robot[0].add_enemy_rat_king(this.king_position, match_id.get());
        }
        if (robot[0].is_king()) {
            robot[0].queue_message(new KingAcknowledgeMessage(message_id, this.sender_id, robot[0].id()));
        }

    }
    // Holy jeepers creepers batman I spent like 40 minutes trying to do this inline and it was so painful i had to make it its own method. Im SURE it's not that complicated; it'd be easy as anything in rust, and I suspect the only reason I can't do it in java is because I'm new to java but 🤷‍♀️
    private Optional<Integer> find_id(Set<Integer> ids) {
        for (Integer id : ids) {
            if (compare_id(id, this.king_id)) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }
    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].terminus_messages().contains(new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, message_id));
    }

    @Override
    public int package_message() {
        return message_id << 27 | Communication.mask(this.king_position.x, 6) << 21 | Communication.mask(this.king_position.y, 6) << 15 | Communication.mask(king_id, 15);
    }
}
