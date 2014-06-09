package com.imaginea.adbexplorer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SystemProperties {
	
	private static Properties properties = new Properties();
	
	static {
		try {
			properties.load(new FileInputStream("system.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getAndroidSdkHome() {
		return (String)properties.get("ANDROID_SDK_HOME");
	}
}
