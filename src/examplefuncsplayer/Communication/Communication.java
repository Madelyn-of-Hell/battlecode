package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import examplefuncsplayer.RobotPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/// An abstract superclass for all types of messages
public abstract class Communication {

    public int sender_id;
    public static int message_id;
    public MapLocation source_tile;

    /// Given the message as an integer and the shared key, returns a communication (child) object
    /// @param raw_message The message object, as pulled from ReadSqueaks
    /// @param self a reference to the robot player (stored as array because that's how you do references in java lol)
    /// @return A fully populated child object of Communication
    public static Communication parse(Message raw_message, RobotPlayer[] self) {
        int decrypted_message = decrypt(raw_message.getBytes(), byte_mask(self[0].shared_key));
        int message_id = decrypted_message >>> 27;
        return switch (message_id) { // Thank you Jetbrains Linter for informing me this is possible !!
            case 1 -> new NewRatProtocol(decrypted_message);
            case 2 -> new KingAcknowledgeMessage(decrypted_message);
            case 3 -> new NewRatProtocolAcknowledge(decrypted_message);
            case 4 -> new RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(decrypted_message);
            case 5 -> new RatPackVolunteerToGoBackInsteadOfAttack(decrypted_message);
            case 6 -> new RatPackHasNewKingToAttack(decrypted_message);
            case 7 -> new RatPackGoingDark(decrypted_message);
            case 8 -> new RatPackShouldAttack(decrypted_message);
            case 9 -> new RatPackReassemble(decrypted_message);
            case 10 -> new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(decrypted_message);
            case 11 -> new HeyYouComeJoinMyRatPackSoThatWeCanGoAttack(decrypted_message);
            case 12 -> new EnemyRatKingFound(decrypted_message);
            case 13 -> new CheeseMineFound(decrypted_message);
            case 14 -> new CatWaypointFound(decrypted_message);
            default -> throw new RuntimeException("Unreachable");
        };
    }

    /// Convert shared key to a mask that can be combined with the message to decrypt it.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return A 32-bit byte mask
    public static int byte_mask(int shared_key) {
        return shared_key << 24 | shared_key << 16 | shared_key << 8 | shared_key;
    }

    /// Decrypt a message. Functionally identical to `encrypt`, but I find it convenient for legibility etc. to have them separate.
    /// @param raw_message The encrypted message int
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return the decrypted message
    public static int decrypt(int raw_message, int shared_key) {
        return raw_message ^ shared_key;
    }

    /// Encrypt a message. Functionally identical to `decrypt`, but I find it convenient for legibility etc. to have them separate.
    /// @param compiled_message The compiled message bytes.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return An encrypted message, ready to be broadcast.
    public static int encrypt(int compiled_message, int shared_key) {
        return compiled_message ^ shared_key;
    }

    /// Create a shared Key
    /// @return a 10-bit randomly generated integer for the King to broadcast to the SharedArrayBuffer
    public static int create_key() {
        return new Random().nextInt(1024);
    }
    /// Handle an incoming message appropriately. An abstract parent class for children to override
    public abstract void handle(RobotPlayer[] interface_array);

    /// Whether the predicate condition has been met. An abstract parent class for children to override
    /// @return The answer, as boolean. r u stupid?
    public abstract boolean predicate_met(RobotPlayer[] interface_array);

    /// Whether the terminus condition has been met. An abstract parent class for children to override
    /// @return The answer, as boolean. r u stupid?
    public abstract boolean terminus_met(RobotPlayer[] interface_array);
    /// Package the message into an integer. An abstract parent class for children to override
    /// @return an int with all data appropriately packaged. Unencrypted.
    public abstract int package_message();

    public int render(RobotPlayer[] interface_array) {
        return encrypt(this.package_message(), interface_array[0].shared_key);
    }

    /// Tiny little helper function to mask bits to make the rest of the work easier
    public static int mask(int value, int places) {
        int mask_32 = 0b11111111111111111111111111111111;
        int shaped_mask = mask_32 >>> (32-places); // The difference between a signed and unsigned bitshift kept me here from hours and I want you to know that I Suffer For My Craft
        return value & shaped_mask;
    }
}

