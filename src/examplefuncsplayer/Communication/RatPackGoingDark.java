package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;

public class RatPackGoingDark extends Communication {
    public static final int message_id = 6;

    public int pack_id;

    public RatPackGoingDark(int decryptedMessage) {
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
        return true;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
