package com.example.gmailviewer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Google OAuth設定プロパティ
 */
@Configuration
@ConfigurationProperties(prefix = "app.google")
@Data
public class GoogleOAuthConfig {

    /**
     * GoogleのOAuth 2.0クライアントID
     */
    private String clientId;

    /**
     * GoogleのOAuth 2.0クライアントシークレット
     */
    private String clientSecret;

    /**
     * OAuth認証後のリダイレクトURI
     */
    private String redirectUri;
}