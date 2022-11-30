package com.carrot.croquis.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class HelloControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `hello를 응답`() {

        val result = client
            .get()
            .uri("/api/hello")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        assertThat(result).isEqualTo("Hello")
    }
}