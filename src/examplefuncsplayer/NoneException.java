package examplefuncsplayer;

public class NoneException extends Exception {
    public String message;
    // Java doesn't have rustlike Options so I'm co-opting exceptions and try blocks to act the same way ^•^

    public NoneException(String message) {
        this.message = message;
    }
}
