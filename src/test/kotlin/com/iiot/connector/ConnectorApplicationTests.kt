package com.iiot.connector

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ConnectorApplicationTests {
    @Test
//    fun contextLoads() {
//    }
    fun contextLoads() {
        throw RuntimeException("CI TEST FAILURE")
    }
}
