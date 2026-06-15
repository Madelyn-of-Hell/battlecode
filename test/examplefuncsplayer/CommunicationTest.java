package examplefuncsplayer;

import static org.junit.Assert.*;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import examplefuncsplayer.Communication.*;
import examplefuncsplayer.dstar.DstarMap;
import org.junit.Test;

import java.awt.*;

public class CommunicationTest {


	final byte key = 0b01011010;
	final int shared_mask = 0b01011010010110100101101001011010;
	final int demo_rat_id = 0b0010110101101011011101011110100000;

	/// Tiny little helper function to mask bits to make the rest of the work easier
	public int mask(int value, int places) {
		int mask_32 = 0b11111111111111111111111111111111;
		int shaped_mask = mask_32 >>> (32-places); // The differen
		// ce between a signed and unsigned bitshift kept me here from hours and I want you to know that I Suffer For My Craft
		return value & shaped_mask;
	}
	@Test //but u know i gotta test it anyways lol
	public void TestMask() {
		int unmasked_value = 0b11101011010101010101111001010011;
		assertEquals(0b1001010011, mask(unmasked_value, 10));
		assertEquals(0b1111001010011, mask(unmasked_value, 13));
	}

	@Test
	public void TestByteMask() {
		final int correct_answer = 0b01011010010110100101101001011010;

		int result = Communication.byte_mask(key);
		assertEquals(correct_answer, result);
	}

	@Test
	public void TestEncrypt() {
		final int unencrypted_message = 0b01100011101010110110110100011101;
		final int correct_answer = unencrypted_message ^ shared_mask;

		int result = Communication.encrypt(unencrypted_message,key);
		assertEquals(correct_answer,result);
	}
	@Test
	public void TestDecrypt() {
		final int encrypted_message = 0b01100011101010110110110100011101;
		final int correct_answer = encrypted_message ^ shared_mask;

		int result = Communication.encrypt(encrypted_message,key);
		assertEquals(correct_answer,result);
	}

	/////////////////////////////NewRatProtocol/////////////////////////////
	@Test
	public void TestPackageNewRatProtocol() {
		final int message_type = NewRatProtocol.message_id;
		final RobotProtocol new_protocol = RobotProtocol.Gather;
		final int correct_answer = message_type << 27 | new_protocol.value << 25 | mask(demo_rat_id, 25);

		NewRatProtocol message = new NewRatProtocol();
		message.prescribed_protocol = new_protocol;
		message.target_rat_id = demo_rat_id;

		assertEquals(correct_answer, message.package_message());
	}

	@Test
	public void TestParseNewRatProtocol() {
	final int message_type = NewRatProtocol.message_id;
	final RobotProtocol new_protocol = RobotProtocol.Gather;
	final int packaged_message = message_type << 27 | new_protocol.value << 25 | mask(demo_rat_id, 25);
	final int encrypted_message = packaged_message ^ shared_mask;

	final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));
	final NewRatProtocol correct_answer = new NewRatProtocol();
	correct_answer.prescribed_protocol = new_protocol;
	correct_answer.target_rat_id = demo_rat_id;
	correct_answer.sender_id = 1;


	Communication output = Communication.parse(message, key);
	assertEquals(output, correct_answer);
	}

	@Test
	public void TestHandleNewRatProtocolIgnore() {
		NewRatProtocol message = new NewRatProtocol();
	message.target_rat_id = demo_rat_id;
	message.prescribed_protocol = RobotProtocol.Gather;
	RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
			1,
			RobotProtocol.None,
			false,
			30,30,
			null
	)};
	message.handle(self);
	assertEquals(
			RobotProtocol.None,
			self[0].current_protocol
	);
	}

	@Test
	public void TestHandleNewRatProtocolAccept() {
		NewRatProtocol message = new NewRatProtocol();
		message.target_rat_id = 100;
		message.prescribed_protocol = RobotProtocol.Gather;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30,30,
				null
		)};
		message.handle(self);
		assertEquals(
				RobotProtocol.Gather,
				self[0].current_protocol
		);
	}

	/////////////////////////KingAcknowledgeMessage/////////////////////////
	@Test
	public void TestPackageKingAcknowledgeMessage() {
		final int message_type = KingAcknowledgeMessage.message_id;
		final int received_message_type = CatWaypointFound.message_id;
		final int correct_answer = message_type << 27 | received_message_type << 22 | mask(demo_rat_id, 22);

		KingAcknowledgeMessage message = new KingAcknowledgeMessage();
		message.acknowledged_message_type = received_message_type;
		message.target_rat_id = demo_rat_id;

		assertEquals(correct_answer, message.package_message());
	}

	@Test
	public void TestParseKingAcknowledgeMessage() {
		final byte message_type = KingAcknowledgeMessage.message_id;
		final int received_message_type = CatWaypointFound.message_id;
		final int packaged_message = message_type << 27 | received_message_type << 22 | mask(demo_rat_id, 22);
		final int encrypted_message = packaged_message ^ shared_mask;
		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));
		final KingAcknowledgeMessage correct_answer = new KingAcknowledgeMessage();
		correct_answer.acknowledged_message_type = received_message_type;
		correct_answer.target_rat_id = demo_rat_id;
		correct_answer.sender_id = 1;


		Communication output = Communication.parse(message, key);
		assertEquals(output, correct_answer);
	}

	@Test
	public void TestHandleKingAcknowledgeMessageIgnore() {
		KingAcknowledgeMessage message = new KingAcknowledgeMessage() ;
		message.target_rat_id = 1;
		message.acknowledged_message_type = CatWaypointFound.message_id;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30,30,
				null
		)};
		message.handle(self);
		assertEquals(0, self[0].terminusMessages.length);
	}

	@Test
	public void TestHandleKingAcknowledgeMessageAccept() {
		KingAcknowledgeMessage message = new KingAcknowledgeMessage() ;
		message.target_rat_id = 100;
		message.acknowledged_message_type = CatWaypointFound.message_id;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30, 30,
				null
		)};
		message.handle(self);
		assertEquals(
				new TerminusMessage(TerminusMessageType.KingAcknowledgeMessage, CatWaypointFound.message_id),
				self[0].terminusMessages[0]
		);
	}

	////////////////////////NewRatProtocolAcknowledge///////////////////////
	@Test
	public void TestPackageNewRatProtocolAcknowledge() {
		final int message_type = NewRatProtocolAcknowledge.message_id;
		final RobotProtocol new_protocol = RobotProtocol.Gather;
		final int correct_answer = message_type << 27 | new_protocol.value << 25 ;

		NewRatProtocolAcknowledge message = new NewRatProtocolAcknowledge();
		message.protocol = new_protocol;

		assertEquals(correct_answer, message.package_message());
	}

	@Test
	public void TestParseNewRatProtocolAcknowledge() {
		final int message_type = NewRatProtocolAcknowledge.message_id;
		final RobotProtocol new_protocol = RobotProtocol.Gather;
		final int packaged_message = message_type << 27 | new_protocol.value << 25;
		final int encrypted_message = packaged_message ^ shared_mask;

		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));
		final NewRatProtocolAcknowledge correct_answer = new NewRatProtocolAcknowledge();
		correct_answer.protocol = new_protocol;
		correct_answer.sender_id = 1;


		Communication output = Communication.parse(message, key);
		assertEquals(output, correct_answer);
	}

	@Test
	public void TestHandleNewRatProtocolAcknowledgeIgnore() {
		NewRatProtocolAcknowledge message = new NewRatProtocolAcknowledge() ;
		message.protocol = RobotProtocol.Gather;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.Explore,
				false,
				30, 30,
				null
		)};

		message.handle(self);

		assertEquals(
				0,
				self[0].terminusMessages.length
		);
	}

	@Test
	public void TestHandleNewRatProtocolAcknowledgeAccept() {
		NewRatProtocolAcknowledge message = new NewRatProtocolAcknowledge() ;
		message.protocol = RobotProtocol.Gather;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.Propagate,
				true,
				30,30,
				null
		)};

		message.handle(self);
		assertEquals(
				new TerminusMessage(TerminusMessageType.NewRatProtocolAcknowledge, RobotProtocol.Gather.value),
				self[0].terminusMessages[0]
		);
	}

	////////////////////////////CatWaypointFound////////////////////////////
	@Test
	public void TestPackageCatWaypointFound() {
		final int message_type = CatWaypointFound.message_id;
		final MapLocation waypoint_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(waypoint_position.x, 6) << 21 | mask(waypoint_position.y, 6) << 15;
		final CatWaypointFound message = new CatWaypointFound();
		message.waypoint_position = waypoint_position;
		assertEquals(correct_answer, message.package_message());
	}
	@Test
	public void TestParseCatWaypointFound() {
		final int message_type = CatWaypointFound.message_id;
		final MapLocation waypoint_position = MapLocation.valueOf("40,22");
		final int packaged_message = message_type << 27 | mask(waypoint_position.x, 6) << 21 | mask(waypoint_position.y, 6) << 15;
		final int encrypted_message = packaged_message ^ shared_mask;

		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));
		final CatWaypointFound correct_answer = new CatWaypointFound();
		correct_answer.waypoint_position = waypoint_position;
		correct_answer.sender_id = 1;


		Communication output = Communication.parse(message, key);
		assertEquals(correct_answer, output);
	}
	@Test
	public void TestHandleCatWaypointFoundIgnore() {
		CatWaypointFound message = new CatWaypointFound() ;
		message.waypoint_position = MapLocation.valueOf("40,22");
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30,
				30,
				null
		)};
		self[0].cat_waypoints = new MapLocation[]{MapLocation.valueOf("40,22")};
		message.handle(self);

		assertEquals(
				new DstarMap(30,30),
				self[0].nav_map
		);
		assertEquals(
					1,
				self[0].cat_waypoints.length
		);
	}
	@Test
	public void TestHandleCatWaypointFoundAccept() {
		CatWaypointFound message = new CatWaypointFound() ;
		message.waypoint_position = MapLocation.valueOf("40,22");
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30,
				30,
				null
		)};
		message.handle(self);

		DstarMap correct_map = RobotPlayer.return_cat_waypoint(new DstarMap(30,30), MapLocation.valueOf("40,22"));
		assertEquals(
				correct_map,
				self[0].nav_map
		);
		assertEquals(
				1,
				self[0].cat_waypoints.length
		);
	}

	/////////////////////////////CheeseMineFound////////////////////////////
	@Test
	public void TestPackageCheeseMineFound() {
		final int message_type = CheeseMineFound.message_id;
		final MapLocation mine_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(mine_position.x, 6) << 21 | mask(mine_position.y, 6) << 15;
		final CheeseMineFound message = new CheeseMineFound();
		message.mine_position = mine_position;
		assertEquals(correct_answer, message.package_message());
	}
	@Test
	public void TestParseCheeseMineFound() {
		final int message_type = CheeseMineFound.message_id;
		final MapLocation mine_position = MapLocation.valueOf("40,22");
		final int packaged_message = message_type << 27 | mask(mine_position.x, 6) << 21 | mask(mine_position.y, 6) << 15;
		final int encrypted_message = packaged_message ^ shared_mask;
		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));
		final CheeseMineFound correct_answer = new CheeseMineFound();
		correct_answer.mine_position = mine_position;

		Communication output = Communication.parse(message, key);
		assertEquals(correct_answer, output);
	}
	@Test
	public void TestHandleCheeseMineFoundIgnore() {
		CheeseMineFound message = new CheeseMineFound() ;
		message.mine_position = MapLocation.valueOf("40,22");
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30, 30,
				null
		)};
		self[0].cheese_mines = new MapLocation[]{MapLocation.valueOf("40,22")};
		message.handle(self);

		assertEquals(
				1,
				self[0].cheese_mines.length
		);
	}

	@Test
	public void TestHandleCheeseMineFoundAdd() {
		CheeseMineFound message = new CheeseMineFound() ;
		message.mine_position = MapLocation.valueOf("40,22");
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
				100,
				RobotProtocol.None,
				false,
				30, 30,
				null
		)};
		self[0].cheese_mines = new MapLocation[]{MapLocation.valueOf("21,21")};
		message.handle(self);

		assertEquals(
				2,
				self[0].cheese_mines.length
		);
	}
	////////////////////////////EnemyRatKingFound///////////////////////////
	@Test
	public void TestPackageEnemyRatKingFound() {
		final int message_type = EnemyRatKingFound.message_id;
		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(demo_rat_id, 15);
		final CatWaypointFound message = new CatWaypointFound();
		message.waypoint_position = king_position;
		assertEquals(correct_answer, message.package_message());
	}
	@Test
	public void TestParseEnemyRatKingFound() {
		final int message_type = EnemyRatKingFound.message_id;

		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int king_id = demo_rat_id;

		final int packaged_message = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(king_id, 15);
		final int encrypted_message = packaged_message ^ shared_mask;
		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));

		final EnemyRatKingFound correct_answer = new EnemyRatKingFound();
		correct_answer.king_position = king_position;
		correct_answer.king_id = king_id;

		Communication output = Communication.parse(message, key);
		assertEquals(correct_answer, output);
	}

	///////////////HeyYouComeJoinMyRatPackSoThatWeCanGoAttack///////////////
	@Test
	public void TestPackageHeyYouComeJoinMyRatPackSoThatWeCanGoAttack() {
		final int message_type = HeyYouComeJoinMyRatPackSoThatWeCanGoAttack.message_id;
		final int pack_size = 0b1110100100011;
		final int correct_answer = message_type << 27 | mask(pack_size, 13) << 14 | mask(demo_rat_id, 13) << 1;
		final HeyYouComeJoinMyRatPackSoThatWeCanGoAttack message = new HeyYouComeJoinMyRatPackSoThatWeCanGoAttack();
		message.pack_size = pack_size;
		message.pack_id = demo_rat_id;
		assertEquals(correct_answer, message.package_message());
	}
	@Test
	public void TestParseHeyYouComeJoinMyRatPackSoThatWeCanGoAttack() {
		final int message_type = HeyYouComeJoinMyRatPackSoThatWeCanGoAttack.message_id;

		final int pack_size = 20;
		final int pack_id = ___;

		final int packaged_message = message_type << 27 | mask(param_one, BIT_ALLOTMENT_ONE) << BITSHIFT_ONE | mask(param_two, BIT_ALLOTMENT_TWO) << BITSHIFT_TWO;
		final int encrypted_message = packaged_message ^ shared_mask;
		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));

		final HeyYouComeJoinMyRatPackSoThatWeCanGoAttack correct_answer = new HeyYouComeJoinMyRatPackSoThatWeCanGoAttack();
		correct_answer.param_one = param_one;
		correct_answer.param_two = param_two;

		Communication output = Communication.parse(message, key);
		assertEquals(correct_answer, output);
	}

	////////////WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack///////////
	@Test
	public void TestPackageWaowieYourRatPackIsSoBigIWannaComeWithYouToAttack() {
		final int message_type = WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack.message_id;
		final int old_pack_id = 0b1110100100011;
		final int correct_answer = message_type << 27 | mask(old_pack_id, 13) << 14 | mask(demo_rat_id, 13) << 1;
		final WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack message = new WaowieYourRatPackIsSoBigIWannaComeWithYouToAttack();
		message.old_pack_id = old_pack_id;
		message.new_pack_id = demo_rat_id;
		assertEquals(correct_answer, message.package_message());
	}

	///////////////////////////RatPackShouldAttack//////////////////////////
	@Test
	public void TestPackageRatPackShouldAttack() {
		final int message_type = RatPackShouldAttack.message_id;
		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(demo_rat_id, 15);
		final RatPackShouldAttack message = new RatPackShouldAttack();
		message.victim_pos = king_position;
		assertEquals(correct_answer, message.package_message());
	}

	////////////////////////RatPackHasNewKingToAttack///////////////////////
	@Test
	public void TestPackageRatPackHasNewKingToAttack() {
		final int message_type = RatPackHasNewKingToAttack.message_id;
		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(demo_rat_id, 15);
		final RatPackHasNewKingToAttack message = new RatPackHasNewKingToAttack();
		message.new_king_loc = king_position;
		assertEquals(correct_answer, message.package_message());
	}

	/////////////////RatPackVolunteerToGoBackInsteadOfAttack////////////////
	@Test
	public void TestPackageRatPackVolunteerToGoBackInsteadOfAttack() {
		final int message_type = RatPackVolunteerToGoBackInsteadOfAttack.message_id;
		final int correct_answer = message_type << 27 | mask(demo_rat_id, 27);
		final RatPackVolunteerToGoBackInsteadOfAttack message = new RatPackVolunteerToGoBackInsteadOfAttack();
		assertEquals(correct_answer, message.package_message());
	}

	///////////RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs//////////
	@Test
	public void TestPackageRatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs() {
		final int message_type = RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs.message_id;
		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(demo_rat_id, 15);
		final RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs message = new RatPackHeyHiHowAreYouWeKilledTheKingAreYouProudOfUs();
		message.corpse_pos = king_position;
		message.corpse_id = demo_rat_id;
		assertEquals(correct_answer, message.package_message());
	}

	////////////////////////////RatPackGoingDark////////////////////////////
	@Test
	public void TestPackageRatPackGoingDark() {
		final int message_type = RatPackGoingDark.message_id;
		final int correct_answer = message_type << 27 | mask(demo_rat_id, 27);

		final RatPackGoingDark message = new RatPackGoingDark();
		message.pack_id = demo_rat_id;
		assertEquals(correct_answer, message.package_message());
	}

	////////////////////////////RatPackReassemble///////////////////////////
	@Test
	public void TestPackageRatPackReassemble() {
		final int message_type = RatPackReassemble.message_id;
		final MapLocation king_position = MapLocation.valueOf("40,22");
		final int correct_answer = message_type << 27 | mask(king_position.x, 6) << 21 | mask(king_position.y, 6) << 15 | mask(demo_rat_id, 15);
		final RatPackReassemble message = new RatPackReassemble();
		message.victim_pos = king_position;
		message.victim_id = demo_rat_id;
		assertEquals(correct_answer, message.package_message());
	}

	public void TestParseTemplate() {
		final int message_type = MESSAGETYPE.message_id;

		final PARAM_ONE_TYPE param_one = ___;
		final PARAM_TWO_TYPE param_two = ___;

		final int packaged_message = message_type << 27 | mask(param_one, BIT_ALLOTMENT_ONE) << BITSHIFT_ONE | mask(param_two., BIT_ALLOTMENT_TWO) << BITSHIFT_TWO;
		final int encrypted_message = packaged_message ^ shared_mask;
		final Message message = new Message(encrypted_message, 1, 1, MapLocation.valueOf("1,1"));

		final MESSAGETYPE correct_answer = new MESSAGETYPE();
		correct_answer.param_one = param_one;
		correct_answer.param_two = param_two;

		Communication output = Communication.parse(message, key);
		assertEquals(correct_answer, output);
	}

	public void TestHandleTemplate() {
		MESSAGETYPE message = new MESSAGETYPE() ;
		message.param_one = ___;
		message.param_two = ___;
		RobotPlayer[] self = new RobotPlayer[]{new RobotPlayer(
			100,
				RobotProtocol.PROTOCOL
		)};

		message.handle(self);

		assertEquals(
				SUCCESS_CONDITION,
				self.EVIDENCE_OF_SUCCESS
		);
	}
}


