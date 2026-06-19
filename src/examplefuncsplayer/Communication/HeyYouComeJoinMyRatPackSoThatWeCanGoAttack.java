package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class HeyYouComeJoinMyRatPackSoThatWeCanGoAttack extends Communication {
    public static final int message_id = 10;

    public int pack_size;
    public int pack_id;

    public HeyYouComeJoinMyRatPackSoThatWeCanGoAttack(int decryptedMessage, int sender_id) {
        this.pack_size = mask(decryptedMessage >>> 18, 8);
        this.pack_id = mask(decryptedMessage, 18);
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
        return false;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
