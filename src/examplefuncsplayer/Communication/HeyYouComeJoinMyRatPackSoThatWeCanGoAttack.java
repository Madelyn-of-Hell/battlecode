package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class HeyYouComeJoinMyRatPackSoThatWeCanGoAttack extends Communication {
    public static final int message_id = 10;

    public int pack_size;
    public int pack_id;
    public int sender_id;

    public HeyYouComeJoinMyRatPackSoThatWeCanGoAttack(int decryptedMessage) {
        super();
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
        return false;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
