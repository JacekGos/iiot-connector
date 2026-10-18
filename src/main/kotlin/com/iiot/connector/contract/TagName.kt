package com.iiot.connector.contract

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class TagName(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "TagName must not be blank" }
    }
}
