package com.example.gmailviewer.service;

import com.example.gmailviewer.config.GoogleOAuthConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private static final String APPLICATION_NAME = "Gmail Viewer";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final GoogleOAuthConfig oauthConfig;
    private final OAuthService oauthService;

    /**
     * Gmail APIクライアントを作成
     *
     * @param session HTTPセッション
     * @return Gmail APIクライアント
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private Gmail createGmailService(HttpSession session) throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = oauthService.getCredential(session);
        if (credential == null) {
            throw new IllegalStateException("OAuth認証が必要です");
        }

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
     * @param session HTTPセッション
     * @return メール一覧
     */
    public List<EmailSummary> getEmailList(HttpSession session) {
        if (!isGmailApiAvailable()) {
            log.warn("Gmail APIが利用できません。サンプルデータを返します。");
            return getSampleEmails();
        }

        if (!oauthService.isAuthenticated(session)) {
            log.warn("OAuth認証が必要です。サンプルデータを返します。");
            return getSampleEmails();
        }

        try {
            Gmail service = createGmailService(session);
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
    @Setter
    @Getter
    public static class EmailSummary {
        private String id;
        private String threadId;
        private String subject;
        private String sender;
        private String date;
        private String snippet;
    }
}
