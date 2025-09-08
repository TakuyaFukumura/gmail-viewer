package com.example.gmailviewer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Gmail API設定プロパティ
 */
@Configuration
@ConfigurationProperties(prefix = "app.gmail")
@Data
public class GmailConfig {

    /**
     * Gmail APIのアクセススコープ
     */
    private List<String> scopes;
}