package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack extends Communication {
    public static final int message_id = 9;

    public int old_pack_id;
    public int new_pack_id;
    public int sender_id;
    public WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(int decrypted_message) {

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
