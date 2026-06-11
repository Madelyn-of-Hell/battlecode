package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotProtocol;

public class CommunicationInterface {
    public TerminusMessage[]  terminusMessages;
    public PredicateMessage[] predicateMessages;
    public int id;
    public boolean is_king;
    public RobotProtocol current_protocol;

    public CommunicationInterface(int id, RobotProtocol start_protocol, boolean is_king) { // be REAL NICE if i could add default values here, JAVA

    };
}
