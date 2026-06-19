package examplefuncsplayer;

public class Result {
    boolean success;
    String message;
    private Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public static Result ok() {
        return new Result(true, null);
    }
    public static Result fail(String message) {
        return new Result(true, message);

    }
}
