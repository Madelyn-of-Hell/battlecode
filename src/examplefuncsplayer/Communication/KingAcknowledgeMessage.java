package examplefuncsplayer.Communication;

public class KingAcknowledgeMessage extends Communication {
    public static final int message_id = 1;

    public int acknowledged_message_type;
    public int target_rat_id;

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
