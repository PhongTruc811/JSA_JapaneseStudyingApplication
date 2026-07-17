package com.mescode.japanese.auth.oauth;

import java.io.*;
import java.util.Properties;

public class GitHubOAuthConfig {
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String CALLBACK_URL;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            Properties props = new Properties();
            InputStream input = GitHubOAuthConfig.class.getClassLoader()
                    .getResourceAsStream("oauth-config.properties");

            if (input == null) {
                System.out.println("oauth-config.properties not found");
                return;
            }

            props.load(input);
            CLIENT_ID = props.getProperty("github.client.id");
            CLIENT_SECRET = props.getProperty("github.client.secret");
            CALLBACK_URL = props.getProperty("github.callback.url");

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
        return "https://github.com/login/oauth/authorize?client_id=" + CLIENT_ID
                + "&redirect_uri=" + CALLBACK_URL
                + "&scope=user:email";
    }

    public static String getAccessTokenUrl() {
        return "https://github.com/login/oauth/access_token";
    }

    public static String getUserInfoUrl() {
        return "https://api.github.com/user";
    }
}
