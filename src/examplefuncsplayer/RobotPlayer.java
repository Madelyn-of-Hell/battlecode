package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

import battlecode.schema.RobotType;
import examplefuncsplayer.Communication.Communication;
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
    public TerminusMessage[]  terminus_messages;
    public PredicateMessage[] predicate_messages;
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
    public LinkedList<Communication> queued_messages;
    public int shared_key;

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
    public static void run(RobotController rc) {
        //Initial Setup
        RobotPlayer[] self = new RobotPlayer[]{ new RobotPlayer(
                rc.getID(),
                RobotProtocol.None,
                rc.getType() == UnitType.RAT_KING,
                rc.getMapWidth(),
                rc.getMapHeight(),
                rc
        )};
        if (self[0].is_king) {
            self[0].current_protocol = RobotProtocol.Propagate;
            self[0].shared_key = Communication.create_key();
            try {
                self[0].rc.writeSharedArray(0, self[0].shared_key);
            } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't add the shared key to the array because ");
                System.out.println(e.getMessage());
            }
        } else {
            try {
                self[0].shared_key = rc.readSharedArray(0);
            } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't read the shared key from the array because ");
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            self[0].handle_incoming_communication();

            switch (self[0].current_protocol) {
                case Explore: {
                    self[0].explore();
                }
                case Gather: {
                    self[0].gather();
                }
                case Attack: {
                    self[0].attack();
                }
                case Propagate: {
                    self[0].propagate();
                }
                case Conserve:{
                    self[0].conserve();
                }
                case None: {

                }
            }
            self[0].handle_outgoing_communication();

            //Turn OVER
            Clock.yield();
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    // TODO: Add Tests
    private void handle_outgoing_communication() {
        for (Communication message : this.queued_messages) {
            if (message.terminus_met(new RobotPlayer[]{this})) {
                this.queued_messages.remove(message);
            }
        }

        this.queued_messages.sort(new Comparator<Communication>() {
            @Override
            public int compare(Communication o1, Communication o2) {
                if (o1.message_id < o2.message_id) {
                    return -1;
                }
                else if (o1.message_id > o2.message_id) {
                    return 1;
                }
                return 0;
            }
        });

        for (Communication message : this.queued_messages) {
            if (message.predicate_met(new RobotPlayer[]{this})) {
                this.rc.squeak(message.render(new RobotPlayer[]{this}));
                break;
            }
        }
    }
    // TODO: Add Tests
    private void gather() {
    }
    // TODO: Add Tests
    private void attack() {
    }
    // TODO: Add Tests
    private void propagate() {
    }
    // TODO: Add Tests
    private void conserve() {
    }
    // TODO: Add Tests
    private void explore() {
    }
    // TODO: Add Tests
    public void handle_incoming_communication() {
        for (Message message : this.rc.readSqueaks(-1)) {
            Communication comm = Communication.parse(message, new RobotPlayer[]{this});
            comm.handle(new RobotPlayer[]{this});
        }
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
