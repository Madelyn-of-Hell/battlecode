package examplefuncsplayer.Communication;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import examplefuncsplayer.RobotPlayer;

/// An abstract superclass for all types of messages
public abstract class Communication {

    public int sender_id;
    public MapLocation source_tile;

    /// Given the message as an integer and the shared key, returns a communication (child) object
    /// @param raw_message The message object, as pulled from ReadSqueaks
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return A fully populated child object of Communication
    public static Communication parse(Message raw_message, byte shared_key) {
        return new CatWaypointFound();
    }

    abstract Communication _sub_parse(int message_data);
    /// Convert shared key to a mask that can be combined with the message to decrypt it.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return A 32-bit byte mask
    public static int byte_mask(byte shared_key) {
        return 1;
//        return shared_key << 24 | shared_key << 16 | shared_key << 8 | shared_key;
    }

    /// Decrypt a message. Functionally identical to `encrypt`, but I find it convenient for legibility etc. to have them separate.
    /// @param raw_message The encrypted message int
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return the decrypted message
    public static int decrypt(int raw_message, byte shared_key) {
        return 1;
    }

    /// Encrypt a message. Functionally identical to `decrypt`, but I find it convenient for legibility etc. to have them separate.
    /// @param compiled_message The compiled message bytes.
    /// @param shared_key The shared key, as pulled from the SharedBufferArray.
    /// @return An encrypted message, ready to be broadcast.
    public static int encrypt(int compiled_message, int shared_key) {
        return 1;
    }

    /// Create a shared Key
    /// @return a 10-bit randomly generated integer for the King to broadcast to the SharedArrayBuffer
    public static int create_key() {
        byte_mask((byte) 0b01101010);
        return 1;
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

}

