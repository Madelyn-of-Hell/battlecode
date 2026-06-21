package maddies_magnificent_horde.Communication;

import maddies_magnificent_horde.RobotPlayer;
import maddies_magnificent_horde.RobotProtocol;

public class WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack extends Communication {
    @Override
public int message_id(){return 9;}

    public int old_pack_id;
    public int new_pack_id;
    public int sender_id;
    public WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(int decrypted_message, int sender_id) {
        this.old_pack_id = mask(decrypted_message >>> 13, 13);
        this.new_pack_id = mask(decrypted_message, 13);
        this.sender_id = sender_id;
    }
    public WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(int old_pack_id, int new_pack_id, int sender_id) {
        this.old_pack_id = old_pack_id;
        this.new_pack_id = new_pack_id;
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if (compare_id(robot[0].pack_id(), this.old_pack_id)) {
            robot[0].join_pack(this.new_pack_id);
            robot[0].add_pack_member(this.sender_id);
        } else if (robot[0].current_protocol() == RobotProtocol.Attack) {
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
        return message_id() << 27 | Communication.mask(this.old_pack_id, 13) << 14 | Communication.mask(this.new_pack_id, 13) << 1;
    }
}
