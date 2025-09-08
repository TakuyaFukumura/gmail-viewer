package com.example.gmailviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/") // URLとの関連付け http://localhost:8080/ の時に呼ばれる
public class IndexController {

    @GetMapping // Getされた時の処理 Postは別
    public String index() {
        return "index";
    }
}
