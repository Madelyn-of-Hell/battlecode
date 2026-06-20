package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.RobotPlayer;

public class RatPackVolunteerToGoBackInsteadOfAttack extends Communication {
    public static final int message_id = 4;

    public int pack_id;

    public RatPackVolunteerToGoBackInsteadOfAttack(int decryptedMessage, int sender_id) {
        this.pack_id = mask(decryptedMessage, 27);
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (compare_id(robot[0].pack_id(), this.pack_id)) {
            if (robot[0].is_debriefing().isEmpty()) {
                robot[0].debrief_opt_in();
                robot[0].queue_message(new RatPackVolunteerToGoBackInsteadOfAttack(this.pack_id, robot[0].id()));
            }
            if (robot[0].id() > this.sender_id) {
                robot[0].debrief_opt_out();
            }
            robot[0].add_pack_member(this.sender_id);
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
        return message_id << 27 | Communication.mask(this.pack_id, 27);
    }
}
