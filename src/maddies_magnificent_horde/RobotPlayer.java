package maddies_magnificent_horde;

import battlecode.common.*;
import java.util.*;
import java.util.stream.Stream;

import maddies_magnificent_horde.Communication.*;
import maddies_magnificent_horde.DStarLiteJava.DStarLite;
import maddies_magnificent_horde.DStarLiteJava.State;
import maddies_magnificent_horde.ExploreMode;

import static maddies_magnificent_horde.Communication.Communication.compare_id;

public class RobotPlayer {
    int turn = 0;

    // Subject to a lottttttttt of changes (probably) (if i get time to tweak stuff)
    public static final int PACK_ATTACK_SIZE = 10;
    static final int CAT_WAYPOINT_DANGER_RADIUS = 4; // this one isn't lol it's just true
    static final int CHEESE_SURVIVAL_BUFFER = 100;
    static final int CHEESE_PROSPERITY_RATE = 100;

    public static final double CAT_WAYPOINT_COST = 5;
    public static final double CAT_COST = -1;
    public static final double CLEAR_GROUND_COST = 1;
    public static final double RAT_COST = -1;
    public static final double WALL_COST = -1;
    public static final double DIRT_COST = 2;
    // Perpetually used properties

        // Basics
        /// The Rat's ID. Provided by {@link RobotController}.
        private final int id; public int id() {return this.id;}

        /// The position of this Rat. Tracked independently of the RobotController.
        private MapLocation position; public MapLocation position() {return this.position;}
        /// The position of the friendly King.
        private MapLocation king_loc; public MapLocation king_loc() {return this.king_loc;}
        /// The Shared Key for the team. Stored in index 0 of the SharedArrayBuffer
        private int shared_key; public int shared_key() {return this.shared_key;}
        /// Whether or not the rat is King
        private final boolean is_king; public boolean is_king() {return this.is_king;}
        /// The currently implemented protocol for this Rat.
        private RobotProtocol current_protocol; public RobotProtocol current_protocol() {return this.current_protocol;}
        /// The RobotController interface
        public RobotController rc;


        // Collections
        /// The pathfinder of the D* implementation I aped.
        private DStarLite pathfinder;
        /// The current target being navigated to. May be None if not navving somewhere.
        private Optional<MapLocation> nav_target; public Optional<MapLocation> nav_target() {return this.nav_target;}

        private boolean map_has_changed;
        private int path_index;
        private HashSet<MapLocation> known_walls;
        /// All messages currently in the outbound queue.
        private LinkedList<Communication> queued_messages; public LinkedList<Communication> queued_messages() {return this.queued_messages;}
        /// All terminus messages waiting to be acted upon.
        private LinkedList<TerminusMessage>  terminus_messages; public LinkedList<TerminusMessage> terminus_messages() {return this.terminus_messages;}
        /// All predicate messages waiting to be acted upon.
        private LinkedList<PredicateMessage> predicate_messages; public LinkedList<PredicateMessage> predicate_messages() {return this.predicate_messages;}
        /// The co-ordinates of all known Cat Waypoints.
        private HashSet<MapLocation> cat_waypoints; public HashSet<MapLocation> cat_waypoints() {return this.cat_waypoints;}
        /// The locations, IDs and statuses of all known Enemy Rat Kings.
        private HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings; public HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings() {return this.enemy_rat_kings;}
        public Random rng;


    // Attack mode specific Properties
        /// The ID of the current pack (if in Attack Mode).
        private int pack_id; public int pack_id() {return this.pack_id;}
        /// The size of the current pack (if in Attack Mode).
        private int pack_size; public int pack_size() {return this.pack_size;}
        /// The set of all known members of the current pack (if in Attack Mode).
        private HashSet<Integer> known_pack_members; public HashSet<Integer> known_pack_members() {return this.known_pack_members;}
        /// The location of the Target King (if in Attack Mode).
        private MapLocation target_king_loc; public MapLocation target_king_loc() {return this.target_king_loc;}
        /// The ID of the Target King (if in Attack Mode).
        private int target_king_id; public int target_king_id() {return this.target_king_id;}
        /// The current Attack substate (if in Attack Mode).
        private AttackState attack_state; public AttackState attack_state() {return attack_state;}
        /// Whether or not the Rat is currently opted in to debrief. Only Some() immediately after a Rat dies.
        private Optional<Boolean> is_debriefing; public Optional<Boolean> is_debriefing() {return this.is_debriefing;}

    // King specific properties
        /// A mirror of the SharedArrayBuffer so the king doesn't have to constantly do expensive lookups of the actual buffer.
        private int[] shared_array_mirror = new int[64];
        /// The number of rats that have been made by the King.
        public int rats_made() { // see 'new king strategy' for explanation
            return (int) ((this.rc.getCurrentRatCost() - 10) << 1) / 5;
        }
        private int[] cheese_recap;
        private int[] d_cheese_recap;
    // Explore mode specific properties
        /// The terminus condition for the Rat (if in Explore Mode). None if the rat started in Explore Mode.
        private Optional<ExploreTerminus> explore_terminus;
        private ExploreMode explore_mode;
    // Gather mode specific properties
        /// The list of all known Cheese Mines.
        private HashSet<MapLocation> cheese_mines; public HashSet<MapLocation> cheese_mines() {return this.cheese_mines;}
        private LinkedList<String> debug_log;

    public RobotPlayer(RobotController rc) {
        this.id = rc.getID();
        this.is_king = rc.getType() == UnitType.RAT_KING;
        this.current_protocol = RobotProtocol.None;
        this.nav_target = Optional.empty();
        this.rc = rc;
        this.pathfinder = new DStarLite();
        this.king_loc = this.parse_broadcast(1);
        this.queued_messages = new LinkedList<>();
        this.terminus_messages = new LinkedList<>();
        this.predicate_messages = new LinkedList<>();
        this.cat_waypoints = new HashSet<>();
        this.cheese_mines = new HashSet<>();
        this.known_walls = new HashSet<>();
        this.known_cheese = new HashSet<>();
        this.debug_log = new LinkedList<>();
        this.rng = new Random();
        this.cheese_recap = new int[10]; this.d_cheese_recap = new int[10];
        this.explore_mode = ExploreMode.Exploring;
        Arrays.fill(this.cheese_recap, this.rc.getGlobalCheese());
    }

    private MapLocation parse_broadcast(int channel) {
        try {
            int data = this.rc.readSharedArray(1);
            return new MapLocation(
                    Communication.mask(data >>> 5, 5) << 1,
                    Communication.mask(data, 5) << 1
            );
        } catch (GameActionException e) {
            throw new RuntimeException("Something Wrong with the broadcast Parsing");
        }
    }

    @SuppressWarnings("unused")
    public static void run(RobotController rc) {

        //Initial Setup
        RobotPlayer robot = new RobotPlayer(rc);

        if (robot.is_king) {
            robot.current_protocol = RobotProtocol.Propagate;
            robot.shared_key = Communication.create_key(robot.reference());
            System.out.println("SHARED KEY: " + robot.shared_key);
            try {
                robot.rc.writeSharedArray(0, robot.shared_key);
                robot.shared_array_mirror[0] = robot.shared_key;
                robot.broadcast_friendly_king(robot.rc.getLocation());
            } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't add the shared key to the array because ");
                System.out.println(e.getMessage());
            }
        } else {
            try { robot.shared_key = rc.readSharedArray(0); } catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't read the shared key from the array because ");
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            robot.map_has_changed = false;
            robot.turn++;
            robot.add_debug_info(Arrays.toString(robot.shared_array_mirror));
            robot.handle_incoming_communication();
            robot.observe();

            switch (robot.current_protocol) {
                case Explore: {
                    robot.explore();
                    break;
                }
                case Gather: {
                    robot.gather();
                    break;
                }
                case Attack: {
                    robot.attack();
                    break;
                }
                case Propagate: {
                    robot.propagate();
                    break;
                }
                case Conserve: {
                    robot.conserve();
                    break;
                }
                case None: {
                    robot.add_debug_info("No job...");
                    break;
                }
            }

            robot.navigate();
            robot.rc.setIndicatorString(String.join("\n", robot.debug_log));
            robot.debug_log.clear();
            robot.handle_outgoing_communication();
            //Turn OVER
            Clock.yield();
        }
    }

    private void debug_log() {

    }
    public void add_debug_info(String info) {
        this.debug_log.add(info);
    }
    /// Take a look at the surroundings and note down All the Things—other rats, cheese, tiles, etc.
    private void observe() {
        this.position = this.rc.getLocation();
        for (MapInfo detail : this.rc.senseNearbyMapInfos()) {
            if (detail.hasCheeseMine()){this.add_cheese_mine(detail.getMapLocation());}
            if (detail.isWall()){this.add_wall(detail.getMapLocation());}
            if (detail.getTrap() == TrapType.RAT_TRAP){}
        }
    }


    /// Sort messages by importance, shoot off the most important one, then clear it out if its terminus condition has been reached
    // TODO: Add Tests
    private void handle_outgoing_communication() {
        this.add_debug_info("Outbound Messages: " + this.queued_messages.size());
        this.queued_messages.sort((o1, o2) -> {
            if (o1.message_id() < o2.message_id()) {
                return -1;
            }
            else if (o1.message_id() > o2.message_id()) {
                return 1;
            }
            return 0;
        });

        for (Communication message : this.queued_messages) {
            if (message.predicate_met(this.reference())) {
                this.add_debug_info("Sending a message of type " + Integer.toBinaryString(message.message_id()));
//                System.out.println("Sending Message: " + Integer.toBinaryString(message.package_message()) + " Encrypted: " + Integer.toBinaryString(message.render(this.reference())) + " Shared key: " + Integer.toBinaryString(this.shared_key()) + " Message ID: " + Integer.toBinaryString(message.message_id()));
                this.rc.squeak(message.render(this.reference()));
                if (message.terminus_met(this.reference())) {
                    this.add_debug_info("Removed " + Integer.toBinaryString(message.message_id()) + " from the queue");
                    this.queued_messages.remove(message);
                }
                break;
            }
        }
    }

    /// Seek Cheese and Cheese Mines
    // TODO: Add Tests
    private void gather() {
    }

    /// Form a pack, hunt down enemy Kings, kill them, report back.
    // TODO: Add Tests
    private void attack() {
    }

    /// Churn out babies as fast as the movement cap will let you.
    // TODO: Add Tests
    private void propagate() {
        int rats = this.rats_made();
        this.add_debug_info("Rats: " + (rats + 2) + " ± 2");
        if (rats <= 20) {
            for (MapLocation i: this.king_spawn_locs()) {
                if (this.rc.canBuildRat(i)) {
                    try {
                        this.rc.buildRat(i);
                    } catch (GameActionException e) {throw new RuntimeException("Failed something definitely shown to be possible on turn" + this.rc.getRoundNum());}

                    RobotProtocol new_protocol = RobotProtocol.Explore;
                    try {
                        this.queue_message(new NewRatProtocol(
                                new_protocol,
                                this.rc.senseRobotAtLocation(i).getID(),
                                this.id
                        ));
                    } catch (GameActionException e) {throw new RuntimeException("Couldn't sense a robot at a location we KNOW it's in.");}
                    break;
                }
            }
        }
        this.add_debug_info("Propagate Mode | " + this.global_cheese_rate() + " Cheese/t");

        if (this.rc.getGlobalCheese() <= 2 * CHEESE_SURVIVAL_BUFFER && this.global_cheese_rate() >= CHEESE_PROSPERITY_RATE) {
            this.set_protocol(RobotProtocol.Conserve);
        }
    }

    /// Produce babies at a more conservative, budgeted rate.
    // TODO: Add Tests
    private void conserve() {
        if (this.rc.getGlobalCheese() < 2 * CHEESE_SURVIVAL_BUFFER || this.global_cheese_rate() <= CHEESE_PROSPERITY_RATE) {
            this.set_protocol(RobotProtocol.Propagate);
        }
    }

    /// Cover as much of the map as is possible, reporting any important discoveries.
    // TODO: Add Tests
    private void explore() {
        this.add_debug_info("Explore Mode");
        if (this.explore_mode == ExploreMode.Exploring) {
            if (this.nav_target.isEmpty()) {
                this.set_explore_target();
            } else if (this.position.distanceSquaredTo(this.nav_target.get()) < 4) {
                this.set_explore_target();
            }
        }
    }
    private void set_explore_target() {
        int width = this.rc.getMapWidth()-1;
        int height = this.rc.getMapHeight()-1;

        LinkedList<MapLocation> places = new LinkedList<MapLocation>();
        places.add(new MapLocation(0, 0));
        places.add(new MapLocation(width, 0));
        places.add(new MapLocation(0, height));
        places.add(new MapLocation(width, height));
        places.add(new MapLocation(width >> 1, height >> 1));
        if (this.nav_target.isPresent()) { places.remove(this.nav_target.get()); }
        this.nav_target = Optional.of(places.get(this.rng.nextInt(places.size())));
        this.add_debug_info("Heading now to " + this.nav_target.get());
    }


    /// Read and handle each incoming squeak as is necessary.
    // TODO: Add Tests
    public void handle_incoming_communication() {
        for (Message message : this.rc.readSqueaks(this.rc.getRoundNum()-1)) {
            // TODO: Figure out how to prevent interpretation of enemy comms. Can't just use sense because that requires looking and we can't turn that much.
            Communication comm = Communication.parse(message, this.reference());
            if (comm instanceof CheeseMineFound) {
                this.add_debug_info("Heard about cheese mine at " + ((CheeseMineFound) comm).mine_position);
            }
            this.add_debug_info("Message Received: " + Integer.toBinaryString(comm.package_message()));
            comm.handle(this.reference());
        }
    }


    /// Adds records of the new Waypoint in all the places they're needed.
    /// @param cat_waypoint the position of the Waypoint.
    //TODO: Add Tests
    public void add_cat_waypoint(MapLocation cat_waypoint) {
        if (this.cat_waypoints.add(cat_waypoint)) {
            if (this.is_king) {
                if (this.broadcast_cat_waypoint(cat_waypoint).success) {
                    add_debug_info("Broadcast a new cat waypoint at " + cat_waypoint);
                } else {
                    add_debug_info("Failed to broadcast a new cat waypoint at " + cat_waypoint);
                }
            } else {
                this.queue_message(new CatWaypointFound(cat_waypoint, this.id()));
                add_debug_info("Found a new cat waypoint at " + cat_waypoint);
            }
        }
    }

    /// Adds records of the new Cheese Mine in all the places they're needed.
    /// @param cheese_mine the position of the Cheese Mine.
    //TODO: Add Tests
    public void add_cheese_mine(MapLocation cheese_mine) {
        if (this.cheese_mines.add(cheese_mine)) {
            if (this.is_king()) {
                if (this.broadcast_cheese_mine(cheese_mine).success) {
                    add_debug_info("Broadcast a new cheese mine at " + cheese_mine);
                } else {
                    add_debug_info("Failed to broadcast a new cheese mine at " + cheese_mine);
                }
            } else {
                this.queue_message(new CheeseMineFound(cheese_mine, this.id));
                add_debug_info("Found a new cheese mine at " + cheese_mine);
            }
        }
    }

    private void add_wall(MapLocation wall) {

        if (this.known_walls.add(wall)) {
            this.pathfinder.updateCell(wall.x, wall.y, WALL_COST);
            this.map_has_changed = true;
        }
    }

    /// Adds a record of an Enemy Rat King, or modifies it if one already exists.
    /// Wrapper for {@link #handle_enemy_rat_king}
    /// @param king_pos the last known location of the King.
    /// @param king_id the ID of the King.
    public void add_enemy_rat_king(MapLocation king_pos, int king_id) {
        handle_enemy_rat_king(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive);
    }

    /// Marks a record of an Enemy Rat King as Dead.
    /// Wrapper for {@link #handle_enemy_rat_king}
    /// @param king_id the ID of the now-dead King.
    public void mark_enemy_rat_king_dead(int king_id) {
        handle_enemy_rat_king(new MapLocation(0,0), king_id, EnemyRatKingPosition.LifeStatus.Dead);
    }

    /// Update an Enemy Rat King according to the given parameters.
    /// @param king_id the ID of the relevant King.
    /// @param king_pos the location of the relevant King.
    /// @param status the status of the relevant king (living or dead).
    public void handle_enemy_rat_king(MapLocation king_pos, int king_id, EnemyRatKingPosition.LifeStatus status) {
        // apparently .equals works with null values so im safe to not check first if it exists
        boolean has_changed = (Objects.equals(this.enemy_rat_kings.get(id), new EnemyRatKingPosition(king_pos, king_id, EnemyRatKingPosition.LifeStatus.Alive)));
        this.enemy_rat_kings.put(king_id, new EnemyRatKingPosition(king_pos, king_id, status));
        if (this.is_king && has_changed) {
            System.out.println(this.broadcast_enemy_king(king_pos).message);
        }
    }

    /// Broadcasts a Cat Waypoint to the first available channel.
    /// Wrapper for {@link #broadcast_coordinates}.
    /// @param cat_waypoint the location of the relevant Cat Waypoint/.
    /// @return Whether or not the broadcast was successful. Fails if the array is full.
    public Result broadcast_cat_waypoint(MapLocation cat_waypoint) {
        return broadcast_coordinates(cat_waypoint, 11, 26);
    }

    /// Broadcasts a Cheese Mine to the first available channel.
    /// Wrapper for {@link #broadcast_coordinates}.
    /// @param cheese_mine the location of the relevant Cheese Mine.
    /// @return Whether or not the broadcast was successful. Fails if the array is full.
    public Result broadcast_cheese_mine(MapLocation cheese_mine) {
        return broadcast_coordinates(cheese_mine, 38, 27);
    }

    /// Broadcasts an Enemy Rat King to the first available channel.
    /// Wrapper for {@link #broadcast_coordinates}.
    /// @param enemy_king the location of the relevant Rat King.
    /// @return Whether or not the broadcast was successful. Fails if the array is full.
    public Result broadcast_enemy_king(MapLocation enemy_king) {
        return broadcast_coordinates(enemy_king, 6, 5);
    }

    /// Broadcasts a Friendly King to the first available channel. As yet without purpose, but I was making them all and it felt wrong to leave this one out.
    /// Wrapper for {@link #broadcast_coordinates}.
    /// @param friendly_king the location of the relevant Rat King.
    /// @return Whether or not the broadcast was successful. Fails if the array is full.
    @SuppressWarnings("unused")
    public Result broadcast_friendly_king(MapLocation friendly_king) {
        return broadcast_coordinates(friendly_king, 1, 5);
    }

    /// Broadcasts a compressed co-ordinate pair to the first available channel within the given range.
    /// @param coordinates the co-ordinates to be broadcast.
    /// @param offset the offset from which to start searching the array.
    /// @param limit the number of indices to search before terminating (because each type of element has a limited allotment of arrays).
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

    /// Compresses a co-ordinate into a 10-digit integer. Lossy, but acceptably so.
    /// @param coordinate the co-ordinate to be compressed.
    private int compressed_coordinate(MapLocation coordinate) {
        // I shouldn't *need* a mask here since the largest map is 60 and thus the largest point is 6 bits long before shift but 'what could possibly go wrong' is the worst reason not to use protection.
        return Communication.mask(coordinate.x >>> 1,5) << 5 | Communication.mask(coordinate.y >>> 1,5);
    }

    /// Returns the first free index of the array mirror within the given range, or None if there aren't any.
    /// @param offset the offset from which to start searching the array.
    /// @param limit the number of indices to search before terminating (because each type of element has a limited allotment of arrays).
    public Optional<Integer> first_free_index(int offset, int limit) {
        for (int i = offset; i < offset + limit; i++) {
            if (this.shared_array_mirror[i] == 0) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /// Adds a message to the outbound queue {@link #queued_messages}.
    /// @param message the message to queue.
    public void queue_message(Communication message) {
        this.queued_messages.add(message);
    }

    /// Adds a message to the terminus queue {@link #terminus_messages}.
    /// @param message the Terminus Message to add.
    public void queue_terminus(TerminusMessage message) {
        this.terminus_messages.add(message);
    }

    /// Sets the rat's pack {@link #pack_id}, and announces it.
    /// @param pack_id the pack to join.
    public void join_pack(int pack_id) {
        this.queue_message( new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(this.pack_id, pack_id, this.id));
        this.pack_id = pack_id;
        this.clear_known_pack_members();
    }

    /// Set's the rat's protocol {@link #current_protocol}.
    /// @param prescribed_protocol the new protocol.
    public void set_protocol(RobotProtocol prescribed_protocol) {
        this.current_protocol = prescribed_protocol;
    }

    /// Returns a reference to this, in the form of an array containing only this.
    /// @return the reference.
    public RobotPlayer[] reference() {
        return new RobotPlayer[] {this};
    }

    /// Sets the location of the target king.
    /// @param target_king_loc the location of the king.
    public void set_target_king_loc(MapLocation target_king_loc) {
        this.target_king_loc = target_king_loc;
    }

    /// Sets the id of the target king.
    /// @param target_king_id the location of the king.
    public void set_target_king_id(int target_king_id) {
        this.target_king_id = target_king_id;
    }

    /// Sets the terminus for the current exploration stint.
    /// @param terminus the terminus condition.
    public void set_explore_terminus(ExploreTerminus terminus) {
        this.explore_terminus = Optional.of(terminus);
    }

    /// Whether or not the Rat has a message that needs to be delivered to the king.
    /// @return it's a secret. guess.
    public boolean has_message_for_king() {
        for (Communication message : this.queued_messages) {
            if (
                    message instanceof CatWaypointFound ||
                    message instanceof CheeseMineFound ||
                    message instanceof EnemyRatKingFound ||
                    message instanceof RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs
            ) {
                return true;
            }
        }
        return false;
    }

    /// Opt in to deliver the debrief message to the King. Expected whenever a king dies.
    public void debrief_opt_in(){this.is_debriefing = Optional.of(true);}

    /// Opt out of delivering the debrief message to the King. Expected if a rat with a lower ID opts in.
    public void debrief_opt_out(){this.is_debriefing = Optional.of(false);}

    /// Clears the set of known pack members. Expected when switching packs.
    public void clear_known_pack_members() {
        this.known_pack_members = new HashSet<Integer>() {
        };
    }

    /// Adds a member of a pack. Expected whenever a Rat does something to indicate its affiliation.
    public void add_pack_member(int id) {
        if (this.known_pack_members().stream().noneMatch(known_id -> compare_id(known_id, id))) {
        }
        this.known_pack_members.add(id);
    }

    public void navigate() {
        if (this.nav_target.isPresent()) {
            this.navigate_naive();
        }
    }
    private void navigate_naive() {
        MapLocation target = this.nav_target().get();
        try {
            this.rc.setIndicatorLine(this.position(), target, 0, 0, 255);
            this.rc.setIndicatorDot(this.nav_target.get(), 0, 0, 255);
        } catch (GameActionException e) {System.out.println("Couldn't render a line ;-;");}

        Optional<Direction> direction = this.find_nearest_direction(this.position().directionTo(target));
        if (direction.isPresent()) {
            try {
                if (this.rc.canTurn()) {this.rc.turn(direction.get());}
                if (this.rc.canRemoveDirt(this.position().add(direction.get()))) {this.rc.removeDirt(this.position().add(direction.get()));}
                this.rc.move(direction.get());
            } catch (GameActionException e) {throw new RuntimeException(e);}
        }
    }

    private void navigate_dstar(MapLocation to) {
        // Making to an Optional means I don't have to check if nav_target is present first
        if (!Optional.of(to).equals(this.nav_target)) {
            this.nav_target = Optional.of(to);
            this.pathfinder.init(this.position().x, this.position().y, to.x,to.y);
            this.known_walls.clear();
            this.map_has_changed = true;
        }
        if (this.map_has_changed) { //Hopefully we rarely need to recalculate
            System.out.println("MAP HAS CHANGED");
            this.pathfinder.updateStart(this.position().x, this.position().y);
            path_index = 1;
            if (!this.pathfinder.replan()) {
                System.out.println("CAN'T GO FROM " + this.position() + " TO " + this.nav_target.get());
                assert (false);
            }
        } else {
            try {
                // For some reason when I try and use index a list with battlecode specifically it throws an error to do with the fact that they try and re-implement lists or something? idfk
                State next_tile = getState();
                MapLocation next_tile_map = new MapLocation(next_tile.x, next_tile.y);
                // While testing I wanna be failing loud and proud
                System.out.println("Current Positon: " + this.position() + "Next tile: " + next_tile_map + " Distance: " + this.position().distanceSquaredTo(next_tile_map));
                assert(this.position().distanceSquaredTo(next_tile_map) <= 2);
                Direction direction = this.position().directionTo(next_tile_map);
                try {
                    try {
                        rc.removeDirt(next_tile_map);
                    } catch (GameActionException ignored) {}//No dirt to remove

                    if (this.rc.canTurn()) {
                        this.rc.turn(direction);
                    }
                    if (this.rc.isMovementReady()) {
                        Optional<Direction> final_direction = this.find_nearest_direction(direction);
                        if (final_direction.isPresent()) {
                            this.rc.move(direction);
                            System.out.println("Moved to " + this.position());
                            path_index++;
                        }
                    }
                } catch (GameActionException e) {

                    throw new RuntimeException(this.id + "Couldn't move from (" + this.position().x + "," + this.position().y + ") to (" + next_tile.x + "," + next_tile.y + ") because " + e);
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e);
            }
        }
    }

    private State getState() {
        Object[] path = this.pathfinder.getPath().toArray();
//                String path_display = String.valueOf("PATH: " + path.length + " ");
//                for (Object node_obj : path) {
//                    State node_state = (State) node_obj;
//                    try {
//                        this.rc.setIndicatorDot(new MapLocation(node_state.x, node_state.y), 255, 255, 255);
//                    } catch (GameActionException e) {
//                        System.out.println("Debug tool failed");
//                        System.out.println(e);
//                    }
//                    path_display = path_display.concat(String.valueOf("(" + node_state.x + "," + node_state.y + "), "));
//                }
//                System.out.println(path_display);
        State next_tile = (State) path[path_index];
        return next_tile;
    }

    private Optional<Direction> find_nearest_direction(Direction direction) {
        final HashMap<Direction, Direction[]> direction_map = new HashMap<>();
        boolean direction_found = false;
        for (int r = 0; r < 8; r++) {
            Direction test_direction = direction;
            for (int dr = 0; dr < r; dr++) {
                test_direction = r % 2 == 0 ? test_direction.rotateLeft() : test_direction.rotateRight();
            }
            if (this.rc.canMove(test_direction) || this.rc.canRemoveDirt(this.position().add(test_direction))) {
                return Optional.of(test_direction);
            }
        }
        return Optional.empty();
    }
    private int global_cheese_rate() {
        this.cheese_recap[this.turn % 10] = this.rc.getGlobalCheese();
        this.d_cheese_recap[this.turn % 10] = this.cheese_recap[this.turn % 10] - this.cheese_recap[(this.turn-1) % 10];
        this.add_debug_info("d(cheese)/dt = " + this.d_cheese_recap[this.turn % 10]);
        // I'm not kidding for some reason battlecode's implementation of java can't do sum or average of an array. so.
        return (this.d_cheese_recap[0] + this.d_cheese_recap[1] + this.d_cheese_recap[2] + this.d_cheese_recap[3] + this.d_cheese_recap[4] + this.d_cheese_recap[5] + this.d_cheese_recap[6] + this.d_cheese_recap[7] + this.d_cheese_recap[8] + this.d_cheese_recap[9]) / 10;
    }
    private void add_cheese(MapLocation cheese) {

        if (this.known_cheese.add(cheese)) {
            System.out.println("Added Cheese at " + cheese);
        } else {
//            System.out.println("Already knew about Cheese at " + cheese);
        }
    }

    private MapLocation[] king_spawn_locs() {
        return new MapLocation[]{
                this.position().add(Direction.NORTH).add(Direction.NORTH),
                this.position().add(Direction.SOUTH).add(Direction.SOUTH),
                this.position().add(Direction.EAST).add(Direction.EAST),
                this.position().add(Direction.WEST).add(Direction.WEST)
        };
    }
}
