package com.gerinberg.mqtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Container object for MQTT properties.
 * 
 * @author Ger Inberg
 */
public class MqttProperties {
	
	private final static Logger LOGGER = LogManager.getLogger(MqttProperties.class);
	
	public final static String JSON = "json";
	public final static String PROTO = "proto";
	public static final String BIM_001_PROTO = "bim_001.proto";
	
	//the abbreviated topic!
	private String topic;
	
	private String format;
	
	private String protoFile;
	
	private String webserviceURL;
	
	//the buffer limit
	private int bufferLimit;
	
	//current buffer size
	private int currentBufferSize;
	
	private List<OutputMessage> outputMessages = Collections.synchronizedList(new ArrayList<OutputMessage>());
	
	public MqttProperties(String topic, String format, String protoFile, String webserviceURL, String bufferLimit){
		this.topic = topic;
		this.format = format;
		this.protoFile = protoFile;
		this.webserviceURL = webserviceURL;
		this.bufferLimit = Integer.valueOf(bufferLimit);
		this.currentBufferSize = 0;
	}
	
	/** Add message to the buffer.
	 * @return if buffer is full.
	 */
	public boolean addMessageToBuffer(String topic, String message){
		//append only if buffer limit is not reached
		if (currentBufferSize < bufferLimit) {
			outputMessages.add(new OutputMessage(topic, message));
			currentBufferSize++;
			//LOGGER.debug("Topic:bufferSize " + topic + ":" + currentBufferSize);
			if (currentBufferSize == bufferLimit){
				return true;
			} else{
				return false;
			}
		} else{
			LOGGER.warn("Not adding message because buffer limit is reached!");
			return true;
		}
	}
	
	public void clearMessageBuffer(){
		this.outputMessages.clear();
		currentBufferSize = 0;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getProtoFile() {
		return protoFile;
	}

	public void setProtoFile(String protoFile) {
		this.protoFile = protoFile;
	}

	public String getWebserviceURL() {
		return webserviceURL;
	}

	public void setWebserviceURL(String webserviceURL) {
		this.webserviceURL = webserviceURL;
	}

	public int getBufferSize() {
		return currentBufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.currentBufferSize = bufferSize;
	}

	public List<OutputMessage> getOutputMessages() {
		return outputMessages;
	}

}
