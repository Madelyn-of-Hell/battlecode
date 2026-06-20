package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.RobotPlayer;

public class RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs extends Communication {
    public static final int message_id = 3;
    public int corpse_id;

    public RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(int decryptedMessage, int sender_id) {
        this.corpse_id = mask(decryptedMessage, 27);
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (robot[0].is_king()) {
            robot[0].mark_enemy_rat_king_dead(this.corpse_id);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return robot[0].predicate_messages().contains(new PredicateMessage(PredicateMessageType.KingInSqueakRadius, 0));
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return robot[0].terminus_messages().contains(new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, message_id));
    }

    @Override
    public int package_message() {
        return message_id << 27 | Communication.mask(corpse_id, 27);
    }
}
