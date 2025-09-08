package com.example.gmailviewer.controller;

import com.example.gmailviewer.config.GoogleOAuthConfig;
import com.example.gmailviewer.service.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

/**
 * OAuth 2.0認証処理用コントローラー
 */
@Controller
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oauthService;
    private final GoogleOAuthConfig oauthConfig;

    /**
     * Google OAuth認証を開始
     *
     * @param session HTTPセッション
     * @return Google認証ページへのリダイレクト
     */
    @GetMapping("/authorize")
    public RedirectView authorize(HttpSession session) {
        log.info("OAuth認証を開始します");

        try {
            String authorizationUrl = oauthService.getAuthorizationUrl(session);
            log.info("認証URLを生成しました: {}", authorizationUrl);
            return new RedirectView(authorizationUrl);
        } catch (Exception e) {
            log.error("OAuth認証開始中にエラーが発生しました", e);
            // エラー時はセットアップページにリダイレクト
            return new RedirectView("/gmail/setup?error=auth_start_failed");
        }
    }

    /**
     * OAuth認証コールバック処理
     *
     * @param code    Googleから返される認証コード
     * @param state   CSRF攻撃防止用のstate値
     * @param error   エラーパラメータ（認証拒否時など）
     * @param session HTTPセッション
     * @return メール一覧ページまたはエラーページへのリダイレクト
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session) {

        log.info("OAuth認証コールバックを受信しました. code={}, error={}",
                code != null ? "存在" : "なし", error);

        // エラーパラメータがある場合（ユーザーが認証を拒否した場合など）
        if (error != null) {
            log.warn("OAuth認証エラー: {}", error);
            if ("access_denied".equals(error)) {
                return new RedirectView("/gmail/setup?error=access_denied");
            } else {
                return new RedirectView("/gmail/setup?error=auth_error");
            }
        }

        // 認証コードがない場合
        if (code == null || code.trim().isEmpty()) {
            log.error("認証コードが提供されませんでした");
            return new RedirectView("/gmail/setup?error=no_code");
        }

        try {
            // 認証コードをアクセストークンに交換
            boolean success = oauthService.handleAuthorizationCallback(code, state, session);

            if (success) {
                log.info("OAuth認証が正常に完了しました");
                return new RedirectView("/gmail/mails?auth=success");
            } else {
                log.error("OAuth認証処理に失敗しました");
                return new RedirectView("/gmail/setup?error=token_exchange_failed");
            }

        } catch (Exception e) {
            log.error("OAuth認証コールバック処理中にエラーが発生しました", e);
            return new RedirectView("/gmail/setup?error=callback_error");
        }
    }

    /**
     * OAuth認証をリセット（ログアウト）
     *
     * @param session HTTPセッション
     * @return ホームページへのリダイレクト
     */
    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        log.info("OAuth認証をリセットします");

        try {
            oauthService.clearCredentials(session);
            session.invalidate();
            log.info("認証情報をクリアしました");
            return new RedirectView("/?logout=success");
        } catch (Exception e) {
            log.error("ログアウト処理中にエラーが発生しました", e);
            return new RedirectView("/?logout=error");
        }
    }
}