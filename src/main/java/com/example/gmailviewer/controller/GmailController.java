package com.example.gmailviewer.controller;

import com.example.gmailviewer.service.GmailService;
import com.example.gmailviewer.service.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Gmail機能用コントローラー
 */
@Controller
@RequestMapping("/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailController {

    private final GmailService gmailService;
    private final OAuthService oauthService;

    /**
     * メール一覧画面を表示
     *
     * @param model     ビューモデル
     * @param session   HTTPセッション
     * @param authParam 認証成功パラメータ
     * @return メール一覧テンプレート名
     */
    @GetMapping("/mails")
    public String listEmails(Model model, HttpSession session,
                             @RequestParam(value = "auth", required = false) String authParam) {
        log.info("メール一覧画面への請求を受信しました");

        // 認証成功メッセージを追加
        if ("success".equals(authParam)) {
            model.addAttribute("authSuccess", true);
        }

        try {
            var emails = gmailService.getEmailList(session);
            model.addAttribute("emails", emails);
            model.addAttribute("apiAvailable", gmailService.isGmailApiAvailable());
            model.addAttribute("authenticated", oauthService.isAuthenticated(session));

            log.info("取得したメール数: {}", emails.size());
            return "gmail/mails";

        } catch (Exception e) {
            log.error("メール一覧取得中にエラーが発生しました", e);
            model.addAttribute("error", "メール一覧の取得に失敗しました: " + e.getMessage());
            model.addAttribute("apiAvailable", false);
            model.addAttribute("authenticated", false);
            return "gmail/mails";
        }
    }

    /**
     * Gmail API設定画面を表示
     *
     * @param model      ビューモデル
     * @param session    HTTPセッション
     * @param errorParam エラーパラメータ
     * @return 設定画面テンプレート名
     */
    @GetMapping("/setup")
    public String showSetup(Model model, HttpSession session,
                            @RequestParam(value = "error", required = false) String errorParam) {
        log.info("Gmail API設定画面への請求を受信しました");

        model.addAttribute("apiAvailable", gmailService.isGmailApiAvailable());
        model.addAttribute("authenticated", oauthService.isAuthenticated(session));

        // エラーメッセージを追加
        if (errorParam != null) {
            String errorMessage = switch (errorParam) {
                case "access_denied" -> "認証が拒否されました。Googleアカウントでのログインが必要です。";
                case "auth_error" -> "認証中にエラーが発生しました。";
                case "no_code" -> "認証コードが取得できませんでした。";
                case "token_exchange_failed" -> "トークンの交換に失敗しました。";
                case "callback_error" -> "認証コールバック処理中にエラーが発生しました。";
                case "auth_start_failed" -> "認証を開始できませんでした。設定を確認してください。";
                default -> "不明なエラーが発生しました。";
            };
            model.addAttribute("error", errorMessage);
        }

        return "gmail/setup";
    }
}
