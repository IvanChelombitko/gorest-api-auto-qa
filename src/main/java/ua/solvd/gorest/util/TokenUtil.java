package ua.solvd.gorest.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TokenUtil {
    private TokenUtil() {
    }

    public static String getTokenFromProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Property file 'config.properties' not found in the classpath.");
            }
            properties.load(inputStream);
            String token = properties.getProperty("gorest.api.token");
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token 'gorest.api.token' is empty or missing in config.properties.");
            }
            return token;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from config.properties.", e);
        }
    }
}