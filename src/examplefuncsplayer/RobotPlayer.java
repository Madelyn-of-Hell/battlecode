package examplefuncsplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Random;

import examplefuncsplayer.Communication.PredicateMessage;
import examplefuncsplayer.Communication.TerminusMessage;
import examplefuncsplayer.dstar.DstarMap;
import scala.Int;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    static final int cat_waypoint_danger_radius = 4;
    public TerminusMessage[]  terminusMessages;
    public PredicateMessage[] predicateMessages;
    public int id;
    public boolean is_king;
    public RobotProtocol current_protocol;
    public RobotController rc;
    public MapLocation[] cat_waypoints;
    public MapLocation[] cheese_mines;
    public HashMap<String, HashMap<Integer, MapLocation>> enemy_rat_kings;
    public DstarMap nav_map;
    public int pack_id;
    public int pack_size;
    public int[] known_pack_members;
    public RobotPlayer(int id, RobotProtocol start_protocol, boolean is_king, int width, int height, RobotController rc) { // be REAL NICE if i could add default values here, JAVA

    };
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        //Initial Setup

        while (true) {
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "FailoutTest");
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    //TODO: Add Tests
    public static DstarMap return_cat_waypoint(DstarMap nav_map, MapLocation cat_waypoint) {
        return nav_map;
    }
    //TODO: Add Tests
    public void add_cat_waypoint(MapLocation cat_waypoint) {
        this.nav_map = return_cat_waypoint(this.nav_map, cat_waypoint);
    }
}
