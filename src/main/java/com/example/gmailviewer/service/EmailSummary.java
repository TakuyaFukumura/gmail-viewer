package com.example.gmailviewer.service;

import lombok.Getter;
import lombok.Setter;

/**
 * メールサマリー情報を格納するクラス
 */
@Setter
@Getter
public class EmailSummary {
    private String id;
    private String threadId;
    private String subject;
    private String sender;
    private String date;
    private String snippet;
}
