package com.example.gmailviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GmailViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmailViewerApplication.class, args);
    }
}
