package com.gerinberg.mqtt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Forwarding service which forwards messages from a MQTT service to a
 * Webservice.
 * 
 * @author Ger Inberg
 */
public class Forwarder implements MqttCallback {

	private final static Logger LOGGER = LogManager.getLogger(Forwarder.class);
	public static final String BROKER = "tcp://broker.ec-is.be:1883";
	public static final String CLIENT_ID = "FORWARDING_SERVICE";
	public static final String PROPERTY_FILENAME = "config.properties";

	private Map<String, MqttProperties> mqttPropertiesMap;
	private final MessageHandler webserviceHandler = new MessageHandler();

	public Forwarder() {
		mqttPropertiesMap = new HashMap<String, MqttProperties>();
	}

	/**
	 * Start the forwarding service.
	 */
	public void start() {
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			LOGGER.info("Forwarding service, start mqttclient");
			MqttAsyncClient client = new MqttAsyncClient(BROKER, CLIENT_ID, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			LOGGER.info("Connecting to broker: " + BROKER);
			client.connect(null, new IMqttActionListener() {
				
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                	LOGGER.info("Connected to broker: " + BROKER);
                    try {
                    	for (String topic : mqttPropertiesMap.keySet()) {
            				LOGGER.info("Subscribing to: " + topic);
	                        client.subscribe(topic,1, null, new IMqttActionListener() {
	                            @Override
	                            public void onSuccess(IMqttToken asyncActionToken) {
	                            	LOGGER.trace("Subscribed to:" + topic);
	                            }
	
	                            @Override
	                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
	                            	LOGGER.error(exception.getMessage());
	                            }
	                        });
                    	}

                    } catch (MqttException e) {
                    	LOGGER.error(e.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                	LOGGER.error(exception.getMessage());
                }
            });
			client.setCallback(this);
		} catch (MqttException me) {
			LOGGER.error(me.getMessage());
		}
	}

	/**
	 * Read properties and store them in mqttPropertiesMap.
	 */
	private void readproperties() {
		LOGGER.info("Reading properties");
		PropertiesReader propReader = new PropertiesReader(PROPERTY_FILENAME);
		try {
			Map<String, String[]> properties = propReader.getPropValues();
			printProperties(properties);
			for (Map.Entry<String, String[]> entry : properties.entrySet()) {
				String key = entry.getKey();
				String[] values = entry.getValue();
				MqttProperties mqttProperties = new MqttProperties(key, values[0], values[1], values[2], values[3]);
				mqttPropertiesMap.put(key, mqttProperties);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			LOGGER.info("Forwarding service is stopping");
			System.exit(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.
	 * Throwable)
	 */
	@Override
	public void connectionLost(Throwable cause) {
		LOGGER.warn("Connection lost: " + cause.getMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.
	 * String, org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		LOGGER.trace("Topic: " + topic + " ,message:" + StringUtils.deleteWhitespace(mqttMessage.toString()));
		//Get the abbreviated topic from given topic
		String originalTopic = getOriginalTopic(topic);
		webserviceHandler.handleMessage(originalTopic, topic, mqttMessage, mqttPropertiesMap.get(originalTopic));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.
	 * paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		LOGGER.debug("Delivery complete: ");
	}

	/**
	 * Retrieve the original topic, it might be shortened.
	 * 
	 * @param topic
	 *            the topic of the message.
	 * @return the original topic.
	 */
	private String getOriginalTopic(String topic) {
		String result = null;
		for (String key : mqttPropertiesMap.keySet()) {
			if (topic.startsWith(key.substring(0, key.length() - 1))) {
				result = key;
			}
		}
		return result;
	}

	/**
	 * Print the properties.
	 * 
	 * @param properties
	 */
	private void printProperties(Map<String, String[]> properties) {
		for (Map.Entry<String, String[]> entry : properties.entrySet()) {
			LOGGER.debug("Topic: " + entry.getKey());
			String[] values = entry.getValue();
			for (String value : values) {
				LOGGER.debug(value);
			}
		}
	}

	public static void main(String[] args) {
		LOGGER.info("Forwarding service starting");
		Forwarder forwarder = new Forwarder();
		forwarder.readproperties();
		forwarder.start();
	}

}
