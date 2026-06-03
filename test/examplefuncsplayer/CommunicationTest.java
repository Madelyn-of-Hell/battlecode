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
	@Test
	public void TestByteMask() {
		final int correct_answer = 0b01011010010110100101101001011010;

		int result = Communication.byte_mask(key);
		assertEquals(correct_answer, result);
	}

	@Test
	public void TestEncrypt() {
		final int unencrypted_message = 0b01100011101010110110110100011101;
		final int correct_answer = 0b00111001111100010011011101000111;

		int result = Communication.encrypt(unencrypted_message,key);
		assertEquals(correct_answer,result);
	}
	@Test
	public void TestDecrypt() {
		final int unencrypted_message = 0b01100011101010110110110100011101;
		final int correct_answer = 0b00111001111100010011011101000111;

		int result = Communication.encrypt(unencrypted_message,key);
		assertEquals(correct_answer,result);
	}

}
