package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack extends Communication {
    public static final int message_id = 9;

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
    public void handle(RobotPlayer[] interface_array) {

    }

    @Override
    public boolean predicate_met(RobotPlayer[] interface_array) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] interface_array) {
        return true;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
