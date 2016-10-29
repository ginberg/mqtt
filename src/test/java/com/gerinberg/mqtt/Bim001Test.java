package com.gerinberg.mqtt;

public class Bim001Test {
	
	byte[] payload = new byte[] {8, 1, 18, 29, 10, 27, 8, 0, 18, 2, 8, 0, 26, 14, 8, 0, 16, -50, 1, 24, 10, 32, -128, -106, 1, 40, -5, 51, 69, 0, 104, -125, 69};

//	@Test
//	public void testParseEvent() {
//		try {
//			Event event = Bim001.Event.parseFrom(payload);
//			Assert.assertEquals(Bim001.Event.Type.TRACK, event.getType());	
//		} catch (InvalidProtocolBufferException e) {
//			Assert.fail(e.getMessage());
//		}
//	}

}
