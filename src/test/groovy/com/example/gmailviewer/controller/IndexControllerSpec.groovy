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

    def "GETリクエストでindexページが正しく表示されること"() {
        when: "ルートパスにGETリクエストを送信"
        def result = mockMvc.perform(get("/"))

        then: "ステータスが200でindexビューが返される"
        result.andExpect(status().isOk())
              .andExpect(view().name("index"))
    }
}
