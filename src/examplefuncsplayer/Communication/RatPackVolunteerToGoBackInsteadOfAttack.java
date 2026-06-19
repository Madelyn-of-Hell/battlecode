package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class RatPackVolunteerToGoBackInsteadOfAttack extends Communication {
    public static final int message_id = 4;

    public int pack_id;

    public RatPackVolunteerToGoBackInsteadOfAttack(int decryptedMessage, int sender_id) {
        this.pack_id = mask(decryptedMessage, 27);
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
