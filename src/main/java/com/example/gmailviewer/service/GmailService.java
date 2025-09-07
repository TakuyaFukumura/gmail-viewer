package com.example.gmailviewer.service;

import com.example.gmailviewer.config.GmailConfig;
import com.example.gmailviewer.config.GoogleOAuthConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gmail API操作サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GmailService {

    private final GoogleOAuthConfig oauthConfig;
    private final GmailConfig gmailConfig;
    
    private static final String APPLICATION_NAME = "Gmail Viewer";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Gmail APIクライアントを作成
     * 
     * @return Gmail APIクライアント
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private Gmail createGmailService() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // クライアント認証情報をJSON形式で作成
        String clientSecretsJson = String.format(
            "{\"installed\":{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"redirect_uris\":[\"%s\"]}}",
            oauthConfig.getClientId(),
            oauthConfig.getClientSecret(),
            oauthConfig.getRedirectUri()
        );
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY, 
            new InputStreamReader(new ByteArrayInputStream(clientSecretsJson.getBytes(StandardCharsets.UTF_8)))
        );

        // OAuth 2.0認証フローを構築
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, gmailConfig.getScopes())
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Gmail APIサービスが利用可能かチェック
     * 
     * @return 利用可能な場合true
     */
    public boolean isGmailApiAvailable() {
        try {
            // 必要な設定がすべて存在するかチェック
            if (oauthConfig.getClientId() == null || oauthConfig.getClientId().equals("your-client-id-here") ||
                oauthConfig.getClientSecret() == null || oauthConfig.getClientSecret().equals("your-client-secret-here")) {
                log.warn("Gmail API設定が不完全です。GOOGLE_CLIENT_IDとGOOGLE_CLIENT_SECRETを設定してください。");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Gmail API利用可能性チェック中にエラーが発生しました", e);
            return false;
        }
    }

    /**
     * メール一覧を取得（最新10件）
     * 
     * @return メール一覧
     */
    public List<EmailSummary> getEmailList() {
        if (!isGmailApiAvailable()) {
            log.warn("Gmail APIが利用できません。サンプルデータを返します。");
            return getSampleEmails();
        }

        try {
            Gmail service = createGmailService();
            String user = "me";
            
            ListMessagesResponse response = service.users().messages().list(user)
                    .setMaxResults(10L)
                    .execute();
            
            List<Message> messages = response.getMessages();
            List<EmailSummary> emailSummaries = new ArrayList<>();
            
            if (messages != null) {
                for (Message message : messages) {
                    try {
                        Message fullMessage = service.users().messages().get(user, message.getId()).execute();
                        EmailSummary summary = createEmailSummary(fullMessage);
                        emailSummaries.add(summary);
                    } catch (GoogleJsonResponseException e) {
                        GoogleJsonError error = e.getDetails();
                        log.error("Gmail API エラー: {} - {}", error.getCode(), error.getMessage());
                    }
                }
            }
            
            return emailSummaries;
            
        } catch (IOException | GeneralSecurityException e) {
            log.error("メール一覧取得中にエラーが発生しました", e);
            return getSampleEmails();
        }
    }

    /**
     * メッセージからEmailSummaryを作成
     * 
     * @param message Gmail Message
     * @return EmailSummary
     */
    private EmailSummary createEmailSummary(Message message) {
        EmailSummary summary = new EmailSummary();
        summary.setId(message.getId());
        summary.setThreadId(message.getThreadId());
        
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            message.getPayload().getHeaders().forEach(header -> {
                switch (header.getName().toLowerCase()) {
                    case "from":
                        summary.setSender(header.getValue());
                        break;
                    case "subject":
                        summary.setSubject(header.getValue());
                        break;
                    case "date":
                        summary.setDate(header.getValue());
                        break;
                }
            });
        }
        
        // メール本文のスニペットを設定
        if (message.getSnippet() != null) {
            summary.setSnippet(message.getSnippet());
        }
        
        return summary;
    }

    /**
     * サンプルメールデータを生成（APIが利用できない場合）
     * 
     * @return サンプルメール一覧
     */
    private List<EmailSummary> getSampleEmails() {
        List<EmailSummary> samples = new ArrayList<>();
        
        EmailSummary sample1 = new EmailSummary();
        sample1.setId("sample1");
        sample1.setSubject("Gmail Viewerへようこそ");
        sample1.setSender("example@gmail.com");
        sample1.setDate("2025-01-07 14:00:00");
        sample1.setSnippet("Gmail APIの設定が完了したら、実際のメールが表示されます。GOOGLE_CLIENT_IDとGOOGLE_CLIENT_SECRETを環境変数で設定してください。");
        samples.add(sample1);
        
        EmailSummary sample2 = new EmailSummary();
        sample2.setId("sample2");
        sample2.setSubject("設定方法について");
        sample2.setSender("support@example.com");
        sample2.setDate("2025-01-07 13:30:00");
        sample2.setSnippet("1. Google Cloud Consoleでプロジェクトを作成 2. Gmail APIを有効化 3. OAuth 2.0クライアントIDを作成 4. 環境変数を設定");
        samples.add(sample2);
        
        EmailSummary sample3 = new EmailSummary();
        sample3.setId("sample3");
        sample3.setSubject("サンプルメール3");
        sample3.setSender("test@example.com");
        sample3.setDate("2025-01-07 12:00:00");
        sample3.setSnippet("これはサンプルメールです。実際のGmail APIが設定されると、本物のメールが表示されます。");
        samples.add(sample3);
        
        return samples;
    }

    /**
     * メールサマリー情報を格納するクラス
     */
    public static class EmailSummary {
        private String id;
        private String threadId;
        private String subject;
        private String sender;
        private String date;
        private String snippet;

        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getThreadId() { return threadId; }
        public void setThreadId(String threadId) { this.threadId = threadId; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    }
}