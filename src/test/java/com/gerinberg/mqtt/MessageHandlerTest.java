package com.gerinberg.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;

public class MessageHandlerTest {
	
	byte[] payload = new byte[] {8, 1, 18, 29, 10, 27, 8, 0, 18, 2, 8, 0, 26, 14, 8, 0, 16, -50, 1, 24, 10, 32, -128, -106, 1, 40, -5, 51, 69, 0, 104, -125, 69};
	
	@Test
	public void testCreateWebserviceMessageJson() {
		MessageHandler messageHandler = new MessageHandler();
		List<OutputMessage> outputMessages = new ArrayList<OutputMessage>();
		outputMessages.add(new OutputMessage("OBU/1/topic1", "message1"));
		outputMessages.add(new OutputMessage("OBU/1/topic2", "message2"));
		outputMessages.add(new OutputMessage("OBU/1/topic3", "message3"));
		String result = messageHandler.createWebserviceMessage(outputMessages);
		
		String expected = "[{ \"topic\": \"OBU/1/topic1\", \"message\": message1},"
				+ "{ \"topic\": \"OBU/1/topic2\", \"message\": message2},"
				+ "{ \"topic\": \"OBU/1/topic3\", \"message\": message3}]";
		Assert.assertEquals(expected, result);
	}
	
//	@Test
//	public void testEmptyJsonMessage() {
//		Bim001.Event event = Bim001.Event.getDefaultInstance();
//		String jsonFormat = JsonFormat.printToString(event);
//		System.out.println(jsonFormat);		
//		Assert.assertEquals("{}", jsonFormat);
//	}
//	
//	@Test
//	public void testJsonMessage() throws InvalidProtocolBufferException {
//		Bim001.Event event = Bim001.Event.parseFrom(payload);
//		String jsonFormat = JsonFormat.printToString(event);
//		System.out.println(jsonFormat);		
//		Assert.assertEquals("{\"type\": \"TRACK\",\"track\": {\"basicInfo\": {\"timestamp\": 0,\"gpsInfo\": {\"nrSats\": 0},\"gsmInfo\": {\"signalStrength\": 0,\"mcc\": 206,\"mnc\": 10,\"lac\": 19200,\"cellid\": 6651},\"batteryVoltage\": 4205.0}}}", jsonFormat);
//	}

}
