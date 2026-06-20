package examplefuncsplayer.Communication;

import examplefuncsplayer.RobotPlayer;
import examplefuncsplayer.RobotProtocol;

public class HeyYouComeJoinMyRatPackSoThatWeCanGoAttack extends Communication {
    public static final int message_id = 10;

    public int pack_size;
    public int pack_id;

    public HeyYouComeJoinMyRatPackSoThatWeCanGoAttack(int decryptedMessage, int sender_id) {
        this.pack_size = mask(decryptedMessage >>> 18, 8);
        this.pack_id = mask(decryptedMessage, 18);
        this.sender_id = sender_id;
    }


    @Override
    public void handle(RobotPlayer[] robot) {
        if ( // Sometimes nice formatting is fun :3
                robot[0].current_protocol() == RobotProtocol.Attack &&
                robot[0].pack_id() != this.pack_id &&
                this.pack_size > robot[0].pack_size()
        ) {
            robot[0].join_pack(this.pack_id);
        }
    }

    @Override
    public boolean predicate_met(RobotPlayer[] robot) {
        return true;
    }

    @Override
    public boolean terminus_met(RobotPlayer[] robot) {
        return (robot[0].pack_size() > RobotPlayer.PACK_ATTACK_SIZE) ||
                (robot[0].terminus_messages().contains(new TerminusMessage(TerminusMessageType.RatPackHasChanged, 0)));
    }

    @Override
    public int package_message() {
        return message_id << 27 | Communication.mask(pack_size, 13) << 14 | Communication.mask(pack_id, 13) << 1;
    }
}
