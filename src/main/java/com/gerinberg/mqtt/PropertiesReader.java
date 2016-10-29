package com.gerinberg.mqtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to read properties from a file.
 * 
 * @author Ger Inberg
 */
public class PropertiesReader {

	public static final String SEPARATOR = ";";
	private final String propFileName;

	public PropertiesReader(String propFileName) {
		this.propFileName = propFileName;
	}

	/**
	 * Get property values from file.
	 * 
	 * @return a map with the properties.
	 * @throws IOException
	 *             in case of an error.
	 */
	public Map<String, String[]> getPropValues() throws IOException {
		Map<String, String[]> properties = null;
		BufferedReader in = null;
		InputStream is = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (is != null) {
			in = new BufferedReader(new InputStreamReader(is));

			properties = new HashMap<String, String[]>();
			String line = null;
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("**")) {
					String[] lineParts = line.split(SEPARATOR);
					properties.put(lineParts[0], Arrays.copyOfRange(lineParts, 1, lineParts.length));
				}
			}
		} else {
			throw new IOException("File " + propFileName + " could not be read from classpath!");
		}
		return properties;
	}

}
