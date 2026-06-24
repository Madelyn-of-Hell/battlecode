package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.RobotPlayer;
import maddies_magnificent_horde.RobotProtocol;

public class RatPackVolunteerToGoBackInsteadOfAttack extends Communication {
    @Override
public int message_id(){return 4;}

    public RatPackVolunteerToGoBackInsteadOfAttack(int decryptedMessage, int sender_id) {
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (robot[0].current_protocol() == RobotProtocol.Attack) {
            if (robot[0].is_debriefing().isEmpty()) {
                robot[0].debrief_opt_in();
                robot[0].queue_message(new RatPackVolunteerToGoBackInsteadOfAttack(0, robot[0].id()));
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
        return message_id() << 27;
    }
}
