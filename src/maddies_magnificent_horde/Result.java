package maddies_magnificent_horde;
/// My custom rust-like Result class. Very much lesser than the actual rust Result, but if I need more i'll implement it on the fly.
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
