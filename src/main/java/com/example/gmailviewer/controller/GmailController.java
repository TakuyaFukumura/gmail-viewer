package com.example.gmailviewer.controller;

import com.example.gmailviewer.service.GmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Gmail機能用コントローラー
 */
@Controller
@RequestMapping("/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailController {

    private final GmailService gmailService;

    /**
     * メール一覧画面を表示
     * 
     * @param model ビューモデル
     * @return メール一覧テンプレート名
     */
    @GetMapping("/mails")
    public String listEmails(Model model) {
        log.info("メール一覧画面への請求を受信しました");
        
        try {
            var emails = gmailService.getEmailList();
            model.addAttribute("emails", emails);
            model.addAttribute("apiAvailable", gmailService.isGmailApiAvailable());
            
            log.info("取得したメール数: {}", emails.size());
            return "gmail/mails";
            
        } catch (Exception e) {
            log.error("メール一覧取得中にエラーが発生しました", e);
            model.addAttribute("error", "メール一覧の取得に失敗しました: " + e.getMessage());
            model.addAttribute("apiAvailable", false);
            return "gmail/mails";
        }
    }

    /**
     * Gmail API設定画面を表示
     * 
     * @param model ビューモデル
     * @return 設定画面テンプレート名
     */
    @GetMapping("/setup")
    public String showSetup(Model model) {
        log.info("Gmail API設定画面への請求を受信しました");
        
        model.addAttribute("apiAvailable", gmailService.isGmailApiAvailable());
        return "gmail/setup";
    }
}