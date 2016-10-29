package com.gerinberg.mqtt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

public class InvokeProtoc {

	public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		byte[] payload = new byte[] {8, 1, 18, 29, 10, 27, 8, 0, 18, 2, 8, 0, 26, 14, 8, 0, 16, -50, 1, 24, 10, 32, -128, -106, 1, 40, -5, 51, 69, 0, 104, -125, 69};

		String currentDir = System.getProperty("user.dir");
		File protoc = new File("/usr/bin/protoc");
		File protoPath = new File(currentDir + "/src/main/resources/");
		File sourcePath = new File(currentDir + "/src/main/java/");
		File protoFile = new File(currentDir + "/src/main/resources/bim_001.proto");
		File genSource = new File(sourcePath + "/com/gerinberg/mqtt/Bim001.java");

		// protoc --proto_path=/home/ger/workspace/mqtt/src/main/resources/
		// --java_out=/home/ger/workspace/mqtt/src/main/java
		// /home/ger/workspace/mqtt/src/main/resources/bim_001.proto

		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		final String command = protoc.getPath() + " --proto_path=" + protoPath.getPath() + " --java_out="
				+ sourcePath.getPath() + " " + protoFile.getPath();
		System.out.println("command = " + command);

		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command, null, new File(currentDir));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		if (process != null) {
			int result = -1;
			try {
				result = process.waitFor();
			} catch (InterruptedException e) {
				// ;
			}
			if (result == 0) {
				// Success!
				System.out.println("succes!");
				
				Class[] paramByteArray = new Class[1];
				paramByteArray[0] = byte[].class;

				Class eventClass = Class.forName("com.gerinberg.mqtt.Bim001$Event");
				System.out.println(eventClass.getName());

				Method method = eventClass.getDeclaredMethod("parseFrom", paramByteArray);
				System.out.println(method.getName());
				
				Object event = method.invoke(null, payload);
				
				String message = JsonFormat.printToString((Message) event);
				System.out.println(message);

			} else {
				System.out.println("failure:" + result);
			}
		}

	}

}
