package com.example.gmailviewer.controller

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * IndexControllerのSpockテスト
 * MockMvcを使用してHTTPリクエストをテストする
 */
class IndexControllerSpec extends Specification {

    def indexController = new IndexController()
    def mockMvc = MockMvcBuilders.standaloneSetup(indexController).build()

    def "GETリクエストでgmail/mailsにリダイレクトされること"() {
        when: "ルートパスにGETリクエストを送信"
        def result = mockMvc.perform(get("/"))

        then: "ステータスが302でgmail/mailsにリダイレクトされる"
        result.andExpect(status().isFound())
              .andExpect(redirectedUrl("/gmail/mails"))
    }
}
