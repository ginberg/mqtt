package com.gerinberg.mqtt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/** Handles messages to webservice.
 * 
 * @author Ger Inberg.
 */
public class MessageHandler {

	private final static Logger LOGGER = LogManager.getLogger(Forwarder.class);
	
	private final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
	
	/**
	 * @param originalTopic
	 * @param topic
	 * @param mqttMessage
	 * @param mqqtProerties
	 */
	public void handleMessage(String originalTopic, String topic, MqttMessage mqttMessage, MqttProperties mqqtProerties){
		String message = null;
		if (mqqtProerties.getFormat().equals(MqttProperties.JSON)) {
			message = mqttMessage.toString();
		} else if (mqqtProerties.getFormat().equals(MqttProperties.PROTO)) {
			message = getJsonFromMessage(mqttMessage.getPayload(), mqqtProerties.getProtoFile());
		}
		String messageWithoutSpaces = StringUtils.deleteWhitespace(message);
		// store message in buffer and send to webservice if buffer is full
		boolean result = mqqtProerties.addMessageToBuffer(topic, messageWithoutSpaces);
		if (result) {
			// Sending to webservice
			List<OutputMessage> messageList = mqqtProerties.getOutputMessages();
			String outputMessage = createWebserviceMessage(messageList);
			sendMessage(mqqtProerties.getWebserviceURL(), originalTopic, outputMessage);
			mqqtProerties.clearMessageBuffer();
		}
	}
	
	/**
	 * @param payload
	 * @param protoFile
	 * @return json message.
	 */
	public String getJsonFromMessage(byte[] payload, String protoFile){
		String result = null;
		if (protoFile.equals(MqttProperties.BIM_001_PROTO)){
//			Event event;
//			try {
//				event = Bim001.Event.parseFrom(payload);
//				result = JsonFormat.printToString(event);
//			} catch (InvalidProtocolBufferException e) {
//				LOGGER.error(e.getMessage());
//			}
			Object event = null;
			try {
				Class[] paramByteArray = new Class[1];
				paramByteArray[0] = byte[].class;
				Class eventClass = Class.forName("com.gerinberg.mqtt.bim_001$Event");
				Method method = eventClass.getDeclaredMethod("parseFrom", paramByteArray);
				event = method.invoke(null, payload);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return JsonFormat.printToString((Message) event);
			
		} else{
			LOGGER.warn("Unsupported protofile:" + protoFile);
		}
		return result;
		
	}
	
	/**
	 * Create the output message for given topic and messagelist.
	 * 
	 * @param topic
	 * @param messageList
	 * @return the output message string.
	 */
	public String createWebserviceMessage(List<OutputMessage> messageList) {
		StringBuffer result = new StringBuffer(100);
		result.append("[");
		for (int i = 0; i < messageList.size(); i++) {
			OutputMessage outputMessage = messageList.get(i);
			result.append("{ \"topic\": \"").append(outputMessage.getTopic()).append("\", \"message\": ")
					.append(outputMessage.getMessage()).append("}");
			if (i != messageList.size() - 1) {
				result.append(",");
			}
		}
		result.append("]");
		return result.toString();
	}
	

	/**
	 * @param urlString
	 * @param topic
	 * @param message
	 */
	private void sendMessage(String urlString, String topic, String message) {
		LOGGER.debug("Topic: " + topic + " sending message:" + message);

		BoundRequestBuilder builder = asyncHttpClient.preparePost(urlString);
		builder.addHeader("Content-Type", "application/json");
		builder.addHeader("User-Agent", "forwarding_service");
		builder.setBody(message);

		builder.execute(new AsyncCompletionHandler<Response>() {

			@Override
			public Response onCompleted(Response response) throws Exception {
				LOGGER.info("Webservice response for topic: " + topic + " : " + response.getStatusText() + " (" + response.getStatusCode() + ")" );
				return response;
			}

			@Override
			public void onThrowable(Throwable t) {
				LOGGER.error(t.getMessage());
			}
		});
	}

}
