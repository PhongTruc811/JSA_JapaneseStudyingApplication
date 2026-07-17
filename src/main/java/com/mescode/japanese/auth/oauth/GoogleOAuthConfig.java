package com.mescode.japanese.auth.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class GoogleOAuthConfig {
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String CALLBACK_URL;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            Properties props = new Properties();
            InputStream input = GoogleOAuthConfig.class.getClassLoader()
                    .getResourceAsStream("oauth-config.properties");

            if (input == null) {
                System.out.println("oauth-config.properties not found");
                return;
            }

            props.load(input);
            CLIENT_ID = props.getProperty("google.client.id");
            CLIENT_SECRET = props.getProperty("google.client.secret");
            CALLBACK_URL = props.getProperty("google.callback.url");

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getClientSecret() {
        return CLIENT_SECRET;
    }

    public static String getCallbackUrl() {
        return CALLBACK_URL;
    }

    public static String getAuthorizationUrl() {
        String encodedCallback = URLEncoder.encode(CALLBACK_URL, StandardCharsets.UTF_8);
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + encodedCallback
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&prompt=consent";
    }
}
