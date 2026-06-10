package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotProtocol;

public class NewRatProtocol extends Communication {
    public static final int message_id = 0;

    public int target_rat_id;
    public RobotProtocol prescribed_protocol;

    @Override
    Communication _sub_parse(int message_data) {
        return null;
    }

    @Override
    public void handle(CommunicationInterface[] interface_array) {

    }

    @Override
    public boolean predicate_met(CommunicationInterface[] interface_array) {
        return true;
    }

    @Override
    public boolean terminus_met(CommunicationInterface[] interface_array) {
        return false;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
