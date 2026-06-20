package maddies_magnificent_horde;

import battlecode.common.*;
import java.util.*;
import maddies_magnificent_horde.Communication.*;
import maddies_magnificent_horde.dstar.DstarMap;

import static maddies_magnificent_horde.Communication.Communication.compare_id;

public class RobotPlayer {

    // Subject to a lottttttttt of changes (probably) (if i get time to tweak stuff)
    public static final int PACK_ATTACK_SIZE = 10;
    static final int CAT_WAYPOINT_DANGER_RADIUS = 4; // this one isn't lol it's just true

    // Perpetually used properties

        // Basics
        /// The Rat's ID. Provided by {@link RobotController}.
        private final int id; public int id() {return this.id;}
        /// The Shared Key for the team. Stored in index 0 of the SharedArrayBuffer
        private int shared_key; public int shared_key() {return this.shared_key;}
        /// Whether or not the rat is King
        private final boolean is_king; public boolean is_king() {return this.is_king;}
        /// The currently implemented protocol for this Rat.
        private RobotProtocol current_protocol; public RobotProtocol current_protocol() {return this.current_protocol;}
        /// The RobotController interface
        public RobotController rc;


        // Collections
        /// The map used by the D* implementation I aped.
        private DstarMap nav_map;
        /// All messages currently in the outbound queue.
        private LinkedList<Communication> queued_messages; public LinkedList<Communication> queued_messages() {return this.queued_messages;}
        /// All terminus messages waiting to be acted upon.
        private LinkedList<TerminusMessage>  terminus_messages; public LinkedList<TerminusMessage> terminus_messages() {return this.terminus_messages;}
        /// All predicate messages waiting to be acted upon.
        private LinkedList<PredicateMessage> predicate_messages; public LinkedList<PredicateMessage> predicate_messages() {return this.predicate_messages;}
        /// The co-ordinates of all known Cat Waypoints.
        private LinkedList<MapLocation> cat_waypoints; public LinkedList<MapLocation> cat_waypoints() {return this.cat_waypoints;}
        /// The locations, IDs and statuses of all known Enemy Rat Kings.
        private HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings; public HashMap<Integer, EnemyRatKingPosition> enemy_rat_kings() {return this.enemy_rat_kings;}


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
    // Explore mode specific properties
        /// The terminus condition for the Rat (if in Explore Mode). None if the rat started in Explore Mode.
        private Optional<ExploreTerminus> explore_terminus;
    // Gather mode specific properties
        /// The list of all known Cheese Mines.
        private LinkedList<MapLocation> cheese_mines; public LinkedList<MapLocation> cheese_mines() {return this.cheese_mines;}

    public RobotPlayer(RobotController rc) {
        this.id = rc.getID();
        this.is_king = rc.getType() == UnitType.RAT_KING;
        this.current_protocol = RobotProtocol.None;
        this.rc = rc;
        this.nav_map = new DstarMap(rc.getMapWidth(), rc.getMapHeight());
        this.queued_messages = new LinkedList<Communication>();
        this.terminus_messages = new LinkedList<TerminusMessage>();
        this.predicate_messages = new LinkedList<PredicateMessage>();
        this.cat_waypoints = new LinkedList<MapLocation>();
        this.cheese_mines = new LinkedList<MapLocation>();
    };
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {

        //Initial Setup
        RobotPlayer robot = new RobotPlayer(rc);

        if (robot.is_king) {
            robot.current_protocol = RobotProtocol.Propagate;
            robot.shared_key = Communication.create_key();

            try { robot.rc.writeSharedArray(0, robot.shared_key); }
            catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't add the shared key to the array because ");
                System.out.println(e.getMessage());
            }
        } else {
            try { robot.shared_key = rc.readSharedArray(0); }
            catch (GameActionException e) {
                // There is NO reason either of these should ever occur, but I don't want to include throws error on the function because I'll forget about it for something important.
                System.out.print("Couldn't read the shared key from the array because ");
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            robot.handle_incoming_communication();
            robot.observe();

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
    }

    /// Take a look at the surroundings and note down All the Things—other rats, cheese, tiles, etc.
    private void observe() {

    }

    /// Sort messages by importance, shoot off the most important one, then clear it out if its terminus condition has been reached
    // TODO: Add Tests
    private void handle_outgoing_communication() {
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
                if (message.terminus_met(this.reference())) {
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
    }

    /// Produce babies at a more conservative, budgeted rate.
    // TODO: Add Tests
    private void conserve() {
    }

    /// Cover as much of the map as is possible, reporting any important discoveries.
    // TODO: Add Tests
    private void explore() {
    }

    /// Read and handle each incoming squeak as is necessary.
    // TODO: Add Tests
    public void handle_incoming_communication() {
        for (Message message : this.rc.readSqueaks(-1)) {
            Communication comm = Communication.parse(message, this.reference());
            comm.handle(this.reference());
        }
    }

    /// Static function that returns a {@link DstarMap} modified to include the warning zone around a cat waypoint.
    /// @param nav_map the {@link DstarMap} to be changed.
    /// @param cat_waypoint the position of the Waypoint.
    /// @return the modified {@link DstarMap}.
    //TODO: Add Tests
    public static DstarMap return_cat_waypoint(DstarMap nav_map, MapLocation cat_waypoint) {
        return nav_map;
    }

    /// Adds records of the new Waypoint in all the places they're needed.
    /// @param cat_waypoint the position of the Waypoint.
    //TODO: Add Tests
    public void add_cat_waypoint(MapLocation cat_waypoint) {
        this.cat_waypoints.add(cat_waypoint);
        this.nav_map = return_cat_waypoint(this.nav_map, cat_waypoint);
        if (this.is_king) {
            System.out.println(this.broadcast_cat_waypoint(cat_waypoint).message);
        }
    }

    /// Adds records of the new Cheese Mine in all the places they're needed.
    /// @param cheese_mine the position of the Cheese Mine.
    //TODO: Add Tests
    public void add_cheese_mine(MapLocation cheese_mine) {
        this.cheese_mines.add(cheese_mine);
        if (this.is_king) {
            System.out.println(this.broadcast_cheese_mine(cheese_mine).message);
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
}
