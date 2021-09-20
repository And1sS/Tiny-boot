package org.and1ss.java_lab_1.util;

import org.and1ss.java_lab_1.Application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private PropertiesUtil() {
    }

    public static Properties loadProperties(String resourceName) throws IOException {
        final Properties applicationProperties = new Properties();
        final InputStream applicationPropertiesStream = Application.class.getClassLoader()
                .getResourceAsStream(resourceName);
        applicationProperties.load(applicationPropertiesStream);
        return applicationProperties;
    }
}
