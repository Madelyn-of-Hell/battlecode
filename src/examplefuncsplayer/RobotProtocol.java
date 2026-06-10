package examplefuncsplayer;

public enum RobotProtocol {
    None(0),
    Explore(1),
    Gather(2),
    Attack(3);

    public final int value;


    RobotProtocol(int value) {
        this.value = value;
    }
}
