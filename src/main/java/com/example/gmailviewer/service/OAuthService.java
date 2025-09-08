package com.example.gmailviewer.service;

import com.example.gmailviewer.config.GmailConfig;
import com.example.gmailviewer.config.GoogleOAuthConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * OAuth 2.0認証処理サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SESSION_STATE_KEY = "oauth_state";
    private static final String SESSION_CREDENTIAL_KEY = "oauth_credential";
    private final GoogleOAuthConfig oauthConfig;
    private final GmailConfig gmailConfig;

    /**
     * OAuth認証URLを生成
     *
     * @param session HTTPセッション
     * @return 認証URL
     */
    public String getAuthorizationUrl(HttpSession session) throws IOException, GeneralSecurityException {
        // CSRF攻撃防止用のstate値を生成
        String state = generateState();
        session.setAttribute(SESSION_STATE_KEY, state);

        GoogleAuthorizationCodeFlow flow = createAuthorizationCodeFlow();

        GoogleAuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(oauthConfig.getRedirectUri())
                .setState(state)
                .setAccessType("offline")
                .setApprovalPrompt("force"); // リフレッシュトークンを確実に取得

        return authorizationUrl.build();
    }

    /**
     * 認証コールバック処理
     *
     * @param code    認証コード
     * @param state   CSRF攻撃防止用のstate値
     * @param session HTTPセッション
     * @return 認証成功の場合true
     */
    public boolean handleAuthorizationCallback(String code, String state, HttpSession session)
            throws IOException, GeneralSecurityException {

        // state値を検証してCSRF攻撃を防止
        String sessionState = (String) session.getAttribute(SESSION_STATE_KEY);
        if (sessionState == null || !sessionState.equals(state)) {
            log.error("無効なstate値です. 期待値: {}, 受信値: {}", sessionState, state);
            return false;
        }

        GoogleAuthorizationCodeFlow flow = createAuthorizationCodeFlow();

        // 認証コードをアクセストークンに交換
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(oauthConfig.getRedirectUri())
                .execute();

        // 認証情報をセッションに保存
        Credential credential = flow.createAndStoreCredential(tokenResponse, getUserId(session));
        session.setAttribute(SESSION_CREDENTIAL_KEY, credential);

        log.info("OAuth認証が正常に完了しました. ユーザーID: {}", getUserId(session));
        return true;
    }

    /**
     * セッションから認証情報を取得
     *
     * @param session HTTPセッション
     * @return 認証情報（存在しない場合はnull）
     */
    public Credential getCredential(HttpSession session) {
        return (Credential) session.getAttribute(SESSION_CREDENTIAL_KEY);
    }

    /**
     * 認証情報をクリア
     *
     * @param session HTTPセッション
     */
    public void clearCredentials(HttpSession session) {
        session.removeAttribute(SESSION_CREDENTIAL_KEY);
        session.removeAttribute(SESSION_STATE_KEY);
    }

    /**
     * 認証済みかどうかをチェック
     *
     * @param session HTTPセッション
     * @return 認証済みの場合true
     */
    public boolean isAuthenticated(HttpSession session) {
        Credential credential = getCredential(session);
        return credential != null && credential.getAccessToken() != null;
    }

    /**
     * GoogleAuthorizationCodeFlowを作成
     *
     * @return GoogleAuthorizationCodeFlow
     */
    private GoogleAuthorizationCodeFlow createAuthorizationCodeFlow()
            throws IOException, GeneralSecurityException {

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // クライアント認証情報をJSON形式で作成
        String clientSecretsJson = String.format(
                "{\"web\":{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"redirect_uris\":[\"%s\"]}}",
                oauthConfig.getClientId(),
                oauthConfig.getClientSecret(),
                oauthConfig.getRedirectUri()
        );

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(new ByteArrayInputStream(clientSecretsJson.getBytes(StandardCharsets.UTF_8)))
        );

        // メモリベースのデータストアを使用（セッション管理）
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, gmailConfig.getScopes())
                .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
                .setAccessType("offline")
                .build();
    }

    /**
     * セッションからユーザーIDを取得（セッションIDを使用）
     *
     * @param session HTTPセッション
     * @return ユーザーID
     */
    private String getUserId(HttpSession session) {
        return session.getId();
    }

    /**
     * CSRF攻撃防止用のランダムstate値を生成
     *
     * @return state値
     */
    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
