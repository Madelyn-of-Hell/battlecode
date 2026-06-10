package examplefuncsplayer.Communication;

public class TerminusMessage {
    public TerminusMessage(TerminusMessageType type, int value) {
        this.type = type;
        this.value = value; // The imperfection of using a value here is Not Lost on me but IDK enough about java to implement a better method and my instinct of using rust-like enums just doesn't work here, which sucks because—look it up—they would genuinely be perfect for this.
    }
    public TerminusMessageType type;
    public int value;
}
