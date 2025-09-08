package com.example.gmailviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
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