package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

import examplefuncsplayer.Communication.Communication;
import examplefuncsplayer.Communication.PredicateMessage;
import examplefuncsplayer.Communication.TerminusMessage;
import examplefuncsplayer.Communication.WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack;
import examplefuncsplayer.dstar.DstarMap;

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

    // Subject to a lottttttttt of changes (probably) (if i get time to tweak stuff)
    public static final int PACK_ATTACK_SIZE = 10;

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
    private LinkedList<TerminusMessage>  terminus_messages; public LinkedList<TerminusMessage> terminus_messages() {return this.terminus_messages;}
    private LinkedList<PredicateMessage> predicate_messages; public LinkedList<PredicateMessage> predicate_messages() {return this.predicate_messages;}
    private final int id; public int id() {return this.id;}
    private final boolean is_king; public boolean is_king() {return this.is_king;}
    private RobotProtocol current_protocol; public RobotProtocol current_protocol() {return this.current_protocol;}
    public RobotController rc;
    private LinkedList<MapLocation> cat_waypoints; public LinkedList<MapLocation> cat_waypoints() {return this.cat_waypoints;}
    private LinkedList<MapLocation> cheese_mines; public LinkedList<MapLocation> cheese_mines() {return this.cheese_mines;}
    private HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings; public HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings() {return this.enemy_rat_kings;}

    private DstarMap nav_map;
    private int pack_id; public int pack_id() {return this.pack_id;}
    private int pack_size; public int pack_size() {return this.pack_size;}
    private int[] known_pack_members; public int[] known_pack_members() {return this.known_pack_members;}
    private LinkedList<Communication> queued_messages; public LinkedList<Communication> queued_messages() {return this.queued_messages;}
    private int shared_key;
    public int shared_key() {
        return this.shared_key;
    }
    private int[] shared_array_mirror = new int[64];
    private Optional<ExploreTerminus> explore_terminus;
    private MapLocation target_king_loc; public MapLocation target_king_loc() {return this.target_king_loc;}
    private int target_king_id; public int target_king_id() {return this.target_king_id;}


    public RobotPlayer(int id, RobotProtocol start_protocol, boolean is_king, int width, int height, RobotController rc) { // be REAL NICE if i could add default values here, JAVA
        this.id = id;
        this.is_king = is_king;
        this.current_protocol = start_protocol;
        this.rc = rc;
        this.nav_map = new DstarMap(width, height);
        this.queued_messages = new LinkedList<>();
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
        RobotPlayer robot = new RobotPlayer(
                rc.getID(),
                RobotProtocol.None,
                rc.getType() == UnitType.RAT_KING,
                rc.getMapWidth(),
                rc.getMapHeight(),
                rc
        );
        if (robot.is_king) {
            robot.current_protocol = RobotProtocol.Propagate;
            robot.shared_key = Communication.create_key();
            try {
                robot.rc.writeSharedArray(0, robot.shared_key);
            } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't add the shared key to the array because ");
                System.out.println(e.getMessage());
            }
        } else {
            try {
                robot.shared_key = rc.readSharedArray(0);
            } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't read the shared key from the array because ");
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            robot.handle_incoming_communication();
            switch (robot.current_protocol) {
                case Explore: {
                    robot.explore();
                }
                case Gather: {
                    robot.gather();
                }
                case Attack: {
                    robot.attack();
                }
                case Propagate: {
                    robot.propagate();
                }
                case Conserve:{
                    robot.conserve();
                }
                case None: {

                }
            }
            robot.handle_outgoing_communication();

            //Turn OVER
            Clock.yield();
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    private void observe() {

    }


    // TODO: Add Tests
    private void handle_outgoing_communication() {
        this.queued_messages.removeIf(
                message -> message.terminus_met(this.reference())
        ); // I gotta admit, Jetbrains' Java linter is kinda popping off with these recommendations

        this.queued_messages.sort((o1, o2) -> {
            if (o1.message_id < o2.message_id) {
                return -1;
            }
            else if (o1.message_id > o2.message_id) {
                return 1;
            }
            return 0;
        });

        for (Communication message : this.queued_messages) {
            if (message.predicate_met(this.reference())) {
                this.rc.squeak(message.render(this.reference()));
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
            Communication comm = Communication.parse(message, this.reference());
            comm.handle(this.reference());
        }
    }

    //TODO: Add Tests
    public static DstarMap return_cat_waypoint(DstarMap nav_map, MapLocation cat_waypoint) {
        return nav_map;
    }
    //TODO: Add Tests
    public void add_cat_waypoint(MapLocation cat_waypoint) {
        this.cat_waypoints.add(cat_waypoint);
        this.nav_map = return_cat_waypoint(this.nav_map, cat_waypoint);
        if (this.is_king) {
            System.out.println(this.broadcast_cat_waypoint(cat_waypoint).message);
        }
    }
    public void add_cheese_mine(MapLocation cheese_mine) {
        this.cat_waypoints.add(cheese_mine);
        if (this.is_king) {
            System.out.println(this.broadcast_cheese_mine(cheese_mine).message);
        }
    }
    public void add_enemy_rat_king(MapLocation king_pos, int king_id) {
        handle_enemy_rat_king(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive);
    }
    public void mark_enemy_rat_king_dead(int king_id) {
        handle_enemy_rat_king(new MapLocation(0,0), king_id, EnemyRatKingPosition.LifeStatus.Dead);
    }
    public void handle_enemy_rat_king(MapLocation king_pos, int king_id, EnemyRatKingPosition.LifeStatus status) {
        // apparently .equals works with null values so im safe to not check first if it exists
        boolean has_changed = (Objects.equals(this.enemy_rat_kings.get(id), new EnemyRatKingPosition(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive)));
        this.enemy_rat_kings.put(king_id, new EnemyRatKingPosition(king_pos, king_id, status));
        if (this.is_king && has_changed) {
            System.out.println(this.broadcast_enemy_king(king_pos).message);
        }
    }

    public Result broadcast_cat_waypoint(MapLocation cat_waypoint) {
        return broadcast_coordinates(cat_waypoint, 11, 26);
    }

    private Result broadcast_coordinates(MapLocation coordinates, int offset, int limit) {
        Optional<Integer> first_free_index = this.first_free_index(offset, limit);
        if (first_free_index.isPresent()) {
            int ffi = first_free_index.get();
            int compressed_coordinate = this.compressed_coordinate(coordinates);
            try {
                this.rc.writeSharedArray(ffi, compressed_coordinate);
                this.shared_array_mirror[ffi] = compressed_coordinate;
            } catch (GameActionException e) {
                // This should NEVER occur if I manage to properly implement the budget-conscious system.
                System.out.println(e);
                return Result.fail("Somehow failed to write to the shared array...");

            }
        }
        return Result.fail("No free spots in shared array...");
    }

    public Result broadcast_cheese_mine(MapLocation cheese_mine) {
        return broadcast_coordinates(cheese_mine, 38, 27);
    }

    public Result broadcast_enemy_king(MapLocation enemy_king) {
        return broadcast_coordinates(enemy_king, 6, 5);
    }

    public Result broadcast_friendly_king(MapLocation friendly_king) {
        return broadcast_coordinates(friendly_king, 1, 5);
    }

    private int compressed_coordinate(MapLocation catWaypoint) {
        // I shouldn't *need* a mask here since the largest map is 60 and thus the largest point is 6 bits long before shift but 'what could possibly go wrong' is the worst reason not to use protection.
        return Communication.mask(catWaypoint.x >>> 1,5) << 5 | Communication.mask(catWaypoint.y >>> 1,5);
    }

    public Optional<Integer> first_free_index(int offset, int limit) {
        for (int i = offset; i < offset + limit; i++) {
            if (this.shared_array_mirror[i] == 0) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public void queue_message(Communication message) {
        this.queued_messages.add(message);
    }
    public void queue_terminus(TerminusMessage message) {
        this.terminus_messages.add(message);
    }

    public void join_pack(int pack_id) {
        this.queue_message( new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(this.pack_id, pack_id, this.id));
        this.pack_id = pack_id;
    }

    public void set_protocol(RobotProtocol prescribedProtocol) {
        this.current_protocol = prescribedProtocol;
    }
    public RobotPlayer[] reference() {
        return new RobotPlayer[] {this};
    }
    public void set_target_king_loc(MapLocation target_king_loc) {
        this.target_king_loc = target_king_loc;
    }
    public void set_target_king_id(int target_king_id) {
        this.target_king_id = target_king_id;
    }
    public void set_explore_terminus(ExploreTerminus terminus) {
        this.explore_terminus = Optional.of(terminus);
    }
}
