package examplefuncsplayer;

import static org.junit.Assert.*;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import examplefuncsplayer.Communication.Communication;
import examplefuncsplayer.Communication.NewRatProtocol;
import org.junit.Before;
import org.junit.Test;
import scala.reflect.internal.Trees;

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

}
