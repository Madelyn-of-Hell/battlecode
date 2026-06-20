package maddies_magnificent_horde.Communication;

public class PredicateMessage {
    public PredicateMessageType type;
    public int value;

    public PredicateMessage(PredicateMessageType message_type, int i) {
        this.type = message_type;
        this.value = i;
    }
}
