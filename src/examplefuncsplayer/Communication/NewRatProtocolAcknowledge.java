package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotProtocol;

public class NewRatProtocolAcknowledge extends Communication {
    public static final int message_id = 2;

    public RobotProtocol protocol;

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
        return true;
    }

    @Override
    public int package_message() {
        return 0;
    }
}
