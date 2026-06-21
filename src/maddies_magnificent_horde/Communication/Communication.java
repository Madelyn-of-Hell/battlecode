package maddies_magnificent_horde.Communication;

import battlecode.common.Message;
import maddies_magnificent_horde.RobotPlayer;

import java.util.Random;

/// An abstract superclass for all types of messages
public abstract class Communication {
    /// The ID of the rat who sent the message
    public int sender_id;
    /// The constant ID of the message. Overridden by each child.
    public abstract int message_id();
    /// Given the message as an integer and the shared key, returns a communication (child) object
    /// @param raw_message The message object, as pulled from ReadSqueaks
    /// @param robot a reference to the robot player (stored as array because that's how you do references in java lol)
    /// @return A fully populated child object of Communication
    public static Communication parse(Message raw_message, RobotPlayer[] robot) {
        int decrypted_message = decrypt(raw_message.getBytes(), byte_mask(robot[0].shared_key()));
        int message_id = decrypted_message >>> 27;
        return switch (message_id) { // Thank you Jetbrains Linter for informing me this is possible !!
            case 0 -> new NewRatProtocol(decrypted_message, raw_message.getSenderID());
            case 1 -> new KingAcknowledgeMessage(decrypted_message, raw_message.getSenderID());
            case 2 -> new NewRatProtocolAcknowledge(decrypted_message, raw_message.getSenderID());
            case 3 -> new RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs(decrypted_message, raw_message.getSenderID());
            case 4 -> new RatPackVolunteerToGoBackInsteadOfAttack(decrypted_message, raw_message.getSenderID());
            case 5 -> new RatPackHasNewKingToAttack(decrypted_message, raw_message.getSenderID());
            case 6 -> new RatPackGoingDark(decrypted_message, raw_message.getSenderID());
            case 7 -> new RatPackShouldAttack(decrypted_message, raw_message.getSenderID());
            case 8 -> new RatPackReassemble(decrypted_message, raw_message.getSenderID());
            case 9 -> new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack(decrypted_message, raw_message.getSenderID());
            case 10 -> new HeyYouComeJoinMyRatPackSoThatWeCanGoAttack(decrypted_message, raw_message.getSenderID());
            case 11 -> new EnemyRatKingFound(decrypted_message, raw_message.getSenderID());
            case 12 -> new CheeseMineFound(decrypted_message, raw_message.getSenderID());
            case 13 -> new CatWaypointFound(decrypted_message, raw_message.getSenderID());
            default -> throw new RuntimeException("Unknown Message: " + Integer.toBinaryString(raw_message.getBytes()) + " Decrypted: " + Integer.toBinaryString(decrypted_message) + " Shared key: " + Integer.toBinaryString(robot[0].shared_key()) + " Message ID: " + Integer.toBinaryString(message_id));
        };
    }

    /// Convert {@link RobotPlayer#shared_key()} into a mask that can be combined with the message to decrypt it.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return A 32-bit byte mask
    public static int byte_mask(int shared_key) {
        int one_byte = mask(shared_key, 8);
        return one_byte << 24 | one_byte << 16 | one_byte << 8 | one_byte;
    }

    /// Decrypt a message. Functionally identical to {@link #encrypt}, but I find it convenient for legibility etc. to have them separate.
    /// @param raw_message The encrypted message int
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return the decrypted message
    public static int decrypt(int raw_message, int shared_key) {
        return raw_message ^ byte_mask(shared_key);
    }

    /// Encrypt a message. Functionally identical to {@link #decrypt}, but I find it convenient for legibility etc. to have them separate.
    /// @param compiled_message The compiled message bytes.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return An encrypted message, ready to be broadcast.
    public static int encrypt(int compiled_message, int shared_key) {
        return compiled_message ^ byte_mask(shared_key);
    }

    /// Create a shared Key
    /// @return a 10-bit randomly generated integer for the King to broadcast to the SharedArrayBuffer
    public static int create_key() {
        return new Random().nextInt(1024);
    }

    /// Handle an incoming message appropriately. An abstract parent class for children to override
    public abstract void handle(RobotPlayer[] robot);

    /// Whether the predicate condition has been met. An abstract parent class for children to override
    /// @return The answer, as boolean.
    public abstract boolean predicate_met(RobotPlayer[] robot);

    /// Whether the terminus condition has been met. An abstract parent class for children to override
    /// @return The answer, as boolean.
    public abstract boolean terminus_met(RobotPlayer[] robot);

    /// Package the message into an integer. An abstract parent class for children to override
    /// @return an int with all data appropriately packaged. Unencrypted.
    public abstract int package_message();

    /// The public-facing method which turns a serialised Communication object into an encrypted, send-ready integer.
    /// @param robot a reference the robot—used to conveniently extract the shared key.
    public int render(RobotPlayer[] robot) {
        return encrypt(this.package_message(), robot[0].shared_key());
    }

    /// Masks an integer to the first n places.
    /// @param value the value to be masked
    /// @param places the number of places to be masked.
    /// @return the masked integer.
    public static int mask(int value, int places) {
        int mask_32 = 0b11111111111111111111111111111111;
        int shaped_mask = mask_32 >>> (32-places); // The difference between a signed and unsigned bitshift kept me here from hours and I want you to know that I Suffer For My Craft
        return value & shaped_mask;
    }

    /// Determines whether two integers are likely stems of the same root. Done because communicated IDs are potentially lossy so direct equality comparison doesn't work.
    /// @param id1 one ID to be compared
    /// @param id2 another ID to be compared
    /// @return whether or not they're likely the same ID.
    public static boolean compare_id(int id1, int id2) {
        // This is gross. However, I have no information as to a better approach
        int id1_masked = mask(id1, Integer.toBinaryString(id2).length());
        int id2_masked = mask(id2, Integer.toBinaryString(id1).length());
        if (id1_masked == id2_masked) {
            return true;
        } else {
            return false;
        }
    }
}

