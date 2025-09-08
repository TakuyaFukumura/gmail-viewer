package com.example.gmailviewer.service;

import lombok.Data;

/**
 * メールサマリー情報を格納するクラス
 */
@Data
public class EmailSummary {
    private String id;
    private String threadId;
    private String subject;
    private String sender;
    private String date;
    private String snippet;
}
