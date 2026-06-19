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
    public LinkedList<TerminusMessage>  terminus_messages;
    public LinkedList<PredicateMessage> predicate_messages;
    public int id;
    public boolean is_king;
    public RobotProtocol current_protocol;
    public RobotController rc;
    public LinkedList<MapLocation> cat_waypoints;
    public LinkedList<MapLocation> cheese_mines;
    public HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings;

    public DstarMap nav_map;
    public int pack_id;
    public int pack_size;
    public int[] known_pack_members;
    public LinkedList<Communication> queued_messages;
    public int shared_key;
    public int[] shared_array_mirror = new int[64];

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
        this.queued_messages.removeIf(
                message -> message.terminus_met(new RobotPlayer[]{this})
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
        // apparently .equals works with null values so im safe to not check first if it exists
        boolean has_changed = (Objects.equals(this.enemy_rat_kings.get(id), new EnemyRatKingPosition(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive)));
        this.enemy_rat_kings.put(king_id, new EnemyRatKingPosition(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive));
        if (this.is_king && has_changed) {
            System.out.println(this.broadcast_enemy_king(king_pos).message);
        }
    }

    public Result broadcast_cat_waypoint(MapLocation cat_waypoint) {
        Optional<Integer> first_free_index = this.first_free_index(11, 26);
        if (first_free_index.isPresent()) {
            int ffi = first_free_index.get();
            int compressed_coordinate = this.compressed_coordinate(cat_waypoint);
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
        Optional<Integer> first_free_index = this.first_free_index(38, 27);
        if (first_free_index.isPresent()) {
            int ffi = first_free_index.get();
            int compressed_coordinate = this.compressed_coordinate(cheese_mine);
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

    public Result broadcast_enemy_king(MapLocation cheese_mine) {
        Optional<Integer> first_free_index = this.first_free_index(6, 5);
        if (first_free_index.isPresent()) {
            int ffi = first_free_index.get();
            int compressed_coordinate = this.compressed_coordinate(cheese_mine);
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

    public Result broadcast_friendly_king(MapLocation cheese_mine) {
        Optional<Integer> first_free_index = this.first_free_index(1, 5);
        if (first_free_index.isPresent()) {
            int ffi = first_free_index.get();
            int compressed_coordinate = this.compressed_coordinate(cheese_mine);
            try {
                this.rc.writeSharedArray(ffi, compressed_coordinate);
                this.shared_array_mirror[ffi] = compressed_coordinate;
                return Result.ok();
            } catch (GameActionException e) {
                // This should NEVER occur if I manage to properly implement the budget-conscious system.
                System.out.println(e);
                return Result.fail("Somehow failed to write to the shared array...");
            }
        }
        return Result.fail("No free spots in shared array...");
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

    public void join_pack(int pack_id) {
        this.queue_message( new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(this.pack_id, pack_id, this.id));
        this.pack_id = pack_id;
    }
}
