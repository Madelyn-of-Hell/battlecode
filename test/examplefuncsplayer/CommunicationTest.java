package examplefuncsplayer;

import static org.junit.Assert.*;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import examplefuncsplayer.Communication.*;
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
	CommunicationInterface[] self = new CommunicationInterface[]{new CommunicationInterface(1, RobotProtocol.None)};
	message.handle(self);
	CommunicationInterface[] correct_answer = new CommunicationInterface[]{new CommunicationInterface(1, RobotProtocol.None)};
	assertEquals(self[0], correct_answer[0]);
	}
	@Test
	public void TestHandleNewRatProtocolAccept() {
		NewRatProtocol message = new NewRatProtocol();
		message.target_rat_id = 1;
		message.prescribed_protocol = RobotProtocol.Gather;
		CommunicationInterface[] self = new CommunicationInterface[]{new CommunicationInterface(100, RobotProtocol.None)};
		message.handle(self);
		CommunicationInterface[] correct_answer = new CommunicationInterface[]{new CommunicationInterface(100, RobotProtocol.Gather)};
		assertEquals(self[0], correct_answer[0]);
	}

}
