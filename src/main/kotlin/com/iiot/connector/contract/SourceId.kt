package com.iiot.connector.contract

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class SourceId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "SourceId must not be blank" }
    }
}
